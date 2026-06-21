package com.fittrack.fittrackbackend.nutrition.controller;

import com.fittrack.fittrackbackend.nutrition.dto.CreateFoodLogRequest;
import com.fittrack.fittrackbackend.nutrition.dto.DailyNutritionSummaryResponse;
import com.fittrack.fittrackbackend.nutrition.dto.FoodLogResponse;
import com.fittrack.fittrackbackend.nutrition.service.FoodLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodLogController {
    private final FoodLogService foodLogService;
    @PostMapping("/log")
    public ResponseEntity<String> createFoodLog(@Valid @RequestBody CreateFoodLogRequest request){
        foodLogService.createFoodLog(request);
        return ResponseEntity.ok("Food logged successfully");
    }
    @GetMapping("/logs/today")
    public List<FoodLogResponse> getFoodLog(){
        return foodLogService.getTodayLogs();
    }

    @GetMapping("/summary/today")
    public DailyNutritionSummaryResponse getTodaySummary(){

        return foodLogService.getTodaySummary();
    }
    @GetMapping("/summary/date")
    public DailyNutritionSummaryResponse getSummaryDate(@RequestParam LocalDate date){
        return foodLogService.getSummaryByDate(date);
    }
    @GetMapping("/logs/date")
    public List<FoodLogResponse>getLogsByDate(@RequestParam LocalDate date){
        return foodLogService.getLogsByDateResponse(date);
    }
    @DeleteMapping("log/{logId}")
    public ResponseEntity<String>deleteFoodLog(@PathVariable Long logId){
        foodLogService.deleteFoodLog(logId);
        return ResponseEntity.ok("Food Log deleted successfully");
    }
    @PutMapping("log/{logId}")
    public ResponseEntity<String>updateFoodLog(@PathVariable Long logId, @Valid @RequestBody CreateFoodLogRequest request){
        foodLogService.updateFoodLog(logId,request);
        return ResponseEntity.ok("Food log updated successfully");
    }

}
