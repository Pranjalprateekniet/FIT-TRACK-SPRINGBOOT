package com.fittrack.fittrackbackend.service;


import com.fittrack.fittrackbackend.dto.CreateWorkoutRequest;
import com.fittrack.fittrackbackend.dto.UpdateWorkoutRequest;
import com.fittrack.fittrackbackend.dto.WorkoutResponse;
import com.fittrack.fittrackbackend.entity.User;
import com.fittrack.fittrackbackend.entity.Workout;
import com.fittrack.fittrackbackend.exception.UnauthorizedWorkoutAccessRequestException;
import com.fittrack.fittrackbackend.exception.WorkoutNotFoundException;
import com.fittrack.fittrackbackend.repository.UserRepository;
import com.fittrack.fittrackbackend.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkoutService {
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    public String createWorkout(CreateWorkoutRequest request){
       User user=getAuthenticatedUser();
        Workout workout=Workout.builder()
                .title(request.getTitle())
                .durationMinutes(request.getDurationMinutes())
                .caloriesBurned(request.getCaloriesBurned())
                .workoutDate(request.getWorkoutDate())
                .user(user)
                .build();
        workoutRepository.save(workout);
        return "Workout created successfully";

    }
    public List<WorkoutResponse> getMyWorkouts(){
        User user=getAuthenticatedUser();
        List<Workout> workouts=workoutRepository.findByUser(user);
        List<WorkoutResponse> responses=new ArrayList<>();
        for(Workout workout : workouts){
            WorkoutResponse workoutResponse=new WorkoutResponse();
            workoutResponse.setId(workout.getId());
            workoutResponse.setWorkoutDate(workout.getWorkoutDate());
            workoutResponse.setTitle(workout.getTitle());
            workoutResponse.setCaloriesBurned(workout.getCaloriesBurned());
            workoutResponse.setDurationMinutes(workout.getDurationMinutes());
            responses.add(workoutResponse);
        }
        return responses;

    }
    private User getAuthenticatedUser(){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(()-> new UnauthorizedWorkoutAccessRequestException("User not found"));
    }
    public void deleteWorkout(UUID workoutId){
        User user =getAuthenticatedUser();
        Workout workout=workoutRepository.findById(workoutId).orElseThrow(()->new WorkoutNotFoundException("Workout not found"));
        if(!workout.getUser().getId().equals(user.getId())){
            throw new UnauthorizedWorkoutAccessRequestException("You are not authorized to delete this workout");
        }
        workoutRepository.delete(workout);

    }
    public WorkoutResponse updateWorkout(UUID workoutId, UpdateWorkoutRequest request){
        User user=getAuthenticatedUser();

        Workout workout = workoutRepository.findById(workoutId)
                .orElseThrow(() -> new WorkoutNotFoundException("Workout not found"));

        if (!workout.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedWorkoutAccessRequestException(
                    "You are not authorized to update this workout"
            );
        }

        workout.setTitle(request.getTitle());
        workout.setDurationMinutes(request.getDurationMinutes());
        workout.setCaloriesBurned(request.getCaloriesBurned());
        workout.setWorkoutDate(request.getWorkoutDate());

        Workout updatedWorkout = workoutRepository.save(workout);

        WorkoutResponse response = new WorkoutResponse();

        response.setId(updatedWorkout.getId());
        response.setTitle(updatedWorkout.getTitle());
        response.setDurationMinutes(updatedWorkout.getDurationMinutes());
        response.setCaloriesBurned(updatedWorkout.getCaloriesBurned());
        response.setWorkoutDate(updatedWorkout.getWorkoutDate());

        return response;
    }


}
