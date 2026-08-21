package com.fitness.activityservice.Controller;

import com.fitness.activityservice.Service.ActivityService;
import com.fitness.activityservice.dto.ActivityRequestDto;
import com.fitness.activityservice.dto.ActivityResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;
    @PostMapping
    public ResponseEntity<ActivityResponseDto> trackActivity(@RequestBody ActivityRequestDto activityRequestDto) {
        return ResponseEntity.ok(activityService.trackActivity(activityRequestDto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<ActivityResponseDto>> getUserActivities(@PathVariable Integer userId) {
    return ResponseEntity.ok(activityService.getUserActivities(userId));
    }
}
