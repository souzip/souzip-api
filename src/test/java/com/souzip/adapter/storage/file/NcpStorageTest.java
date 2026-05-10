package com.souzip.adapter.storage.file;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.souzip.adapter.config.ObjectStorageProperties;
import com.souzip.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class NcpStorageTest {

    @Mock
    private AmazonS3 s3Client;

    @Mock
    private ObjectStorageProperties properties;

    @InjectMocks
    private NcpStorage ncpStorage;

    @DisplayName("파일을 정상적으로 업로드한다")
    @Test
    void upload() throws IOException {
        // given
        given(properties.getBucket()).willReturn("test-bucket");

        MultipartFile file = createValidMultipartFile();
        given(file.getInputStream()).willReturn(new ByteArrayInputStream(new byte[1024]));
        String userId = "550e8400-e29b-41d4-a716-446655440000";  // UUID 형식 (36글자)

        // when
        String storageKey = ncpStorage.upload(userId, file);

        // then
        assertThat(storageKey).startsWith("550e8400e29b");  // 하이픈 제거 후 12글자
        assertThat(storageKey).endsWith(".jpg");
        assertThat(storageKey).contains("/");

        then(s3Client).should().putObject(
                eq("test-bucket"),
                anyString(),
                any(InputStream.class),
                any(ObjectMetadata.class)
        );
    }

    @DisplayName("빈 파일 업로드 시 예외가 발생한다")
    @Test
    void uploadWithEmptyFile() {
        // given
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(true);

        // when & then
        assertThatThrownBy(() -> ncpStorage.upload("user-id", file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("비어있습니다");
    }

    @DisplayName("파일 크기 초과 시 예외가 발생한다")
    @Test
    void uploadWithOversizedFile() {
        // given
        long oversizedFile = 51 * 1024 * 1024; // 51MB
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);
        given(file.getSize()).willReturn(oversizedFile);

        // when & then
        assertThatThrownBy(() -> ncpStorage.upload("user-id", file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("초과");
    }

    @DisplayName("허용되지 않은 파일 타입 업로드 시 예외가 발생한다")
    @Test
    void uploadWithInvalidFileType() {
        // given
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);
        given(file.getSize()).willReturn(1024L);
        given(file.getOriginalFilename()).willReturn("virus.exe");

        // when & then
        assertThatThrownBy(() -> ncpStorage.upload("user-id", file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("허용되지 않은");
    }

    @DisplayName("S3 업로드 실패 시 예외가 발생한다")
    @Test
    void uploadWithS3Error() throws IOException {
        // given
        given(properties.getBucket()).willReturn("test-bucket");

        MultipartFile file = createValidMultipartFile();
        given(file.getInputStream()).willThrow(new IOException("S3 error"));
        String userId = "550e8400-e29b-41d4-a716-446655440000";

        // when & then
        assertThatThrownBy(() -> ncpStorage.upload(userId, file))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("업로드");
    }

    @DisplayName("파일을 정상적으로 삭제한다")
    @Test
    void delete() {
        // given
        given(properties.getBucket()).willReturn("test-bucket");
        String storageKey = "user123/uuid-1234.jpg";

        // when
        ncpStorage.delete(storageKey);

        // then
        then(s3Client).should().deleteObject("test-bucket", storageKey);
    }

    @DisplayName("S3 삭제 실패 시 예외가 발생한다")
    @Test
    void deleteWithS3Error() {
        // given
        given(properties.getBucket()).willReturn("test-bucket");

        String storageKey = "user123/uuid-1234.jpg";
        AmazonServiceException exception = new AmazonServiceException("Delete failed");
        willThrow(exception).given(s3Client).deleteObject(anyString(), anyString());

        // when & then
        assertThatThrownBy(() -> ncpStorage.delete(storageKey))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("삭제");
    }

    @DisplayName("임시 URL을 생성한다")
    @Test
    void generateUrl() throws Exception {
        // given
        given(properties.getBucket()).willReturn("test-bucket");

        String storageKey = "user123/uuid-1234.jpg";
        URL mockUrl = new URL("https://test-bucket.s3.amazonaws.com/" + storageKey);
        given(s3Client.generatePresignedUrl(
                eq("test-bucket"),
                eq(storageKey),
                any(Date.class)
        )).willReturn(mockUrl);

        // when
        String url = ncpStorage.generateUrl(storageKey);

        // then
        assertThat(url).contains(storageKey);
        assertThat(url).startsWith("https://");
    }

    private MultipartFile createValidMultipartFile() {
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);
        given(file.getSize()).willReturn(1024L);
        given(file.getOriginalFilename()).willReturn("test.jpg");
        given(file.getContentType()).willReturn("image/jpeg");
        return file;
    }
}
