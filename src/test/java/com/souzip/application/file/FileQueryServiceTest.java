package com.souzip.application.file;

import com.souzip.application.file.required.FileRepository;
import com.souzip.domain.file.File;
import com.souzip.domain.file.FileRegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FileQueryServiceTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileQueryService fileQueryService;

    @DisplayName("엔티티에 속한 모든 파일을 조회한다")
    @Test
    void findByEntity() {
        // given
        File file1 = createFile(1L, "NOTICE", 1L, 1);
        File file2 = createFile(2L, "NOTICE", 1L, 2);
        List<File> files = List.of(file1, file2);

        given(fileRepository.findByEntityTypeAndEntityIdOrderByDisplayOrderAsc("NOTICE", 1L))
                .willReturn(files);

        // when
        List<File> result = fileQueryService.findByEntity("NOTICE", 1L);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(file1, file2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @DisplayName("엔티티의 첫 번째 파일을 조회한다")
    @Test
    void findFirst() {
        // given
        File file = createFile(1L, "NOTICE", 1L, 1);

        given(fileRepository.findFirstByEntityTypeAndEntityIdOrderByDisplayOrderAsc("NOTICE", 1L))
                .willReturn(Optional.of(file));

        // when
        File result = fileQueryService.findFirst("NOTICE", 1L);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDisplayOrder()).isEqualTo(1);
    }

    @DisplayName("첫 번째 파일이 없으면 예외가 발생한다")
    @Test
    void findFirst_notFound() {
        // given
        given(fileRepository.findFirstByEntityTypeAndEntityIdOrderByDisplayOrderAsc("NOTICE", 1L))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fileQueryService.findFirst("NOTICE", 1L))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("NOTICE")
                .hasMessageContaining("1");
    }

    @DisplayName("여러 엔티티의 썸네일을 일괄 조회한다")
    @Test
    void findThumbnailsByEntityIds() {
        // given
        File file1 = createFile(1L, "NOTICE", 1L, 1);
        File file2 = createFile(2L, "NOTICE", 2L, 1);
        List<File> files = List.of(file1, file2);
        List<Long> entityIds = List.of(1L, 2L);

        given(fileRepository.findByEntityTypeAndEntityIdInAndDisplayOrderOrderByDisplayOrder(
                eq("NOTICE"), eq(entityIds), eq(1)
        )).willReturn(files);

        // when
        Map<Long, File> result = fileQueryService.findThumbnailsByEntityIds("NOTICE", entityIds);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(1L).getId()).isEqualTo(1L);
        assertThat(result.get(2L).getId()).isEqualTo(2L);
    }

    @DisplayName("엔티티 ID 목록이 비어있으면 빈 Map을 반환한다")
    @Test
    void findThumbnailsByEntityIds_emptyList() {
        // when
        Map<Long, File> result = fileQueryService.findThumbnailsByEntityIds("NOTICE", List.of());

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("엔티티 ID 목록이 null이면 빈 Map을 반환한다")
    @Test
    void findThumbnailsByEntityIds_nullList() {
        // when
        Map<Long, File> result = fileQueryService.findThumbnailsByEntityIds("NOTICE", null);

        // then
        assertThat(result).isEmpty();
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
        File file = File.register(request);
        ReflectionTestUtils.setField(file, "id", id);
        return file;
    }
}
