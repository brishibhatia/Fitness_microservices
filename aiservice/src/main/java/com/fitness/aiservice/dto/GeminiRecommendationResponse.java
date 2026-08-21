package com.fitness.aiservice.dto;

import java.util.List;

public record GeminiRecommendationResponse(
        String recommendation,
        List<String> improvements,
        List<String> suggestions,
        List<String> safety
) {
}