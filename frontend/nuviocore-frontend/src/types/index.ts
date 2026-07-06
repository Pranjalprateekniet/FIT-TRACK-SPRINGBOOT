// Enums
export type AuthProvider = 'LOCAL' | 'GOOGLE';

export type GoalType = 'WEIGHT_LOSS' | 'MAINTENANCE' | 'MUSCLE_GAIN';

export type GoalPace = 'EASY' | 'MODERATE' | 'AGGRESSIVE' | 'EXTREME';

export type Gender = 'MALE' | 'FEMALE';

export type ActivityLevel = 
  | 'SEDENTARY' 
  | 'LIGHTLY_ACTIVE' 
  | 'MODERATELY_ACTIVE' 
  | 'VERY_ACTIVE' 
  | 'HYPER_ACTIVE';

export type MealType = 
  | 'BREAKFAST' 
  | 'MORNING_SNACK' 
  | 'LUNCH' 
  | 'EVENING_SNACK' 
  | 'DINNER';

// Goals DTOs
export interface CreateGoalRequest {
  goalType: GoalType;
  goalPace: GoalPace;
  gender: Gender;
  activityLevel: ActivityLevel;
  weightKg: number;
  heightCm: number;
  targetWeightKg: number;
  age: number;
}

export interface GoalResponse {
  goalType: GoalType;
  goalPace: GoalPace;
  targetCalories: number;
  targetProtein: number;
  targetCarbohydrates: number;
  targetFat: number;
  bmi: number;
  bmiCategory: string;
  currentWeightKg: number;
  targetWeightKg: number;
  estimatedWeeksToGoal: number;
}

export type WorkoutCategory = 'STRENGTH' | 'CARDIO' | 'YOGA';

export type WorkoutIntensity = 'LIGHT' | 'MODERATE' | 'HEAVY' | 'VERY_HEAVY' | 'VIGOROUS' | 'VERY_VIGOROUS' | 'ADVANCED';

export type WorkoutExercise =
  // Strength
  | 'BENCH_PRESS' | 'INCLINE_BENCH_PRESS' | 'DECLINE_BENCH_PRESS' | 'CHEST_FLY' | 'PUSH_UPS'
  | 'LAT_PULLDOWN' | 'PULL_UPS' | 'SEATED_ROW' | 'BARBELL_ROW' | 'DEADLIFT'
  | 'SHOULDER_PRESS' | 'LATERAL_RAISE' | 'FRONT_RAISE' | 'REAR_DELT_FLY' | 'ARNOLD_PRESS'
  | 'BARBELL_CURL' | 'DUMBBELL_CURL' | 'HAMMER_CURL' | 'CABLE_CURL'
  | 'PUSHDOWN' | 'SKULL_CRUSHERS' | 'OVERHEAD_EXTENSION' | 'BENCH_DIPS'
  | 'SQUATS' | 'LEG_PRESS' | 'LUNGES' | 'ROMANIAN_DEADLIFT' | 'LEG_EXTENSION' | 'HAMSTRING_CURL' | 'CALF_RAISE'
  | 'CRUNCHES' | 'HANGING_LEG_RAISE' | 'RUSSIAN_TWIST' | 'PLANK'
  // Cardio
  | 'WALKING' | 'JOGGING' | 'RUNNING' | 'CYCLING' | 'STATIONARY_BIKE' | 'ELLIPTICAL' | 'SKIPPING_ROPE' | 'SWIMMING' | 'STAIR_CLIMBER' | 'ROWING' | 'TREADMILL'
  // Yoga
  | 'HATHA_YOGA' | 'POWER_YOGA' | 'VINYASA' | 'ASHTANGA' | 'SURYA_NAMASKAR' | 'STRETCHING';

export interface CreateWorkoutRequest {
  category: WorkoutCategory;
  exercise: WorkoutExercise;
  sets?: number;
  reps?: number;
  intensity: WorkoutIntensity;
  durationMinutes?: number;
  workoutDate: string; // YYYY-MM-DD
}

export interface UpdateWorkoutRequest {
  category: WorkoutCategory;
  exercise: WorkoutExercise;
  sets?: number;
  reps?: number;
  intensity: WorkoutIntensity;
  durationMinutes?: number;
  workoutDate: string; // YYYY-MM-DD
}

export interface WorkoutResponse {
  id: string; // UUID
  category: WorkoutCategory;
  exercise: WorkoutExercise;
  sets?: number;
  reps?: number;
  intensity: WorkoutIntensity;
  durationMinutes?: number;
  estimatedDuration: number;
  caloriesBurned: number;
  workoutDate: string; // YYYY-MM-DD
}

// Nutrition DTOs
export interface CreateFoodLogRequest {
  foodId: number;
  gramsConsumed: number;
  mealType: MealType;
  logDate?: string; // YYYY-MM-DD
}

export interface FoodLogResponse {
  logId: number;
  foodName: string;
  gramsConsumed: number;
  mealType: MealType;
  logDate: string; // YYYY-MM-DD
}

export interface DailyNutritionSummaryResponse {
  totalCalories: number;
  totalProtein: number;
  totalCarbs: number;
  totalFat: number;
  totalFiber: number;
  totalFreeSugar: number;
  totalCholesterol: number;
}

export interface FoodSearchResponse {
  foodName: string;
  calories: number;
  protein: number;
  carbs: number;
  fat: number;
  fiber: number;
  cholesterol: number;
  freeSugar: number;
  servingSizeG: number;
  dataType: string;
}

export interface FoodNutritionEntity {
  id: number;
  foodName: string;
  foodType: string;
  calories: number;
  carbsG: number;
  proteinG: number;
  fatG: number;
  freeSugarG: number;
  fiberG: number;
  cholesterolMg: number;
  servingSizeG: number;
  proteinCalorieRatio: number;
  source: string;
}
