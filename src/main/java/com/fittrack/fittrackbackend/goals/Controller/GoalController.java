package com.fittrack.fittrackbackend.goals.Controller;


import com.fittrack.fittrackbackend.goals.Service.GoalsService;
import com.fittrack.fittrackbackend.goals.dto.CreateGoalRequest;
import com.fittrack.fittrackbackend.goals.dto.GoalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {
    private final GoalsService goalsService;

    @PostMapping
    public ResponseEntity<GoalResponse>createOrUpdateGoal(@Valid @RequestBody CreateGoalRequest request, Authentication authentication){
        String email = authentication.getName();
        GoalResponse goalResponse = goalsService.createOrUpdateGoal(email, request);
        return ResponseEntity.ok(goalResponse);
    }

    @GetMapping
    public ResponseEntity<GoalResponse> getGoal(
            Authentication authentication) {

        String email = authentication.getName();

        GoalResponse response =
                goalsService.getGoal(email);

        return ResponseEntity.ok(response);
    }
    @DeleteMapping
    public ResponseEntity<String> deleteGoal(
            Authentication authentication) {

        String email = authentication.getName();

        goalsService.deleteGoal(email);

        return ResponseEntity.ok("Goal deleted successfully");
    }
}
