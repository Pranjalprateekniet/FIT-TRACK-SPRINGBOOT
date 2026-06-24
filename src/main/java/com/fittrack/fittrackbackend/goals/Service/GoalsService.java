package com.fittrack.fittrackbackend.goals.Service;

import com.fittrack.fittrackbackend.entity.User;
import com.fittrack.fittrackbackend.goals.dto.CreateGoalRequest;
import com.fittrack.fittrackbackend.goals.dto.GoalResponse;
import com.fittrack.fittrackbackend.goals.entity.GoalEntity;
import com.fittrack.fittrackbackend.goals.enums.ActivityLevel;
import com.fittrack.fittrackbackend.goals.enums.Gender;
import com.fittrack.fittrackbackend.goals.enums.GoalPace;
import com.fittrack.fittrackbackend.goals.enums.GoalType;
import com.fittrack.fittrackbackend.goals.repository.GoalRepository;
import com.fittrack.fittrackbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoalsService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    private double calculateBMR(CreateGoalRequest request) {
        if(request.getGender()== Gender.MALE){
            return (10*request.getWeightKg())+(6.25*request.getHeightCm())-(5*request.getAge())+5;

        }
        return (10*request.getWeightKg())+(6.25*request.getHeightCm())-(5*request.getAge())-161;
    }

    private double calculateTDEE(double bmr, ActivityLevel activityLevel){
        return switch (activityLevel){
            case SEDENTARY -> bmr * 1.2;

            case LIGHTLY_ACTIVE -> bmr * 1.375;

            case MODERATELY_ACTIVE -> bmr * 1.55;

            case VERY_ACTIVE -> bmr * 1.725;

            case HYPER_ACTIVE -> bmr * 1.9;
        };
    }

    private int calculateTargetCalories(double tdee,
                                        CreateGoalRequest request) {

        GoalType goalType = request.getGoalType();
        GoalPace goalPace = request.getGoalPace();

        int calories;

        // Maintenance
        if (goalType == GoalType.MAINTENANCE) {
            calories = (int) tdee;
        } else {

            int calorieAdjustment = switch (goalPace) {
                case EASY -> 250;
                case MODERATE -> 500;
                case AGGRESSIVE -> 750;
                case EXTREME -> 1000;
            };

            // Weight Loss
            if (goalType == GoalType.WEIGHT_LOSS) {
                calories = (int) (tdee - calorieAdjustment);
            }

            // Muscle Gain
            else {
                calories = switch (goalPace) {
                    case EASY -> (int) (tdee + 150);
                    case MODERATE -> (int) (tdee + 300);
                    case AGGRESSIVE -> (int) (tdee + 500);

                    // Extreme bulking is not recommended
                    case EXTREME ->
                            throw new RuntimeException(
                                    "Extreme muscle gain is not supported");
                };
            }
        }

        // Enforce minimum safe calorie intake
        if (request.getGender() == Gender.MALE) {
            calories = Math.max(calories, 1500);
        } else {
            calories = Math.max(calories, 1200);
        }

        return calories;
    }

    private double calculateBMI(double weightKg,
                                double heightCm){

        double heightM = heightCm / 100;

        return weightKg / (heightM * heightM);
    }
    private String calculateBMICategory(double bmi){

        if(bmi < 18.5)
            return "UNDERWEIGHT";

        if(bmi < 25)
            return "NORMAL";

        if(bmi < 30)
            return "OVERWEIGHT";

        return "OBESE";
    }

    private double calculateProtein(CreateGoalRequest request){

        return switch (request.getGoalType()){

            case WEIGHT_LOSS -> request.getWeightKg()*2.0;

            case MAINTENANCE -> request.getWeightKg()*1.6;

            case MUSCLE_GAIN -> request.getWeightKg()*2.2;
        };
    }
    private double calculateFat(CreateGoalRequest request){
        return request.getWeightKg()*0.8;
    }

    private double calculateCarbohydrates(int calories,
                                          double protein,
                                          double fat){

        double caloriesFromProtein = protein*4;

        double caloriesFromFat = fat*9;

        double remainingCalories =
                calories-caloriesFromProtein-caloriesFromFat;

        return remainingCalories/4;
    }

    private double round(double value){
        return Math.round(value*10.0)/10.0;
    }

    private void validateGoal(CreateGoalRequest request,
                              double bmi) {

        if (bmi < 18.5 &&
                request.getGoalType() == GoalType.WEIGHT_LOSS) {

            throw new RuntimeException(
                    "Weight loss goal is not recommended for underweight users");
        }

        if (bmi >= 30 &&
                request.getGoalType() == GoalType.MUSCLE_GAIN) {

            throw new RuntimeException(
                    "Muscle gain is not recommended for obese users");
        }

        if (request.getGoalType() == GoalType.WEIGHT_LOSS &&
                request.getTargetWeightKg() >= request.getWeightKg()) {

            throw new RuntimeException(
                    "Target weight must be less than current weight");
        }



        if (request.getGoalType() == GoalType.MUSCLE_GAIN &&
                request.getTargetWeightKg() <= request.getWeightKg()) {

            throw new RuntimeException(
                    "Target weight must be greater than current weight");
        }

        if (request.getGoalType() == GoalType.MAINTENANCE &&
                !request.getWeightKg()
                        .equals(request.getTargetWeightKg())) {

            throw new RuntimeException(
                    "Maintenance goal should have same current and target weight");
        }

        double targetBMI = calculateBMI(
                request.getTargetWeightKg(),
                request.getHeightCm()
        );

        if (targetBMI < 18.5) {
            throw new RuntimeException(
                    "Target weight results in an unhealthy BMI");
        }

        if (request.getGoalType() == GoalType.MUSCLE_GAIN &&
                request.getTargetWeightKg()
                        - request.getWeightKg() > 30) {

            throw new RuntimeException(
                    "Target weight increase is unrealistic");
        }

        double weightDifference =
                Math.abs(request.getWeightKg()
                        - request.getTargetWeightKg());

        if (weightDifference < 5 &&
                request.getGoalPace() == GoalPace.EXTREME) {

            throw new RuntimeException(
                    "Extreme pace is not recommended for small weight changes");
        }
    }

    public GoalResponse createOrUpdateGoal(String email,CreateGoalRequest request){
        User user=userRepository.findByEmail(email).orElseThrow(()->new RuntimeException("User not found"));

        GoalEntity goal=goalRepository.findByUser(user).orElse(new GoalEntity());

        goal.setUser(user);

        //calulating bmi
        double bmi=calculateBMI(request.getWeightKg(),request.getHeightCm());

        validateGoal(request,bmi);

        double bmr=calculateBMR(request);

        double tdee=calculateTDEE(bmr,request.getActivityLevel());

        int targetcalories=calculateTargetCalories(tdee,request);

        double protein=calculateProtein(request);

        double fat= calculateFat(request);

        double carbohydrates=calculateCarbohydrates(targetcalories,protein,fat);

        goal.setGoalType(request.getGoalType());
        goal.setGoalPace(request.getGoalPace());

        goal.setGender(request.getGender());
        goal.setActivityLevel(request.getActivityLevel());

        goal.setWeightKg(request.getWeightKg());
        goal.setHeightCm(request.getHeightCm());
        goal.setAge(request.getAge());

        goal.setTargetCalories(targetcalories);
        goal.setTargetProtein(round(protein));
        goal.setTargetFat(round(fat));
        goal.setTargetCarbohydrates(
                round(carbohydrates)
        );

        goal.setBmi(round(bmi));
        goal.setBmiCategory(
                calculateBMICategory(bmi)
        );

        goal.setTargetWeightKg(
                request.getTargetWeightKg());

        // Save
        GoalEntity savedGoal =
                goalRepository.save(goal);

        // Return response
        return GoalResponse.builder()
                .goalType(savedGoal.getGoalType())
                .goalPace(savedGoal.getGoalPace())
                .targetCalories(
                        savedGoal.getTargetCalories()
                )
                .targetProtein(
                        savedGoal.getTargetProtein()
                )
                .targetCarbohydrates(
                        savedGoal.getTargetCarbohydrates()
                )
                .targetFat(
                        savedGoal.getTargetFat()
                )
                .bmi(savedGoal.getBmi())
                .bmiCategory(
                        savedGoal.getBmiCategory()
                )
                .currentWeightKg(savedGoal.getWeightKg())
                .targetWeightKg(savedGoal.getTargetWeightKg())
                .estimatedWeeksToGoal(
                        calculateEstimatedWeeks(
                                savedGoal.getWeightKg(),
                                savedGoal.getTargetWeightKg(),
                                savedGoal.getGoalPace()
                        ))
                .build();
    }

    public GoalResponse getGoal(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        GoalEntity goal = goalRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Goal not found"));

        return GoalResponse.builder()
                .goalType(goal.getGoalType())
                .goalPace(goal.getGoalPace())
                .targetCalories(goal.getTargetCalories())
                .targetProtein(goal.getTargetProtein())
                .targetCarbohydrates(goal.getTargetCarbohydrates())
                .targetFat(goal.getTargetFat())
                .bmi(goal.getBmi())
                .bmiCategory(goal.getBmiCategory())
                .currentWeightKg(goal.getWeightKg())
                .targetWeightKg(goal.getTargetWeightKg())
                .estimatedWeeksToGoal(
                        calculateEstimatedWeeks(
                                goal.getWeightKg(),
                                goal.getTargetWeightKg(),
                                goal.getGoalPace()
                        ))
                .build();
    }

    public void deleteGoal(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        GoalEntity goal = goalRepository.findByUser(user)
                .orElseThrow(() ->
                        new RuntimeException("Goal not found"));

        goalRepository.delete(goal);
    }

    private int calculateEstimatedWeeks(double currentWeight, double targetWeight, GoalPace goalPace) {

        double weightDifference =
                Math.abs(currentWeight - targetWeight);

        double weeklyChange = switch (goalPace) {
            case EASY -> 0.25;
            case MODERATE -> 0.5;
            case AGGRESSIVE -> 0.75;
            case EXTREME -> 1.0;
        };

        return (int) Math.ceil(weightDifference / weeklyChange);
    }
}
