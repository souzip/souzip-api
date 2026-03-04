package com.souzip.application.file;

import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.file.required.FileRepository;
import com.souzip.application.file.required.FileStorage;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.file.File;
import com.souzip.domain.file.FileRegisterRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FileQueryServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileStorage fileStorage;

    @InjectMocks
    private FileQueryService fileQueryService;

    @DisplayName("엔티티에 속한 모든 파일을 조회한다")
    @Test
    void findByEntity() {
        File file1 = createFile(1L, EntityType.NOTICE, 1L, 1);
        File file2 = createFile(2L, EntityType.NOTICE, 1L, 2);
        List<File> files = List.of(file1, file2);

        given(fileRepository.findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(EntityType.NOTICE, 1L))
                .willReturn(files);

        List<File> result = fileQueryService.findByEntity(EntityType.NOTICE, 1L);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(file1, file2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @DisplayName("엔티티의 첫 번째 파일을 조회한다")
    @Test
    void findFirst() {
        File file = createFile(1L, EntityType.NOTICE, 1L, 1);

        given(fileRepository.findFirstByEntityTypeAndEntityIdOrderByDisplayOrderAsc(EntityType.NOTICE, 1L))
                .willReturn(Optional.of(file));

        File result = fileQueryService.findFirst(EntityType.NOTICE, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDisplayOrder()).isEqualTo(1);
    }

    @DisplayName("첫 번째 파일이 없으면 예외가 발생한다")
    @Test
    void findFirst_notFound() {
        given(fileRepository.findFirstByEntityTypeAndEntityIdOrderByDisplayOrderAsc(EntityType.NOTICE, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> fileQueryService.findFirst(EntityType.NOTICE, 1L))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("NOTICE")
                .hasMessageContaining("1");
    }

    @DisplayName("여러 엔티티의 썸네일을 일괄 조회한다")
    @Test
    void findThumbnailsByEntityIds() {
        File file1 = createFile(1L, EntityType.NOTICE, 1L, 1);
        File file2 = createFile(2L, EntityType.NOTICE, 2L, 1);
        List<File> files = List.of(file1, file2);
        List<Long> entityIds = List.of(1L, 2L);

        given(fileRepository.findByEntityTypeAndEntityIdInAndDisplayOrderOrderByDisplayOrder(
                eq(EntityType.NOTICE), eq(entityIds), eq(1)
        )).willReturn(files);

        Map<Long, File> result = fileQueryService.findThumbnailsByEntityIds(EntityType.NOTICE, entityIds);

        assertThat(result).hasSize(2);
        assertThat(result.get(1L).getId()).isEqualTo(1L);
        assertThat(result.get(2L).getId()).isEqualTo(2L);
    }

    @DisplayName("엔티티 ID 목록이 비어있으면 빈 Map을 반환한다")
    @Test
    void findThumbnailsByEntityIds_emptyList() {
        Map<Long, File> result = fileQueryService.findThumbnailsByEntityIds(EntityType.NOTICE, List.of());

        assertThat(result).isEmpty();
    }

    @DisplayName("엔티티 ID 목록이 null이면 빈 Map을 반환한다")
    @Test
    void findThumbnailsByEntityIds_nullList() {
        Map<Long, File> result = fileQueryService.findThumbnailsByEntityIds(EntityType.NOTICE, null);

        assertThat(result).isEmpty();
    }

    @DisplayName("URL과 함께 파일 응답 목록을 조회한다")
    @Test
    void findFileResponsesByEntity() {
        File file1 = createFile(1L, EntityType.NOTICE, 1L, 1);
        File file2 = createFile(2L, EntityType.NOTICE, 1L, 2);
        List<File> files = List.of(file1, file2);

        given(fileRepository.findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(EntityType.NOTICE, 1L))
                .willReturn(files);
        given(fileStorage.generateUrl("storage-key-1"))
                .willReturn("https://example.com/file1.jpg");
        given(fileStorage.generateUrl("storage-key-2"))
                .willReturn("https://example.com/file2.jpg");

        List<FileResponse> result = fileQueryService.findFileResponsesByEntity(EntityType.NOTICE, 1L);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().id()).isEqualTo(1L);
        assertThat(result.getFirst().url()).isEqualTo("https://example.com/file1.jpg");
        assertThat(result.get(0).originalName()).isEqualTo("file1.jpg");
        assertThat(result.get(0).displayOrder()).isEqualTo(1);

        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).url()).isEqualTo("https://example.com/file2.jpg");
        assertThat(result.get(1).originalName()).isEqualTo("file2.jpg");
        assertThat(result.get(1).displayOrder()).isEqualTo(2);
    }

    @DisplayName("파일이 없으면 빈 응답 목록을 반환한다")
    @Test
    void findFileResponsesByEntity_emptyFiles() {
        given(fileRepository.findByEntityTypeAndEntityIdOrderByDisplayOrderAsc(EntityType.NOTICE, 1L))
                .willReturn(List.of());

        List<FileResponse> result = fileQueryService.findFileResponsesByEntity(EntityType.NOTICE, 1L);

        assertThat(result).isEmpty();
    }

    @DisplayName("여러 엔티티의 파일을 일괄 조회한다")
    @Test
    void findFilesByEntityIds() {
        File file1 = createFile(1L, EntityType.NOTICE, 1L, 1);
        File file2 = createFile(2L, EntityType.NOTICE, 1L, 2);
        File file3 = createFile(3L, EntityType.NOTICE, 2L, 1);
        List<File> files = List.of(file1, file2, file3);
        List<Long> entityIds = List.of(1L, 2L);

        given(fileRepository.findByEntityTypeAndEntityIdIn(EntityType.NOTICE, entityIds))
                .willReturn(files);
        given(fileStorage.generateUrl("storage-key-1"))
                .willReturn("https://example.com/file1.jpg");
        given(fileStorage.generateUrl("storage-key-2"))
                .willReturn("https://example.com/file2.jpg");
        given(fileStorage.generateUrl("storage-key-3"))
                .willReturn("https://example.com/file3.jpg");

        Map<Long, List<FileResponse>> result = fileQueryService.findFilesByEntityIds(
                EntityType.NOTICE,
                entityIds
        );

        assertThat(result).hasSize(2);

        assertThat(result.get(1L)).hasSize(2);
        assertThat(result.get(1L).get(0).id()).isEqualTo(1L);
        assertThat(result.get(1L).get(0).url()).isEqualTo("https://example.com/file1.jpg");
        assertThat(result.get(1L).get(1).id()).isEqualTo(2L);
        assertThat(result.get(1L).get(1).url()).isEqualTo("https://example.com/file2.jpg");

        assertThat(result.get(2L)).hasSize(1);
        assertThat(result.get(2L).get(0).id()).isEqualTo(3L);
        assertThat(result.get(2L).get(0).url()).isEqualTo("https://example.com/file3.jpg");
    }

    @DisplayName("파일이 없는 엔티티는 결과에 포함되지 않는다")
    @Test
    void findFilesByEntityIds_someEntitiesHaveNoFiles() {
        File file1 = createFile(1L, EntityType.NOTICE, 1L, 1);
        List<File> files = List.of(file1);
        List<Long> entityIds = List.of(1L, 2L, 3L);

        given(fileRepository.findByEntityTypeAndEntityIdIn(EntityType.NOTICE, entityIds))
                .willReturn(files);
        given(fileStorage.generateUrl("storage-key-1"))
                .willReturn("https://example.com/file1.jpg");

        Map<Long, List<FileResponse>> result = fileQueryService.findFilesByEntityIds(
                EntityType.NOTICE,
                entityIds
        );

        assertThat(result).hasSize(1);
        assertThat(result).containsKey(1L);
        assertThat(result).doesNotContainKey(2L);
        assertThat(result).doesNotContainKey(3L);
    }

    @DisplayName("엔티티 ID 목록이 비어있으면 빈 Map을 반환한다 - findFilesByEntityIds")
    @Test
    void findFilesByEntityIds_emptyList() {
        Map<Long, List<FileResponse>> result = fileQueryService.findFilesByEntityIds(
                EntityType.NOTICE,
                List.of()
        );

        assertThat(result).isEmpty();
    }

    @DisplayName("엔티티 ID 목록이 null이면 빈 Map을 반환한다 - findFilesByEntityIds")
    @Test
    void findFilesByEntityIds_nullList() {
        Map<Long, List<FileResponse>> result = fileQueryService.findFilesByEntityIds(
                EntityType.NOTICE,
                null
        );

        assertThat(result).isEmpty();
    }

    @DisplayName("모든 엔티티에 파일이 없으면 빈 Map을 반환한다")
    @Test
    void findFilesByEntityIds_noFiles() {
        List<Long> entityIds = List.of(1L, 2L, 3L);

        given(fileRepository.findByEntityTypeAndEntityIdIn(EntityType.NOTICE, entityIds))
                .willReturn(List.of());

        Map<Long, List<FileResponse>> result = fileQueryService.findFilesByEntityIds(
                EntityType.NOTICE,
                entityIds
        );

        assertThat(result).isEmpty();
    }

    private File createFile(Long id, EntityType entityType, Long entityId, Integer displayOrder) {
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
