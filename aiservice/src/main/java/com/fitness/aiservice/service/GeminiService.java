package com.fitness.aiservice.service;
import tools.jackson.databind.JsonNode;
import com.fitness.aiservice.dto.GeminiRequest;
import com.fitness.aiservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {
    private final WebClient geminiWebClient;

    @Value("${gemini.api.model}")
    private String model;

//    public String getRecommendations(Activity activity){
//         String prompt = buildPrompt(activity);
//        GeminiRequest request = new GeminiRequest(
//                model,
//                prompt
//        );
//
//        JsonNode response = geminiWebClient
//                .post()
//                .uri("/v1beta/interactions")
//                .bodyValue(request)
//                .retrieve()
//                .bodyToMono(JsonNode.class)
//                .block();
//
//        if (response == null) {
//            throw new RuntimeException("Empty response from Gemini");
//        }
//
//        JsonNode steps = response.get("steps");
//
//        if (steps != null && steps.isArray()) {
//
//            for (JsonNode step : steps) {
//
//                if ("model_output".equals(step.path("type").asText())) {
//
//                    JsonNode content = step.get("content");
//
//                    if (content != null && content.isArray()
//                            && !content.isEmpty()) {
//
//                        return content
//                                .get(0)
//                                .path("text")
//                                .asText();
//                    }
//                }
//            }
//        }
//
//        throw new RuntimeException(
//                "Could not find model output in Gemini response"
//        );
//
//    }
public String getRecommendations(Activity activity) {

    log.info(">>> ENTERED GeminiService");

    String prompt = buildPrompt(activity);

    GeminiRequest request = new GeminiRequest(
            model,
            prompt
    );

    log.info(">>> Gemini model: {}", model);
    log.info(">>> Calling Gemini API");

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
                                                    ">>> GEMINI HTTP ERROR status={} body={}",
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

        log.info(">>> RAW GEMINI RESPONSE: {}", response);

        if (response == null) {
            throw new RuntimeException("Gemini returned null response");
        }

        JsonNode steps = response.path("steps");

        for (JsonNode step : steps) {

            if ("model_output".equals(step.path("type").asText())) {

                JsonNode content = step.path("content");

                if (content.isArray() && !content.isEmpty()) {

                    String recommendation =
                            content.get(0)
                                    .path("text")
                                    .asText();

                    log.info(
                            ">>> EXTRACTED RECOMMENDATION: {}",
                            recommendation
                    );

                    return recommendation;
                }
            }
        }

        throw new RuntimeException(
                "model_output not found in Gemini response: " + response
        );

    } catch (Exception e) {

        log.error(
                ">>>>>>>> GEMINI SERVICE FAILED <<<<<<<<",
                e
        );

        throw e;
    }
}
    private String buildPrompt(Activity activity) {

        return """
                You are a fitness recommendation assistant.

                Analyze this activity:

                Activity type: %s
                Duration: %s minutes
                Calories burned: %s
                Start time: %s
                Additional metrics: %s

                Provide:
                1. A short evaluation of the activity
                2. Recovery advice
                3. Recommended next workout
                4. Hydration and nutrition advice
                5. One improvement suggestion

                Keep the recommendation concise and practical.
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
