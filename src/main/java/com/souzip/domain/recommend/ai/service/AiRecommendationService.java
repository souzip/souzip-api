package com.souzip.domain.recommend.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.souzip.application.file.FileQueryService;
import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.file.required.FileStorage;
import com.souzip.domain.category.entity.Category;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.file.File;
import com.souzip.domain.recommend.ai.dto.AiRecommendationResponse;
import com.souzip.domain.recommend.ai.repository.AiRecommendationRepositoryCustom;
import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.wishlist.repository.WishlistRepository;
import com.souzip.global.clova.ClovaStudioClient;
import com.souzip.global.clova.PromptLoader;
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
    private final FileQueryService fileQueryService;
    private final FileStorage fileStorage;
    private final WishlistRepository wishlistRepository;

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
                mapToRecommendedSouvenirs(recommendedNamesByCategory, userId)
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

        List<Souvenir> souvenirs = recommendedNamesByCategory.values().stream()
                .flatMap(List::stream)
                .map(aiRecommendationRepository::findByName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        List<Long> souvenirIds = souvenirs.stream().map(Souvenir::getId).toList();
        Set<Long> wishlistedIds = wishlistRepository.findSouvenirIdsByUserId(userId);
        Map<Long, Long> wishlistCountMap = wishlistRepository.countBySouvenirIds(souvenirIds);

        List<AiRecommendationResponse.RecommendedSouvenir> finalSouvenirs = souvenirs.stream()
                .map(s -> AiRecommendationResponse.RecommendedSouvenir.from(
                        s.getId(),
                        s.getName(),
                        s.getCategory().name(),
                        s.getCountryCode(),
                        getThumbnailUrl(s.getId()),
                        wishlistCountMap.getOrDefault(s.getId(), 0L),
                        wishlistedIds.contains(s.getId())
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
            Map<String, List<String>> recommendedNamesByCategory,
            Long userId
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

        Map<Long, FileResponse> thumbnailMap = getThumbnails(souvenirIds);
        Set<Long> wishlistedIds = wishlistRepository.findSouvenirIdsByUserId(userId);
        Map<Long, Long> wishlistCountMap = wishlistRepository.countBySouvenirIds(souvenirIds);

        return souvenirs.stream()
                .map(s -> AiRecommendationResponse.RecommendedSouvenir.from(
                        s.getId(),
                        s.getName(),
                        s.getCategory().name(),
                        s.getCountryCode(),
                        Optional.ofNullable(thumbnailMap.get(s.getId()))
                                .map(FileResponse::url)
                                .orElse(null),
                        wishlistCountMap.getOrDefault(s.getId(), 0L),
                        wishlistedIds.contains(s.getId())
                ))
                .collect(Collectors.toList());
    }

    private String getThumbnailUrl(Long souvenirId) {
        File file = fileQueryService.findFirst(EntityType.SOUVENIR, souvenirId);
        return fileStorage.generateUrl(file.getStorageKey());
    }

    private Map<Long, FileResponse> getThumbnails(List<Long> souvenirIds) {
        Map<Long, File> fileMap = fileQueryService.findThumbnailsByEntityIds(EntityType.SOUVENIR, souvenirIds);

        return fileMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> FileResponse.of(
                                entry.getValue(),
                                fileStorage.generateUrl(entry.getValue().getStorageKey())
                        )
                ));
    }
}
