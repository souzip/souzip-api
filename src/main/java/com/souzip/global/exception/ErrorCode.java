package com.souzip.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    HTTP_REQUEST_CONTEXT_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "HTTP 요청 컨텍스트를 찾을 수 없습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    ONBOARDING_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "이미 온보딩을 완료한 사용자입니다."),
    INVALID_PROFILE_IMAGE_COLOR(HttpStatus.BAD_REQUEST, "유효하지 않은 프로필 이미지 색상입니다."),
    NICKNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),

    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 Refresh Token입니다."),
    INVALID_TOKEN_FORMAT(HttpStatus.BAD_REQUEST, "토큰 형식이 올바르지 않습니다."),
    INVALID_JWT_STRUCTURE(HttpStatus.BAD_REQUEST, "JWT 구조가 올바르지 않습니다."),
    TOKEN_DECODE_FAILED(HttpStatus.BAD_REQUEST, "토큰 디코딩에 실패했습니다."),
    TOKEN_PARSE_FAILED(HttpStatus.BAD_REQUEST, "토큰 파싱에 실패했습니다."),

    KAKAO_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 API 호출에 실패했습니다."),
    GOOGLE_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "구글 API 호출에 실패했습니다."),
    APPLE_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "애플 API 호출에 실패했습니다."),

    COUNTRY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 국가 정보를 찾을 수 없습니다."),
    COUNTRY_REGION_INVALID(HttpStatus.BAD_REQUEST, "국가의 지역 코드가 유효하지 않습니다."),

    CITY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 도시 정보를 찾을 수 없습니다."),

    CURRENCY_NOT_FOUND(HttpStatus.BAD_REQUEST, "통화를 찾을 수 없습니다."),

    EXCHANGE_RATE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 환율 정보를 찾을 수 없습니다."),

    SOUVENIR_NOT_FOUND(HttpStatus.NOT_FOUND, "기념품을 찾을 수 없습니다."),
    INVALID_SOUVENIR_LOCATION(HttpStatus.BAD_REQUEST, "기념품의 위치 정보가 유효하지 않습니다."),

    WISHLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "찜 목록에 없는 기념품입니다."),
    ALREADY_WISHLISTED(HttpStatus.BAD_REQUEST, "이미 찜한 기념품입니다."),

    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 50MB를 초과합니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "허용되지 않은 파일 형식입니다."),
    FILE_DELETE_FAILED(HttpStatus.BAD_REQUEST, "파일 삭제에 실패했습니다."),
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "파일이 비어있습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),

    INVALID_CATEGORY(HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리입니다."),

    SEARCH_INDEX_NOT_READY(HttpStatus.SERVICE_UNAVAILABLE, "검색 인덱스가 준비되지 않았습니다."),
    SEARCH_INDEX_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "검색 인덱스 생성에 실패했습니다."),
    SEARCH_INDEX_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "검색 인덱스 삭제에 실패했습니다."),
    SEARCH_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "검색 데이터 저장에 실패했습니다."),
    SEARCH_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "검색 서비스에 오류가 발생했습니다."),

    AI_RECOMMENDATION_NOT_READY(HttpStatus.BAD_REQUEST, "추천 시스템을 위해 기념품 업로드 이력이 있어야 합니다."),

    APPLE_MIGRATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Apple 마이그레이션 준비 중 오류가 발생했습니다."),
    APPLE_TRANSFER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "App Transfer가 아직 완료되지 않았습니다."),
    NO_APPLE_USERS_FOUND(HttpStatus.NOT_FOUND, "마이그레이션할 Apple 사용자가 없습니다."),

    FCM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 푸시 전송에 실패했습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
