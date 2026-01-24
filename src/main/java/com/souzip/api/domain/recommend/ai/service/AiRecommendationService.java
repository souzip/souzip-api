package com.souzip.api.domain.recommend.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.file.dto.FileResponse;
import com.souzip.api.domain.file.service.FileService;
import com.souzip.api.domain.recommend.ai.dto.AiRecommendationResponse;
import com.souzip.api.domain.recommend.ai.repository.AiRecommendationRepositoryCustom;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import com.souzip.api.global.clova.ClovaStudioClient;
import com.souzip.api.global.clova.PromptLoader;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendationService {

    private final JdbcTemplate jdbcTemplate;
    private final AiRecommendationRepositoryCustom aiRecommendationRepository;
    private final ClovaStudioClient clovaStudioClient;
    private final PromptLoader promptLoader;
    private final ObjectMapper objectMapper;
    private final FileService fileService;

    public AiRecommendationResponse getCategoryRecommendationsForUser(Long userId) {
        log.info("getCategoryRecommendationsForUser 시작, userId={}", userId);

        List<String> categoryNames = jdbcTemplate.queryForList(
                "SELECT category FROM user_category WHERE user_id = ?",
                String.class,
                userId
        );
        log.info("사용자 카테고리 조회: {}", categoryNames);

        List<Category> preferredCategories = categoryNames.stream()
                .map(Category::valueOf)
                .toList();
        log.info("Category enum 변환: {}", preferredCategories);

        Map<String, List<Souvenir>> souvenirsByCategory = loadSouvenirsByPreferredCategory(preferredCategories);
        log.info("선호 카테고리별 souvenirs 개수: {}",
                souvenirsByCategory.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().size()))
        );

        String prompt = buildPromptForCategories(souvenirsByCategory);
        log.info("Clova Prompt: {}", prompt);

        String clovaResponse = callClova(prompt);
        log.info("Clova Response: {}", clovaResponse);

        Map<String, List<String>> recommendedNamesByCategory = parseClovaResponse(clovaResponse);
        log.info("추천 이름 파싱: {}", recommendedNamesByCategory);

        return new AiRecommendationResponse(
                mapToRecommendedSouvenirs(recommendedNamesByCategory)
        );
    }

    public AiRecommendationResponse getRecentSouvenirRecommendations(Long userId) {
        log.info("getRecentSouvenirRecommendations 시작, userId={}", userId);

        var latestOpt = aiRecommendationRepository.findLatestByUserId(userId);
        log.info("사용자 최근 souvenir 조회: present={}", latestOpt.isPresent());

        if (latestOpt.isEmpty()) {
            return new AiRecommendationResponse(Collections.emptyList());
        }
        var latest = latestOpt.get();
        log.info("최근 souvenir: id={}, name={}", latest.getId(), latest.getName());

        String countryCode = latest.getCountryCode();
        List<String> recentNames = List.of(latest.getName());

        List<Souvenir> candidateSouvenirs = aiRecommendationRepository.findAllByCountryCode(countryCode);
        log.info("국가({})에 해당하는 후보 souvenirs 개수: {}", countryCode, candidateSouvenirs.size());

        if (candidateSouvenirs.isEmpty()) {
            return new AiRecommendationResponse(Collections.emptyList());
        }

        List<String> categoryNames = jdbcTemplate.queryForList(
                "SELECT category FROM user_category WHERE user_id = ?",
                String.class,
                userId
        );
        log.info("사용자 카테고리 조회: {}", categoryNames);

        String prompt = buildPromptForRecentSouvenir(candidateSouvenirs, recentNames, categoryNames, countryCode);
        log.info("Clova Prompt: {}", prompt);

        String clovaResponse = callClova(prompt);
        log.info("Clova Response: {}", clovaResponse);

        Map<String, List<String>> recommendedNamesByCategory = parseClovaResponse(clovaResponse);
        log.info("추천 이름 파싱: {}", recommendedNamesByCategory);

        List<AiRecommendationResponse.RecommendedSouvenir> finalSouvenirs =
                recommendedNamesByCategory.values().stream()
                        .flatMap(List::stream)
                        .map(aiRecommendationRepository::findByName)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(s -> AiRecommendationResponse.RecommendedSouvenir.from(
                                s.getId(),
                                s.getName(),
                                s.getCategory().name(),
                                s.getCountryCode(),
                                getThumbnailUrl(s.getId())
                        ))
                        .collect(Collectors.toList());

        log.info("최종 추천 souvenirs 개수: {}", finalSouvenirs.size());
        return new AiRecommendationResponse(finalSouvenirs);
    }

    private Map<String, List<Souvenir>> loadSouvenirsByPreferredCategory(List<Category> preferredCategories) {
        return preferredCategories.stream()
                .collect(Collectors.toMap(
                        Category::name,
                        aiRecommendationRepository::findAllByCategory
                ));
    }

    private String buildPromptForCategories(Map<String, List<Souvenir>> souvenirsByCategory) {
        StringBuilder sb = new StringBuilder();
        souvenirsByCategory.forEach((categoryName, list) -> {
            sb.append(categoryName).append(":\n");
            list.forEach(s -> sb.append(" - ").append(s.getName()).append("\n"));
        });
        return promptLoader.loadPrompt("souvenir-recommendation-category.txt")
                .replace("{souvenirList}", sb.toString());
    }

    private String buildPromptForRecentSouvenir(List<Souvenir> candidateSouvenirs,
                               List<String> recentNames,
                               List<String> userCategories,
                               String countryCode) {
        StringBuilder sb = new StringBuilder();
        candidateSouvenirs.forEach(s -> sb.append(" - ").append(s.getName()).append("\n"));

        return promptLoader.loadPrompt("souvenir-recommendation-upload.txt")
                .replace("{souvenirList}", sb.toString())
                .replace("{recentSouvenirNames}", recentNames.toString())
                .replace("{userCategories}", userCategories.toString())
                .replace("{countryCode}", countryCode);
    }

    private String callClova(String prompt) {
        String response = clovaStudioClient.chatAsCurator(prompt);
        response = response.replaceAll("(?s)```json|```", "");
        log.info("Clova response: {}", response);
        return response;
    }

    private Map<String, List<String>> parseClovaResponse(String clovaResponse) {
        try {
            return objectMapper.readValue(
                            clovaResponse,
                            new TypeReference<Map<String, List<Map<String, String>>>>() {}
                    ).entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().stream()
                                    .map(m -> m.get("name"))
                                    .collect(Collectors.toList())
                    ));
        } catch (Exception e) {
            throw new RuntimeException("클로바 응답 JSON 파싱 실패", e);
        }
    }

    private List<AiRecommendationResponse.RecommendedSouvenir> mapToRecommendedSouvenirs(
        Map<String, List<String>> recommendedNamesByCategory
    ) {
        List<Souvenir> souvenirs = recommendedNamesByCategory.values().stream()
            .flatMap(List::stream)
            .map(aiRecommendationRepository::findByName)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

        List<Long> souvenirIds = souvenirs.stream()
            .map(Souvenir::getId)
            .toList();

        Map<Long, FileResponse> thumbnailMap = fileService
            .getThumbnailsByEntityIds("Souvenir", souvenirIds);

        return souvenirs.stream()
            .map(s -> AiRecommendationResponse.RecommendedSouvenir.from(
                s.getId(),
                s.getName(),
                s.getCategory().name(),
                s.getCountryCode(),
                Optional.ofNullable(thumbnailMap.get(s.getId()))
                    .map(FileResponse::url)
                    .orElse(null)
            ))
            .collect(Collectors.toList());
    }

    private String getThumbnailUrl(Long souvenirId) {
        return fileService.getFirstFile("Souvenir", souvenirId).url();
    }
}
