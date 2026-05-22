package com.souzip.domain.recommend.ai.controller;

import com.souzip.domain.recommend.ai.dto.AiRecommendationResponse;
import com.souzip.domain.recommend.ai.service.AiRecommendationService;
import com.souzip.global.common.dto.SuccessResponse;
import com.souzip.global.security.annotation.CurrentUserId;
import com.souzip.global.security.annotation.RequireAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/discovery/ai")
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    @RequireAuth
    @GetMapping("/preference-category")
    public SuccessResponse<AiRecommendationResponse> getAiCategoryRecommendations(@CurrentUserId Long userId) {
        return SuccessResponse.of(aiRecommendationService.getCategoryRecommendationsForUser(userId));
    }

    @RequireAuth
    @GetMapping("/preference-upload")
    public SuccessResponse<AiRecommendationResponse> getAiRecentSouvenirRecommendations(@CurrentUserId Long userId) {
        return SuccessResponse.of(aiRecommendationService.getRecentSouvenirRecommendations(userId));
    }
}
