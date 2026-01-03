package com.souzip.api.domain.recommend.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.souzip.api.domain.category.entity.Category;
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
        List<String> categoryNames = jdbcTemplate.queryForList(
                "SELECT category FROM user_category WHERE user_id = ?",
                String.class,
                userId
        );
        List<Category> preferredCategories = categoryNames.stream()
                .map(Category::valueOf)
                .toList();

        Map<String, List<Souvenir>> souvenirsByCategory = loadSouvenirsByPreferredCategory(preferredCategories);

        String prompt = buildPromptForCategories(souvenirsByCategory);
        String clovaResponse = callClova(prompt);

        Map<String, List<String>> recommendedNamesByCategory = parseClovaResponse(clovaResponse);

        return new AiRecommendationResponse(
                mapToRecommendedSouvenirs(recommendedNamesByCategory)
        );
    }

    public AiRecommendationResponse getRecentSouvenirRecommendations(Long userId) {
        var latestOpt = aiRecommendationRepository.findLatestByUserId(userId);
        if (latestOpt.isEmpty()) {
            return new AiRecommendationResponse(Collections.emptyList());
        }
        var latest = latestOpt.get();

        String countryCode = latest.getCountryCode();
        List<String> recentNames = List.of(latest.getName());

        List<Souvenir> candidateSouvenirs = aiRecommendationRepository.findAllByCountryCode(countryCode);
        if (candidateSouvenirs.isEmpty()) {
            return new AiRecommendationResponse(Collections.emptyList());
        }

        List<String> categoryNames = jdbcTemplate.queryForList(
                "SELECT category FROM user_category WHERE user_id = ?",
                String.class,
                userId
        );

        String prompt = buildPromptForRecentSouvenir(candidateSouvenirs, recentNames, categoryNames, countryCode);

        String clovaResponse = callClova(prompt);

        Map<String, List<String>> recommendedNamesByCategory = parseClovaResponse(clovaResponse);

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
            Map<String, List<String>> recommendedNames
    ) {
        return recommendedNames.values().stream()
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
    }

    private String getThumbnailUrl(Long souvenirId) {
        return fileService.getFirstFile("Souvenir", souvenirId).url();
    }
}
