package com.souzip.adapter.webapi.admin;

import com.souzip.adapter.webapi.admin.dto.NoticeRequest;
import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.notice.dto.NoticeAuthorResponse;
import com.souzip.application.notice.dto.NoticeResponse;
import com.souzip.application.notice.provided.NoticeFinder;
import com.souzip.application.notice.provided.NoticeRegister;
import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.admin.model.AdminRole;
import com.souzip.domain.notice.Notice;
import com.souzip.domain.notice.NoticeRegisterRequest;
import com.souzip.domain.notice.NoticeStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.util.ReflectionTestUtils;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminNoticeApiTest extends RestDocsSupport {

    private static final UUID TEST_ADMIN_ID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private final NoticeFinder noticeFinder = mock(NoticeFinder.class);
    private final NoticeRegister noticeRegister = mock(NoticeRegister.class);

    @Override
    protected Object initController() {
        return new AdminNoticeApi(noticeRegister, noticeFinder);
    }

    @DisplayName("공지사항을 등록할 수 있다")
    @Test
    void register() throws Exception {
        // given
        Notice mockNotice = createNotice("공지사항 제목", "공지사항 내용", NoticeStatus.ACTIVE);

        NoticeRequest requestDto = new NoticeRequest(
                "공지사항 제목",
                "공지사항 내용",
                NoticeStatus.ACTIVE
        );

        MockMultipartFile noticePart = new MockMultipartFile(
                "notice",
                "notice.json",
                "application/json",
                objectMapper.writeValueAsBytes(requestDto)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "files",
                "test.jpg",
                "image/jpeg",
                "test image".getBytes()
        );

        List<FileResponse> mockFiles = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "test.jpg", 1)
        );

        NoticeAuthorResponse author = NoticeAuthorResponse.of(TEST_ADMIN_ID, "admin");

        NoticeResponse mockResponse = new NoticeResponse(
                1L,
                "공지사항 제목",
                "공지사항 내용",
                author,
                NoticeStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                mockFiles
        );

        given(noticeRegister.register(any(NoticeRegisterRequest.class), anyList()))
                .willReturn(mockNotice);
        given(noticeFinder.findByIdWithFiles(eq(1L)))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(multipart("/api/admin/notices")
                        .file(noticePart)
                        .file(filePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("공지사항 제목"))
                .andExpect(jsonPath("$.data.content").value("공지사항 내용"))
                .andExpect(jsonPath("$.data.author.authorId").value(TEST_ADMIN_ID.toString()))
                .andExpect(jsonPath("$.data.author.username").value("admin"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.message").value("공지사항이 등록되었습니다."))
                .andDo(document("admin/notice/register",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParts(
                                partWithName("notice").description("공지사항 정보(JSON) (필수)"),
                                partWithName("files").description("첨부 파일").optional()
                        ),
                        requestPartFields("notice",
                                fieldWithPath("title").description("공지사항 제목"),
                                fieldWithPath("content").description("공지사항 내용"),
                                fieldWithPath("status").description("공지사항 상태 (ACTIVE, INACTIVE)")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("공지사항 ID"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("내용"),

                                fieldWithPath("data.author").type(JsonFieldType.OBJECT).description("작성자 정보"),
                                fieldWithPath("data.author.authorId").type(JsonFieldType.STRING).description("작성자 ID (UUID)"),
                                fieldWithPath("data.author.username").type(JsonFieldType.STRING).description("작성자 이름"),

                                fieldWithPath("data.status").type(JsonFieldType.STRING).description("상태 (ACTIVE/INACTIVE)"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시"),
                                fieldWithPath("data.files").type(JsonFieldType.ARRAY).description("첨부 파일 목록"),
                                fieldWithPath("data.files[].id").type(JsonFieldType.NUMBER).description("파일 ID"),
                                fieldWithPath("data.files[].url").type(JsonFieldType.STRING).description("파일 URL"),
                                fieldWithPath("data.files[].originalName").type(JsonFieldType.STRING).description("원본 파일명"),
                                fieldWithPath("data.files[].displayOrder").type(JsonFieldType.NUMBER).description("파일 순서"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    @DisplayName("공지사항을 수정할 수 있다")
    @Test
    void update() throws Exception {
        // given
        Notice mockNotice = createNotice("수정된 제목", "수정된 내용", NoticeStatus.INACTIVE);

        NoticeRequest requestDto = new NoticeRequest(
                "수정된 제목",
                "수정된 내용",
                NoticeStatus.INACTIVE
        );

        MockMultipartFile noticePart = new MockMultipartFile(
                "notice",
                "notice.json",
                "application/json",
                objectMapper.writeValueAsBytes(requestDto)
        );

        MockMultipartFile deleteFileIdsPart = new MockMultipartFile(
                "deleteFileIds",
                "deleteFileIds.json",
                "application/json",
                objectMapper.writeValueAsBytes(List.of(10L, 20L))
        );

        MockMultipartFile newFilePart = new MockMultipartFile(
                "newFiles",
                "new.jpg",
                "image/jpeg",
                "new image".getBytes()
        );

        List<FileResponse> mockFiles = List.of(
                new FileResponse(3L, "https://example.com/new.jpg", "new.jpg", 1)
        );

        NoticeAuthorResponse author = NoticeAuthorResponse.of(TEST_ADMIN_ID, "admin");

        NoticeResponse mockResponse = new NoticeResponse(
                1L,
                "수정된 제목",
                "수정된 내용",
                author,
                NoticeStatus.INACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                mockFiles
        );

        given(noticeRegister.update(anyLong(), any(), anyList(), anyList()))
                .willReturn(mockNotice);
        given(noticeFinder.findByIdWithFiles(eq(1L)))
                .willReturn(mockResponse);

        // when & then
        mockMvc.perform(multipart("/api/admin/notices/{noticeId}", 1L)
                        .file(noticePart)
                        .file(deleteFileIdsPart)
                        .file(newFilePart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.content").value("수정된 내용"))
                .andExpect(jsonPath("$.data.author.authorId").value(TEST_ADMIN_ID.toString()))
                .andExpect(jsonPath("$.data.author.username").value("admin"))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andExpect(jsonPath("$.message").value("공지사항이 수정되었습니다."))
                .andDo(document("admin/notice/update",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("noticeId").description("공지사항 ID")
                        ),
                        requestParts(
                                partWithName("notice").description("공지사항 정보(JSON) (필수)"),
                                partWithName("deleteFileIds").description("삭제할 파일 ID 목록(JSON array)").optional(),
                                partWithName("newFiles").description("새로 추가할 파일").optional()
                        ),
                        requestPartFields("notice",
                                fieldWithPath("title").description("공지사항 제목"),
                                fieldWithPath("content").description("공지사항 내용"),
                                fieldWithPath("status").description("공지사항 상태 (ACTIVE, INACTIVE)")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("공지사항 ID"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("내용"),

                                fieldWithPath("data.author").type(JsonFieldType.OBJECT).description("작성자 정보"),
                                fieldWithPath("data.author.authorId").type(JsonFieldType.STRING).description("작성자 ID (UUID)"),
                                fieldWithPath("data.author.username").type(JsonFieldType.STRING).description("작성자 이름"),

                                fieldWithPath("data.status").type(JsonFieldType.STRING).description("상태 (ACTIVE/INACTIVE)"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시"),
                                fieldWithPath("data.files").type(JsonFieldType.ARRAY).description("첨부 파일 목록"),
                                fieldWithPath("data.files[].id").type(JsonFieldType.NUMBER).description("파일 ID"),
                                fieldWithPath("data.files[].url").type(JsonFieldType.STRING).description("파일 URL"),
                                fieldWithPath("data.files[].originalName").type(JsonFieldType.STRING).description("원본 파일명"),
                                fieldWithPath("data.files[].displayOrder").type(JsonFieldType.NUMBER).description("파일 순서"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    @DisplayName("공지사항을 활성화할 수 있다")
    @Test
    void activate() throws Exception {
        // given
        willDoNothing().given(noticeRegister).activate(anyLong());

        // when & then
        mockMvc.perform(patch("/api/admin/notices/{noticeId}/activate", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항이 활성화되었습니다."))
                .andDo(document("admin/notice/activate",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("noticeId").description("공지사항 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    @DisplayName("공지사항을 비활성화할 수 있다")
    @Test
    void deactivate() throws Exception {
        // given
        willDoNothing().given(noticeRegister).deactivate(anyLong());

        // when & then
        mockMvc.perform(patch("/api/admin/notices/{noticeId}/deactivate", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항이 비활성화되었습니다."))
                .andDo(document("admin/notice/deactivate",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("noticeId").description("공지사항 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    @DisplayName("공지사항을 삭제할 수 있다")
    @Test
    void deleteNotice() throws Exception {
        // given
        willDoNothing().given(noticeRegister).delete(anyLong());

        // when & then
        mockMvc.perform(delete("/api/admin/notices/{noticeId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("공지사항이 삭제되었습니다."))
                .andDo(document("admin/notice/delete",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("noticeId").description("공지사항 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    @DisplayName("전체 공지사항 목록을 조회할 수 있다")
    @Test
    void getAll() throws Exception {
        // given
        List<FileResponse> mockFiles = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 1),
                new FileResponse(2L, "https://example.com/file2.jpg", "file2.jpg", 2)
        );

        NoticeAuthorResponse author1 = NoticeAuthorResponse.of(TEST_ADMIN_ID, "admin");
        NoticeAuthorResponse author2 = NoticeAuthorResponse.of(TEST_ADMIN_ID, "admin");

        List<NoticeResponse> mockResponses = List.of(
                new NoticeResponse(1L, "공지사항 1", "내용 1", author1, NoticeStatus.ACTIVE,
                        LocalDateTime.now(), LocalDateTime.now(), mockFiles),
                new NoticeResponse(2L, "공지사항 2", "내용 2", author2, NoticeStatus.INACTIVE,
                        LocalDateTime.now(), LocalDateTime.now(), List.of())
        );

        given(noticeFinder.findAllWithFiles()).willReturn(mockResponses);

        // when & then
        mockMvc.perform(get("/api/admin/notices"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("공지사항 1"))
                .andExpect(jsonPath("$.data[0].author.authorId").value(TEST_ADMIN_ID.toString()))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].author.authorId").value(TEST_ADMIN_ID.toString()))
                .andDo(document("admin/notice/get-all",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("공지사항 목록"),
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("공지사항 ID"),
                                fieldWithPath("data[].title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("data[].content").type(JsonFieldType.STRING).description("내용"),

                                fieldWithPath("data[].author").type(JsonFieldType.OBJECT).description("작성자 정보"),
                                fieldWithPath("data[].author.authorId").type(JsonFieldType.STRING).description("작성자 ID (UUID)"),
                                fieldWithPath("data[].author.username").type(JsonFieldType.STRING).description("작성자 이름"),

                                fieldWithPath("data[].status").type(JsonFieldType.STRING).description("상태 (ACTIVE/INACTIVE)"),
                                fieldWithPath("data[].createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data[].updatedAt").type(JsonFieldType.STRING).description("수정일시"),
                                fieldWithPath("data[].files").type(JsonFieldType.ARRAY).description("첨부 파일 목록"),
                                fieldWithPath("data[].files[].id").type(JsonFieldType.NUMBER).description("파일 ID"),
                                fieldWithPath("data[].files[].url").type(JsonFieldType.STRING).description("파일 URL"),
                                fieldWithPath("data[].files[].originalName").type(JsonFieldType.STRING).description("원본 파일명"),
                                fieldWithPath("data[].files[].displayOrder").type(JsonFieldType.NUMBER).description("파일 순서"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    @DisplayName("공지사항 상세 정보를 조회할 수 있다")
    @Test
    void getById() throws Exception {
        // given
        List<FileResponse> mockFiles = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 1)
        );

        NoticeAuthorResponse author = NoticeAuthorResponse.of(TEST_ADMIN_ID, "admin");

        NoticeResponse mockResponse = new NoticeResponse(
                1L,
                "공지사항 제목",
                "공지사항 내용",
                author,
                NoticeStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now(),
                mockFiles
        );

        given(noticeFinder.findByIdWithFiles(anyLong())).willReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/admin/notices/{noticeId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("공지사항 제목"))
                .andExpect(jsonPath("$.data.content").value("공지사항 내용"))
                .andExpect(jsonPath("$.data.author.authorId").value(TEST_ADMIN_ID.toString()))
                .andExpect(jsonPath("$.data.author.username").value("admin"))
                .andDo(document("admin/notice/get-by-id",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        pathParameters(
                                parameterWithName("noticeId").description("공지사항 ID")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("공지사항 ID"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("data.content").type(JsonFieldType.STRING).description("내용"),

                                fieldWithPath("data.author").type(JsonFieldType.OBJECT).description("작성자 정보"),
                                fieldWithPath("data.author.authorId").type(JsonFieldType.STRING).description("작성자 ID (UUID)"),
                                fieldWithPath("data.author.username").type(JsonFieldType.STRING).description("작성자 이름"),

                                fieldWithPath("data.status").type(JsonFieldType.STRING).description("상태 (ACTIVE/INACTIVE)"),
                                fieldWithPath("data.createdAt").type(JsonFieldType.STRING).description("생성일시"),
                                fieldWithPath("data.updatedAt").type(JsonFieldType.STRING).description("수정일시"),
                                fieldWithPath("data.files").type(JsonFieldType.ARRAY).description("첨부 파일 목록"),
                                fieldWithPath("data.files[].id").type(JsonFieldType.NUMBER).description("파일 ID"),
                                fieldWithPath("data.files[].url").type(JsonFieldType.STRING).description("파일 URL"),
                                fieldWithPath("data.files[].originalName").type(JsonFieldType.STRING).description("원본 파일명"),
                                fieldWithPath("data.files[].displayOrder").type(JsonFieldType.NUMBER).description("파일 순서"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
                        )
                ));
    }

    private Notice createNotice(String title, String content, NoticeStatus status) {
        NoticeRegisterRequest request = NoticeRegisterRequest.of(
                title,
                content,
                TEST_ADMIN_ID,
                status
        );
        Notice notice = Notice.register(request);
        ReflectionTestUtils.setField(notice, "id", 1L);
        ReflectionTestUtils.setField(notice, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(notice, "updatedAt", LocalDateTime.now());
        return notice;
    }
}
