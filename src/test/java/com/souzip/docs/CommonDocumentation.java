package com.souzip.docs;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.snippet.Snippet;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;

public class CommonDocumentation {

    public static Snippet apiResponseFields(FieldDescriptor... allFields) {
        return responseFields(allFields);
    }

    public static FieldDescriptor[] paginationResponseFields(FieldDescriptor... contentFields) {
        FieldDescriptor[] baseFields = {
            fieldWithPath("data").type(JsonFieldType.OBJECT).description("응답 데이터"),
            fieldWithPath("data.content[]").type(JsonFieldType.ARRAY).description("데이터 목록"),

            fieldWithPath("data.pagination").type(JsonFieldType.OBJECT).description("페이지 정보"),
            fieldWithPath("data.pagination.currentPage").type(JsonFieldType.NUMBER).description("현재 페이지 번호 (1부터 시작)"),
            fieldWithPath("data.pagination.totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
            fieldWithPath("data.pagination.totalItems").type(JsonFieldType.NUMBER).description("전체 아이템 수"),
            fieldWithPath("data.pagination.pageSize").type(JsonFieldType.NUMBER).description("페이지당 아이템 수"),
            fieldWithPath("data.pagination.first").type(JsonFieldType.BOOLEAN).description("첫 페이지 여부"),
            fieldWithPath("data.pagination.last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
            fieldWithPath("data.pagination.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부"),
            fieldWithPath("data.pagination.hasPrevious").type(JsonFieldType.BOOLEAN).description("이전 페이지 존재 여부"),

            fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional()
        };

        FieldDescriptor[] allFields = new FieldDescriptor[baseFields.length + contentFields.length];
        System.arraycopy(baseFields, 0, allFields, 0, baseFields.length);
        System.arraycopy(contentFields, 0, allFields, baseFields.length, contentFields.length);

        return allFields;
    }

    public static FieldDescriptor[] errorResponseFields() {
        return new FieldDescriptor[]{
            fieldWithPath("traceId").type(JsonFieldType.STRING)
                .description("에러 추적 ID (운영 환경에서만 포함)")
                .optional(),
            fieldWithPath("message").type(JsonFieldType.STRING)
                .description("에러 메시지")
        };
    }

    public static FieldDescriptor[] validationErrorResponseFields() {
        return new FieldDescriptor[]{
            fieldWithPath("traceId").type(JsonFieldType.STRING)
                .description("에러 추적 ID (운영 환경에서만 포함)")
                .optional(),
            fieldWithPath("message").type(JsonFieldType.STRING)
                .description("에러 메시지"),
            fieldWithPath("errors").type(JsonFieldType.ARRAY)
                .description("필드별 에러 상세 정보"),
            fieldWithPath("errors[].field").type(JsonFieldType.STRING)
                .description("에러 필드명"),
            fieldWithPath("errors[].value").type(JsonFieldType.STRING)
                .description("입력된 값"),
            fieldWithPath("errors[].reason").type(JsonFieldType.STRING)
                .description("에러 사유")
        };
    }
}
