package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityMessageListener {

    private final GeminiService geminiService;

    @KafkaListener(
            topics = "${kafka.topic.name}",
            groupId = "activity-processor-group"
    )
    public void processActivity(Activity activity){
        log.info("Processing Activity : {}", activity.getUserId());
        log.info("Processing Activity for userId: {}", activity.getUserId());

        String recommendations = geminiService.getRecommendations(activity);
        log.info("Recommendations: {}", recommendations);

    }
}
