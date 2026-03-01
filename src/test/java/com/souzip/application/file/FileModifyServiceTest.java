package com.souzip.application.file;

import com.souzip.application.file.required.FileRepository;
import com.souzip.application.file.required.FileStorage;
import com.souzip.domain.file.File;
import com.souzip.domain.file.FileRegisterRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FileModifyServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileStorage fileStorage;

    @InjectMocks
    private FileModifyService fileModifyService;

    @DisplayName("파일을 등록한다 - displayOrder 지정")
    @Test
    void register_withDisplayOrder() {
        // given
        MultipartFile multipartFile = createMockMultipartFile();
        String storageKey = "user123/uuid.jpg";

        given(fileStorage.upload("user123", multipartFile)).willReturn(storageKey);
        given(fileRepository.save(any(File.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        File result = fileModifyService.register("user123", "NOTICE", 1L, multipartFile, 5);

        // then
        assertThat(result.getEntityType()).isEqualTo("NOTICE");
        assertThat(result.getEntityId()).isEqualTo(1L);
        assertThat(result.getStorageKey()).isEqualTo(storageKey);
        assertThat(result.getDisplayOrder()).isEqualTo(5);

        then(fileStorage).should().upload("user123", multipartFile);
        then(fileRepository).should().save(any(File.class));
    }

    @DisplayName("파일을 등록한다 - displayOrder 자동 계산 (첫 파일)")
    @Test
    void register_autoDisplayOrder_first() {
        // given
        MultipartFile multipartFile = createMockMultipartFile();
        String storageKey = "user123/uuid.jpg";

        given(fileStorage.upload("user123", multipartFile)).willReturn(storageKey);
        given(fileRepository.findByEntityTypeAndEntityIdOrderByDisplayOrderAsc("NOTICE", 1L))
                .willReturn(List.of());
        given(fileRepository.save(any(File.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        File result = fileModifyService.register("user123", "NOTICE", 1L, multipartFile, null);

        // then
        assertThat(result.getDisplayOrder()).isEqualTo(1);
    }

    @DisplayName("파일을 등록한다 - displayOrder 자동 계산 (기존 파일 있음)")
    @Test
    void register_autoDisplayOrder_notFirst() {
        // given
        MultipartFile multipartFile = createMockMultipartFile();
        String storageKey = "user123/uuid.jpg";
        File existingFile = createFile(1L, "NOTICE", 1L, 3);

        given(fileStorage.upload("user123", multipartFile)).willReturn(storageKey);
        given(fileRepository.findByEntityTypeAndEntityIdOrderByDisplayOrderAsc("NOTICE", 1L))
                .willReturn(List.of(existingFile));
        given(fileRepository.save(any(File.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        File result = fileModifyService.register("user123", "NOTICE", 1L, multipartFile, null);

        // then
        assertThat(result.getDisplayOrder()).isEqualTo(4);
    }

    @DisplayName("파일을 삭제한다")
    @Test
    void delete() {
        // given
        File file = createFile(1L, "NOTICE", 1L, 1);

        given(fileRepository.findById(1L)).willReturn(Optional.of(file));

        // when
        fileModifyService.delete(1L);

        // then
        then(fileStorage).should().delete(file.getStorageKey());
        then(fileRepository).should().delete(file);
    }

    @DisplayName("존재하지 않는 파일 삭제 시 예외가 발생한다")
    @Test
    void delete_notFound() {
        // given
        given(fileRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fileModifyService.delete(999L))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("999");
    }

    @DisplayName("엔티티의 모든 파일을 삭제한다")
    @Test
    void deleteByEntity() {
        // given
        File file1 = createFile(1L, "NOTICE", 1L, 1);
        File file2 = createFile(2L, "NOTICE", 1L, 2);
        List<File> files = List.of(file1, file2);

        given(fileRepository.findByEntityTypeAndEntityIdOrderByDisplayOrderAsc("NOTICE", 1L))
                .willReturn(files);

        // when
        fileModifyService.deleteByEntity("NOTICE", 1L);

        // then
        then(fileStorage).should().delete(file1.getStorageKey());
        then(fileStorage).should().delete(file2.getStorageKey());
        then(fileRepository).should(times(2)).delete(any(File.class));
    }

    private MultipartFile createMockMultipartFile() {
        MultipartFile file = mock(MultipartFile.class);
        given(file.getOriginalFilename()).willReturn("test.jpg");
        given(file.getSize()).willReturn(1024L);
        given(file.getContentType()).willReturn("image/jpeg");
        return file;
    }

    private File createFile(Long id, String entityType, Long entityId, Integer displayOrder) {
        FileRegisterRequest request = FileRegisterRequest.of(
                entityType,
                entityId,
                "storage-key-" + id,
                "file" + id + ".jpg",
                1024L,
                "image/jpeg",
                displayOrder
        );
        return File.register(request);
    }
}
