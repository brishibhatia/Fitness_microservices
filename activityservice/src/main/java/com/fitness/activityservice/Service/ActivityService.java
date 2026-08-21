package com.fitness.activityservice.Service;

import com.fitness.activityservice.Repository.ActivityRepository;
import com.fitness.activityservice.dto.ActivityRequestDto;
import com.fitness.activityservice.dto.ActivityResponseDto;
import com.fitness.activityservice.model.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final KafkaTemplate<String, Activity> kafkaTemplate;
    @Value("${kafka.topic.name}")
    private String topic;

    public ActivityResponseDto trackActivity(ActivityRequestDto activityRequestDto) {
        boolean isvalid = userValidationService.validate(activityRequestDto.getUserId());
        if(!isvalid){
            throw new RuntimeException("Invalid User" + activityRequestDto.getUserId()) ;
        }
        log.info("User is present : {}", activityRequestDto.getUserId());
        Activity activity = Activity.builder()
                .userId(activityRequestDto.getUserId())
                .type(activityRequestDto.getType())
                .caloriesBurned(activityRequestDto.getCaloriesBurned())
                .duration(activityRequestDto.getDuration())
                .additionalMetrices(activityRequestDto.getAdditionalMetrices())
                .startTime(activityRequestDto.getStartTime())
                .build();
        Activity savedActivity = activityRepository.save(activity);

        try{
            kafkaTemplate.send(
                    topic,
                    savedActivity.getUserId(),
                    savedActivity
            ).whenComplete((result, ex) -> {

                if (ex == null) {

                    log.info(
                            "Kafka publish SUCCESS topic={}, partition={}, offset={}",
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset()
                    );

                } else {

                    log.error("Kafka publish FAILED", ex);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return MaptoResponse(savedActivity);
    }







    private ActivityResponseDto MaptoResponse(Activity savedActivity) {
        ActivityResponseDto activityResponseDto = new ActivityResponseDto();
        activityResponseDto.setId(savedActivity.getId());
        activityResponseDto.setUserId(savedActivity.getUserId());
        activityResponseDto.setType(savedActivity.getType());
        activityResponseDto.setStartTime(savedActivity.getStartTime());
        activityResponseDto.setDuration(savedActivity.getDuration());
        activityResponseDto.setAdditionalMetrices(savedActivity.getAdditionalMetrices());
        activityResponseDto.setCreatedAt(savedActivity.getCreatedAt());
        activityResponseDto.setUpdatedAt(savedActivity.getUpdatedAt());

        return activityResponseDto;
    }

    public List<ActivityResponseDto> getUserActivities(Integer userId) {
        return null;
    }
}
