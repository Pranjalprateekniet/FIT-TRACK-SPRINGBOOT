package com.fittrack.fittrackbackend.controller;

import com.fittrack.fittrackbackend.dto.CreateWorkoutRequest;
import com.fittrack.fittrackbackend.dto.UpdateWorkoutRequest;
import com.fittrack.fittrackbackend.dto.WorkoutResponse;
import com.fittrack.fittrackbackend.service.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workouts")
public class WorkoutController {
    private final WorkoutService workoutService;
    @PostMapping
    public String createWorkout(@Valid @RequestBody CreateWorkoutRequest workoutRequest){
        return workoutService.createWorkout(workoutRequest);
    }
    @GetMapping
    public List<WorkoutResponse>getMyWorkouts(){
        System.out.println("GET WORKOUTS HIT");
        return workoutService.getMyWorkouts();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteWorkout(@PathVariable UUID id){
        workoutService.deleteWorkout(id);
        return ResponseEntity.ok("Workout deleted Successfully");
    }
    @PutMapping("/{id}")
    public ResponseEntity<WorkoutResponse>upadteWorkout(@PathVariable UUID id,  @Valid @RequestBody UpdateWorkoutRequest request){
        WorkoutResponse response=workoutService.updateWorkout(id,request);
        return ResponseEntity.ok(response);
    }
}
