package com.souzip.api.domain.recommend.ai.controller;

import com.souzip.api.domain.recommend.ai.dto.AiRecommendationResponse;
import com.souzip.api.domain.recommend.ai.service.AiRecommendationService;
import com.souzip.api.global.common.dto.SuccessResponse;
import com.souzip.api.global.security.annotation.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/discovery/ai")
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;

    @GetMapping("/preference-category")
    public SuccessResponse<AiRecommendationResponse> getAiCategoryRecommendations(@CurrentUserId Long userId) {
        return SuccessResponse.of(aiRecommendationService.getCategoryRecommendationsForUser(userId));
    }
}
