package com.fitness.aiservice.service;

import com.fitness.aiservice.dto.GeminiRecommendationResponse;
import com.fitness.aiservice.dto.GeminiRequest;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.model}")
    private String model;

    public Recommendation getRecommendations(Activity activity) {

        log.info(
                "Generating recommendation for activityId={}",
                activity.getId()
        );

        String prompt = buildPrompt(activity);

        GeminiRequest request =
                new GeminiRequest(model, prompt);

        try {

            JsonNode response = geminiWebClient
                    .post()
                    .uri("/v1beta/interactions")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            clientResponse ->
                                    clientResponse
                                            .bodyToMono(String.class)
                                            .flatMap(body -> {

                                                log.error(
                                                        "Gemini HTTP error status={} body={}",
                                                        clientResponse.statusCode(),
                                                        body
                                                );

                                                return Mono.error(
                                                        new RuntimeException(
                                                                "Gemini error: " + body
                                                        )
                                                );
                                            })
                    )
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null) {
                throw new RuntimeException(
                        "Gemini returned empty response"
                );
            }

            String generatedJson =
                    extractModelOutput(response);

            log.info(
                    "Gemini generated recommendation for activityId={}",
                    activity.getId()
            );

            GeminiRecommendationResponse aiResponse =
                    objectMapper.readValue(
                            cleanJson(generatedJson),
                            GeminiRecommendationResponse.class
                    );

            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType().toString())

                    .recommendation(
                            aiResponse.recommendation()
                    )

                    .improvements(
                            aiResponse.improvements()
                    )

                    .suggestions(
                            aiResponse.suggestions()
                    )

                    .safety(
                            aiResponse.safety()
                    )

                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {

            log.error(
                    "Failed to generate recommendation for activityId={}",
                    activity.getId(),
                    e
            );

            throw new RuntimeException(
                    "Failed to generate recommendation",
                    e
            );
        }
    }

    private String extractModelOutput(JsonNode response) {

        JsonNode steps = response.path("steps");

        for (JsonNode step : steps) {

            if ("model_output".equals(
                    step.path("type").asString())) {

                JsonNode content =
                        step.path("content");

                if (content.isArray()
                        && !content.isEmpty()) {

                    return content
                            .get(0)
                            .path("text")
                            .asString();
                }
            }
        }

        throw new RuntimeException(
                "model_output not found in Gemini response"
        );
    }

    private String cleanJson(String json) {

        return json
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private String buildPrompt(Activity activity) {

        return """
                You are a fitness recommendation assistant.

                Analyze the following activity:

                Activity type: %s
                Duration: %s minutes
                Calories burned: %s
                Start time: %s
                Additional metrics: %s

                Return ONLY valid JSON.

                Do not include Markdown.
                Do not include ```json.
                Do not include explanations outside the JSON.

                The response MUST have exactly this structure:

                {
                  "recommendation": "short overall evaluation",
                  "improvements": [
                    "improvement 1",
                    "improvement 2"
                  ],
                  "suggestions": [
                    "suggestion 1",
                    "suggestion 2"
                  ],
                  "safety": [
                    "safety advice 1",
                    "safety advice 2"
                  ]
                }

                recommendation:
                Give a concise evaluation of the completed activity.

                improvements:
                Give practical ways the user could improve future workouts.

                suggestions:
                Give recovery, next-workout, hydration, and nutrition suggestions.

                safety:
                Give relevant general exercise safety precautions.

                Keep everything concise and practical.
                """
                .formatted(
                        activity.getType(),
                        activity.getDuration(),
                        activity.getCaloriesBurned(),
                        activity.getStartTime(),
                        activity.getAdditionalMetrices()
                );
    }
}