package com.fitness.aiservice.service;

import com.fitness.aiservice.Repository.RecommendationRepository;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final GeminiService geminiService;
    private final RecommendationRepository recommendationRepository;

    @KafkaListener(
            topics = "${kafka.topic.name}",
            groupId = "activity-processor-group"
    )
    public void processActivity(Activity activity){
        log.info("Processing Activity : {}", activity.getUserId());
        log.info("Processing Activity for userId: {}", activity.getUserId());

        Recommendation recommendation = geminiService.getRecommendations(activity);
        Recommendation savedRecommendation = recommendationRepository.save(recommendation);
        log.info("Recommendations: {}", recommendation);
        log.info(
                "Recommendation saved id={} for activityId={}",
                savedRecommendation.getId(),
                activity.getId()
        );
    }
}
