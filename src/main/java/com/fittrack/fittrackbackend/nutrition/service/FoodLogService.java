package com.fittrack.fittrackbackend.nutrition.service;


import com.fittrack.fittrackbackend.entity.User;
import com.fittrack.fittrackbackend.exception.UnauthorizedWorkoutAccessRequestException;
import com.fittrack.fittrackbackend.nutrition.dto.CreateFoodLogRequest;
import com.fittrack.fittrackbackend.nutrition.dto.DailyNutritionSummaryResponse;
import com.fittrack.fittrackbackend.nutrition.dto.FoodLogResponse;
import com.fittrack.fittrackbackend.nutrition.entity.FoodLog;
import com.fittrack.fittrackbackend.nutrition.entity.FoodNutrition;
import com.fittrack.fittrackbackend.nutrition.exception.FoodNotFoundException;
import com.fittrack.fittrackbackend.nutrition.repository.FoodLogRepository;
import com.fittrack.fittrackbackend.nutrition.repository.FoodNutritionRepository;
import com.fittrack.fittrackbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodLogService {
    private final FoodLogRepository foodLogRepository;
    private final FoodNutritionRepository foodNutritionRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(()-> new UnauthorizedWorkoutAccessRequestException("User not found"));
    }
    public void createFoodLog(CreateFoodLogRequest request){
        User user =getAuthenticatedUser();
        FoodNutrition foodNutrition=foodNutritionRepository.findById(request.getFoodId()).orElseThrow(()->new FoodNotFoundException("Food not found"));
        LocalDate logDate=request.getLogDate()==null
                ?LocalDate.now()
                :request.getLogDate();

        FoodLog foodLog=FoodLog.builder()
                .user(user)
                .foodNutrition(foodNutrition)
                .gramsConsumed(request.getGramsConsumed())
                .mealType(request.getMealType())
                .logDate(logDate)
                .build();
        foodLogRepository.save(foodLog);

    }
    public List<FoodLogResponse>getTodayLogs(){
        return mapToFoodLogResponse(getLogsByDate(LocalDate.now()));
    }

    public DailyNutritionSummaryResponse getSummaryByDate(LocalDate date){
        return calculateSummary(date);
    }
    public DailyNutritionSummaryResponse getTodaySummary(){
        return calculateSummary(LocalDate.now());
    }


    private double safe(Double value){
        if(value!=null){
            return value;
        }
        return 0;
    }
    private double safe(Integer value) {
        return value == null ? 0 : value;
    }

    private List<FoodLog>getLogsByDate(LocalDate date){
        User user=getAuthenticatedUser();
        return foodLogRepository.findByUserAndLogDate(user,date);
    }

    private DailyNutritionSummaryResponse calculateSummary(
            LocalDate date
    ){
        List<FoodLog> foodLogs =
                getLogsByDate(date);

        double totalCalories = 0;
        double totalProtein = 0;
        double totalCarbs = 0;
        double totalFat = 0;
        double totalFiber = 0;
        double totalFreeSugar = 0;
        double totalCholesterol = 0;

        for(FoodLog foodLog : foodLogs){

            FoodNutrition food =
                    foodLog.getFoodNutrition();

            double servingSize =
                    food.getServingSizeG() == null
                            ? 100.0
                            : food.getServingSizeG();

            double multiplier =
                    foodLog.getGramsConsumed() / servingSize;

            totalCalories +=
                    safe(food.getCalories()) * multiplier;

            totalProtein +=
                    safe(food.getProteinG()) * multiplier;

            totalCarbs +=
                    safe(food.getCarbsG()) * multiplier;

            totalFat +=
                    safe(food.getFatG()) * multiplier;

            totalFiber +=
                    safe(food.getFiberG()) * multiplier;

            totalFreeSugar +=
                    safe(food.getFreeSugarG()) * multiplier;

            totalCholesterol +=
                    safe(food.getCholesterolMg()) * multiplier;
        }

        return DailyNutritionSummaryResponse.builder()
                .totalCalories(totalCalories)
                .totalCarbs(totalCarbs)
                .totalProtein(totalProtein)
                .totalCholesterol(totalCholesterol)
                .totalFat(totalFat)
                .totalFiber(totalFiber)
                .totalFreeSugar(totalFreeSugar)
                .build();
    }

    private List<FoodLogResponse>mapToFoodLogResponse(List<FoodLog>foodLogs){
        return foodLogs.stream()
                .map(foodLog -> FoodLogResponse.builder()
                        .foodName(foodLog.getFoodNutrition().getFoodName()
                        )
                        .logId(foodLog.getId())
                        .gramsConsumed(foodLog.getGramsConsumed())
                        .mealType((foodLog.getMealType()))
                        .logDate(foodLog.getLogDate())
                        .build()
                )
                .toList();

    }
    public List<FoodLogResponse>getLogsByDateResponse(LocalDate date){
        return mapToFoodLogResponse(getLogsByDate(date));
    }

    public void deleteFoodLog(Long logId) {
        User user=getAuthenticatedUser();
        FoodLog foodLog=foodLogRepository.findById(logId)
                .orElseThrow(()->new FoodNotFoundException("Food log not found"));
        if(!foodLog.getUser().getId().equals(user.getId())){
            throw new UnauthorizedWorkoutAccessRequestException("You cannnot delete another user's food log");

        }
        foodLogRepository.delete(foodLog);
    }

    public void updateFoodLog(Long logId,CreateFoodLogRequest request){
        User user=getAuthenticatedUser();
        FoodLog foodLog=foodLogRepository.findById(logId)
                .orElseThrow(()->new FoodNotFoundException("Food log not found"));
        if(!foodLog.getUser().getId().equals(user.getId())){
            throw new UnauthorizedWorkoutAccessRequestException("You cannnot update another user's food log");
        }
        FoodNutrition foodNutrition=foodNutritionRepository.findById(request.getFoodId()).orElseThrow(()->new FoodNotFoundException("Food not found"));
        foodLog.setFoodNutrition(foodNutrition);
        foodLog.setGramsConsumed(request.getGramsConsumed());
        foodLog.setMealType(request.getMealType());
        LocalDate logDate=request.getLogDate()==null
                ?foodLog.getLogDate()
                :request.getLogDate();
        foodLog.setLogDate(logDate);
        foodLogRepository.save(foodLog);
    }

}
