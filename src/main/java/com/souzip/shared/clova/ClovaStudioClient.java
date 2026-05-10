package com.souzip.shared.clova;

import com.souzip.shared.config.ClovaStudioProperties;
import com.souzip.shared.exception.BusinessException;
import com.souzip.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClovaStudioClient {

    private static final String CURATOR_SYSTEM_PROMPT = "당신은 여행 기념품 전문 큐레이터입니다.";
    private static final double TOP_P = 0.8;
    private static final int TOP_K = 0;
    private static final int MAX_TOKENS = 2000;
    private static final double TEMPERATURE = 0.5;
    private static final double REPEAT_PENALTY = 1.1;

    private final ClovaStudioProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    public String chatAsCurator(String userMessage) {
        return chat(userMessage);
    }

    private String chat(String userMessage) {
        try {
            HttpEntity<Map<String, Object>> request = createRequest(userMessage);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    properties.getApiUrl(),
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }
            );

            return parseResponse(response.getBody());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private HttpEntity<Map<String, Object>> createRequest(String userMessage) {
        HttpHeaders headers = createHeaders();
        Map<String, Object> body = createRequestBody(userMessage);
        return new HttpEntity<>(body, headers);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + properties.getApiKey());
        headers.set("X-NCP-CLOVASTUDIO-REQUEST-ID", UUID.randomUUID().toString());
        return headers;
    }

    private Map<String, Object> createRequestBody(String userMessage) {
        return Map.of(
                "messages", createMessages(userMessage),
                "topP", TOP_P,
                "topK", TOP_K,
                "maxTokens", MAX_TOKENS,
                "temperature", TEMPERATURE,
                "repeatPenalty", REPEAT_PENALTY,
                "includeAiFilters", true
        );
    }

    private List<Map<String, Object>> createMessages(String userMessage) {
        return List.of(
                createMessage("system", CURATOR_SYSTEM_PROMPT),
                createMessage("user", userMessage)
        );
    }

    private Map<String, Object> createMessage(String role, String text) {
        return Map.of(
                "role", role,
                "content", List.of(Map.of("type", "text", "text", text))
        );
    }

    private String parseResponse(Map<String, Object> responseBody) {
        Map<String, Object> result = (Map<String, Object>) responseBody.get("result");
        Map<String, Object> message = (Map<String, Object>) result.get("message");
        Object content = message.get("content");

        if (content instanceof List) {
            return parseListContent((List<Map<String, Object>>) content);
        }

        if (content instanceof String) {
            return (String) content;
        }

        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String parseListContent(List<Map<String, Object>> contentList) {
        return contentList.stream()
                .map(item -> (String) item.get("text"))
                .reduce("", String::concat);
    }
}
