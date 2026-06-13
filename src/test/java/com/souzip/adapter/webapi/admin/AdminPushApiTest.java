package com.souzip.adapter.webapi.admin;

import com.souzip.adapter.webapi.admin.dto.PushBroadcastRequest;
import com.souzip.application.notification.FcmNotificationService;
import com.souzip.application.notification.PushBroadcastHistoryCommandService;
import com.souzip.application.notification.PushBroadcastHistoryQueryService;
import com.souzip.application.notification.dto.PushBroadcastHistoryResponse;
import com.souzip.application.notification.dto.PushBroadcastResult;
import com.souzip.docs.RestDocsSupport;
import com.souzip.shared.common.dto.pagination.PaginationResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

import static com.souzip.docs.ApiDocumentUtils.getDocumentRequest;
import static com.souzip.docs.ApiDocumentUtils.getDocumentResponse;
import static com.souzip.docs.CommonDocumentation.apiResponseFields;
import static com.souzip.docs.CommonDocumentation.paginationResponseFields;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminPushApiTest extends RestDocsSupport {

    private final FcmNotificationService fcmNotificationService = mock(FcmNotificationService.class);
    private final PushBroadcastHistoryCommandService pushBroadcastHistoryCommandService = mock(PushBroadcastHistoryCommandService.class);
    private final PushBroadcastHistoryQueryService pushBroadcastHistoryQueryService = mock(PushBroadcastHistoryQueryService.class);

    @Override
    protected Object initController() {
        return new AdminPushApi(fcmNotificationService, pushBroadcastHistoryCommandService, pushBroadcastHistoryQueryService);
    }

    @DisplayName("전체 기기에 푸시를 브로드캐스트할 수 있다")
    @Test
    void broadcast() throws Exception {
        // given
        PushBroadcastRequest request = new PushBroadcastRequest("공지 제목", "공지 내용");
        PushBroadcastResult result = new PushBroadcastResult(100, 95, 5, true);

        given(fcmNotificationService.broadcastToAllActiveTokens("공지 제목", "공지 내용")).willReturn(result);
        willDoNothing().given(pushBroadcastHistoryCommandService)
                .record(eq(TEST_ADMIN_ID), eq("공지 제목"), eq("공지 내용"), any(PushBroadcastResult.class));

        // when & then
        mockMvc.perform(post("/api/admin/push/broadcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTargets").value(100))
                .andExpect(jsonPath("$.data.successCount").value(95))
                .andExpect(jsonPath("$.data.failCount").value(5))
                .andExpect(jsonPath("$.data.firebaseConfigured").value(true))
                .andDo(document("admin/push/broadcast",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("title").type(JsonFieldType.STRING).description("푸시 알림 제목 (최대 200자)"),
                                fieldWithPath("body").type(JsonFieldType.STRING).description("푸시 알림 내용 (최대 1000자)")
                        ),
                        apiResponseFields(
                                fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
                                fieldWithPath("data.totalTargets").type(JsonFieldType.NUMBER).description("전체 대상 기기 수"),
                                fieldWithPath("data.successCount").type(JsonFieldType.NUMBER).description("전송 성공 수"),
                                fieldWithPath("data.failCount").type(JsonFieldType.NUMBER).description("전송 실패 수"),
                                fieldWithPath("data.firebaseConfigured").type(JsonFieldType.BOOLEAN).description("Firebase 설정 여부"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지")
                        )
                ));
    }

    @DisplayName("Firebase 미설정 시 브로드캐스트 결과에 firebaseConfigured=false가 반환된다")
    @Test
    void broadcast_firebaseNotConfigured() throws Exception {
        // given
        PushBroadcastRequest request = new PushBroadcastRequest("공지 제목", "공지 내용");
        PushBroadcastResult result = new PushBroadcastResult(50, 0, 0, false);

        given(fcmNotificationService.broadcastToAllActiveTokens("공지 제목", "공지 내용")).willReturn(result);
        willDoNothing().given(pushBroadcastHistoryCommandService)
                .record(eq(TEST_ADMIN_ID), eq("공지 제목"), eq("공지 내용"), any(PushBroadcastResult.class));

        // when & then
        mockMvc.perform(post("/api/admin/push/broadcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firebaseConfigured").value(false))
                .andExpect(jsonPath("$.message").value("Firebase가 설정되지 않아 전송하지 않았습니다. (대상 기기 50건)"));
    }

    @DisplayName("활성 기기가 없으면 브로드캐스트 결과에 전송 없음 메시지가 반환된다")
    @Test
    void broadcast_noActiveDevices() throws Exception {
        // given
        PushBroadcastRequest request = new PushBroadcastRequest("공지 제목", "공지 내용");
        PushBroadcastResult result = new PushBroadcastResult(0, 0, 0, true);

        given(fcmNotificationService.broadcastToAllActiveTokens("공지 제목", "공지 내용")).willReturn(result);
        willDoNothing().given(pushBroadcastHistoryCommandService)
                .record(eq(TEST_ADMIN_ID), eq("공지 제목"), eq("공지 내용"), any(PushBroadcastResult.class));

        // when & then
        mockMvc.perform(post("/api/admin/push/broadcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("등록된 활성 기기가 없어 전송하지 않았습니다."));
    }

    @DisplayName("제목 없이 브로드캐스트하면 400 에러가 반환된다")
    @Test
    void broadcast_missingTitle_returnsBadRequest() throws Exception {
        // given
        String requestBody = "{\"body\": \"내용만 있음\"}";

        // when & then
        mockMvc.perform(post("/api/admin/push/broadcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("브로드캐스트 이력을 페이지 조회할 수 있다")
    @Test
    void broadcastHistory() throws Exception {
        // given
        PushBroadcastHistoryResponse item = new PushBroadcastHistoryResponse(
                1L,
                TEST_ADMIN_ID,
                "공지 제목",
                "공지 내용",
                100, 95, 5,
                true,
                LocalDateTime.of(2026, 6, 13, 12, 0, 0)
        );

        PaginationResponse<PushBroadcastHistoryResponse> page = PaginationResponse.of(
                List.of(item), 1, 20, 1L, 1
        );

        given(pushBroadcastHistoryQueryService.findPage(any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/admin/push/broadcast/history")
                        .param("page", "1")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("공지 제목"))
                .andExpect(jsonPath("$.data.content[0].totalTargets").value(100))
                .andDo(document("admin/push/broadcast-history",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        queryParameters(
                                parameterWithName("page").description("페이지 번호 (1부터 시작)"),
                                parameterWithName("size").description("페이지 크기")
                        ),
                        apiResponseFields(
                                paginationResponseFields(
                                        fieldWithPath("data.content[].id").type(JsonFieldType.NUMBER).description("이력 ID"),
                                        fieldWithPath("data.content[].adminId").type(JsonFieldType.STRING).description("발송한 관리자 ID"),
                                        fieldWithPath("data.content[].title").type(JsonFieldType.STRING).description("푸시 제목"),
                                        fieldWithPath("data.content[].body").type(JsonFieldType.STRING).description("푸시 내용"),
                                        fieldWithPath("data.content[].totalTargets").type(JsonFieldType.NUMBER).description("전체 대상 기기 수"),
                                        fieldWithPath("data.content[].successCount").type(JsonFieldType.NUMBER).description("전송 성공 수"),
                                        fieldWithPath("data.content[].failCount").type(JsonFieldType.NUMBER).description("전송 실패 수"),
                                        fieldWithPath("data.content[].firebaseConfigured").type(JsonFieldType.BOOLEAN).description("Firebase 설정 여부"),
                                        fieldWithPath("data.content[].createdAt").type(JsonFieldType.STRING).description("발송 일시")
                                )
                        )
                ));
    }

    @DisplayName("브로드캐스트 이력이 없으면 빈 목록이 반환된다")
    @Test
    void broadcastHistory_empty() throws Exception {
        // given
        PaginationResponse<PushBroadcastHistoryResponse> emptyPage = PaginationResponse.of(
                List.of(), 1, 20, 0L, 0
        );

        given(pushBroadcastHistoryQueryService.findPage(any())).willReturn(emptyPage);

        // when & then
        mockMvc.perform(get("/api/admin/push/broadcast/history")
                        .param("page", "1")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }
}
