package com.souzip.adapter.webapi.user;

import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.notice.dto.NoticeAuthorResponse;
import com.souzip.application.notice.dto.NoticeResponse;
import com.souzip.application.notice.provided.NoticeFinder;
import com.souzip.docs.RestDocsSupport;
import com.souzip.domain.notice.NoticeStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NoticeApiTest extends RestDocsSupport {

    private static final UUID TEST_ADMIN_ID =
            UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    private final NoticeFinder noticeFinder = mock(NoticeFinder.class);

    @Override
    protected Object initController() {
        return new NoticeApi(noticeFinder);
    }

    @DisplayName("활성화된 공지사항 목록을 조회할 수 있다")
    @Test
    void getAllActive() throws Exception {
        List<FileResponse> mockFiles = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 1)
        );

        NoticeAuthorResponse author = NoticeAuthorResponse.of(TEST_ADMIN_ID, "admin");

        List<NoticeResponse> mockResponses = List.of(
                new NoticeResponse(
                        1L, "공지사항 1", "내용 1", author, NoticeStatus.ACTIVE,
                        LocalDateTime.now(), LocalDateTime.now(), mockFiles
                ),
                new NoticeResponse(
                        2L, "공지사항 2", "내용 2", author, NoticeStatus.ACTIVE,
                        LocalDateTime.now(), LocalDateTime.now(), List.of()
                )
        );

        given(noticeFinder.findAllActiveWithFiles()).willReturn(mockResponses);

        mockMvc.perform(get("/api/notices"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data[1].status").value("ACTIVE"))
                .andExpect(jsonPath("$.data[0].author.authorId").value(TEST_ADMIN_ID.toString()))
                .andExpect(jsonPath("$.data[0].author.username").value("admin"))
                .andDo(document("notice/get-all-active",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.ARRAY).description("활성 공지사항 목록"),
                                fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("공지사항 ID"),
                                fieldWithPath("data[].title").type(JsonFieldType.STRING).description("제목"),
                                fieldWithPath("data[].content").type(JsonFieldType.STRING).description("내용"),

                                fieldWithPath("data[].author").type(JsonFieldType.OBJECT).description("작성자 정보"),
                                fieldWithPath("data[].author.authorId").type(JsonFieldType.STRING)
                                        .description("작성자 ID (UUID)"),
                                fieldWithPath("data[].author.username").type(JsonFieldType.STRING)
                                        .description("작성자 이름"),

                                fieldWithPath("data[].status").type(JsonFieldType.STRING).description("상태 (ACTIVE)"),
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
        List<FileResponse> mockFiles = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 1)
        );

        NoticeAuthorResponse author = NoticeAuthorResponse.of(TEST_ADMIN_ID, "admin");

        NoticeResponse mockResponse = new NoticeResponse(
                1L, "공지사항 제목", "공지사항 내용", author, NoticeStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(), mockFiles
        );

        given(noticeFinder.findActiveByIdWithFiles(anyLong())).willReturn(mockResponse);

        mockMvc.perform(get("/api/notices/{noticeId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("공지사항 제목"))
                .andExpect(jsonPath("$.data.content").value("공지사항 내용"))
                .andExpect(jsonPath("$.data.author.authorId").value(TEST_ADMIN_ID.toString()))
                .andExpect(jsonPath("$.data.author.username").value("admin"))
                .andDo(document("notice/get-by-id",
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
                                fieldWithPath("data.author.authorId").type(JsonFieldType.STRING)
                                        .description("작성자 ID (UUID)"),
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
}
