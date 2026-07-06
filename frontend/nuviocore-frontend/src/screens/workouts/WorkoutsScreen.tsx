import React, { useState, useEffect } from 'react';
import { StyleSheet, Text, View, ScrollView, TouchableOpacity, RefreshControl, Modal, Alert, TextInput } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { AppLayout } from '../../components/AppLayout';
import { GlassCard } from '../../components/GlassCard';
import { GlassInput } from '../../components/GlassInput';
import { PrimaryButton, SecondaryButton } from '../../components/Buttons';
import { SectionHeader } from '../../components/SectionHeader';
import { fetchClient } from '../../api/FetchClient';
import { useNavigation } from '../../context/NavigationContext';
import { ThemeColors, Spacing, Typography } from '../../theme/Theme';
import { WorkoutResponse, CreateWorkoutRequest, WorkoutCategory, WorkoutExercise, WorkoutIntensity, GoalResponse } from '../../types';

export const WorkoutsScreen = () => {
  const { navigateTo } = useNavigation();
  const [refreshing, setRefreshing] = useState(false);
  const [workouts, setWorkouts] = useState<WorkoutResponse[]>([]);
  const [userWeight, setUserWeight] = useState<number>(70);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Modal forms state
  const [modalVisible, setModalVisible] = useState(false);
  const [editingWorkout, setEditingWorkout] = useState<WorkoutResponse | null>(null);
  const [workoutDate, setWorkoutDate] = useState(new Date().toISOString().split('T')[0]);
  const [formLoading, setFormLoading] = useState(false);

  // Dynamic Form State
  const [category, setCategory] = useState<WorkoutCategory>('STRENGTH');
  const [exercise, setExercise] = useState<WorkoutExercise>('BENCH_PRESS');
  const [sets, setSets] = useState('3');
  const [reps, setReps] = useState('10');
  const [duration, setDuration] = useState('30');
  const [intensity, setIntensity] = useState<WorkoutIntensity>('MODERATE');

  // Exercise Selection Modal State
  const [exerciseModalVisible, setExerciseModalVisible] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [recentExercises, setRecentExercises] = useState<WorkoutExercise[]>([]);

  const loadData = async () => {
    setError(null);
    try {
      const data: WorkoutResponse[] = await fetchClient('/api/workouts');
      setWorkouts(data.sort((a, b) => b.workoutDate.localeCompare(a.workoutDate)));
      
      try {
        const goalData: GoalResponse = await fetchClient('/api/goals/current');
        if (goalData && goalData.currentWeightKg) {
          setUserWeight(goalData.currentWeightKg);
        }
      } catch (e) {
        // Silent fail for goal, use default 70kg
        console.log("No goal found or error fetching goal, using default weight.");
      }

    } catch (err: any) {
      console.log("Error loading workouts:", err);
      if (err.message === 'SESSION_EXPIRED') {
        navigateTo('LOGIN');
      } else {
        setError(err.message || 'Failed to load workouts');
      }
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    loadData();
    const loadRecent = async () => {
      try {
        const stored = await AsyncStorage.getItem('@recent_exercises');
        if (stored) setRecentExercises(JSON.parse(stored));
      } catch(e) {}
    };
    loadRecent();
  }, []);

  const handleSelectExercise = async (e: WorkoutExercise) => {
    setExercise(e);
    setExerciseModalVisible(false);
    
    const newRecent = [e, ...recentExercises.filter(ex => ex !== e)].slice(0, 5);
    setRecentExercises(newRecent);
    try {
      await AsyncStorage.setItem('@recent_exercises', JSON.stringify(newRecent));
    } catch(e) {}
  };

  const onRefresh = () => {
    setRefreshing(true);
    loadData();
  };

  const openLogModal = (workout: WorkoutResponse | null = null) => {
    if (workout) {
      setEditingWorkout(workout);
      setWorkoutDate(workout.workoutDate);
      setCategory(workout.category);
      setExercise(workout.exercise);
      setIntensity(workout.intensity);
      if (workout.category === 'STRENGTH') {
        setSets(workout.sets?.toString() || '3');
        setReps(workout.reps?.toString() || '10');
      } else {
        setDuration(workout.durationMinutes?.toString() || '30');
      }
    } else {
      setEditingWorkout(null);
      setWorkoutDate(new Date().toISOString().split('T')[0]);
      setCategory('STRENGTH');
      setExercise('BENCH_PRESS');
      setSets('3');
      setReps('10');
      setIntensity('MODERATE');
      setDuration('30');
    }
    setModalVisible(true);
  };

  const handleFormSubmit = async () => {
    const durationNum = parseInt(duration, 10);
    const setsNum = parseInt(sets, 10);
    const repsNum = parseInt(reps, 10);
    
    if (category !== 'STRENGTH' && (isNaN(durationNum) || durationNum <= 0)) {
      Alert.alert("Validation Error", "Duration must be greater than zero.");
      return;
    }
    if (category === 'STRENGTH' && (isNaN(setsNum) || setsNum <= 0 || isNaN(repsNum) || repsNum <= 0)) {
      Alert.alert("Validation Error", "Sets and Reps must be greater than zero.");
      return;
    }
    if (!/^\d{4}-\d{2}-\d{2}$/.test(workoutDate)) {
      Alert.alert("Validation Error", "Date must be formatted as YYYY-MM-DD.");
      return;
    }

    setFormLoading(true);
    const body: CreateWorkoutRequest = {
      category,
      exercise,
      intensity,
      workoutDate,
      ...(category === 'STRENGTH' ? { sets: setsNum, reps: repsNum } : { durationMinutes: durationNum }),
    };

    try {
      if (editingWorkout) {
        await fetchClient(`/api/workouts/${editingWorkout.id}`, { method: 'PUT', body });
      } else {
        await fetchClient('/api/workouts', { method: 'POST', body });
      }
      setModalVisible(false);
      loadData();
    } catch (err: any) {
      Alert.alert("Submission Failed", err.message || "Failed to log workout.");
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeleteWorkout = async (workoutId: string) => {
    Alert.alert(
      "Delete Workout",
      "Are you sure you want to delete this workout?",
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Delete",
          style: "destructive",
          onPress: async () => {
            try {
              await fetchClient(`/api/workouts/${workoutId}`, { method: 'DELETE' });
              loadData();
            } catch (err: any) {
              Alert.alert("Deletion Failed", err.message || "Failed to delete workout.");
            }
          }
        }
      ]
    );
  };

  // Safe Math Helpers for Today
  const todayStr = new Date().toISOString().split('T')[0];
  const todayWorkouts = workouts.filter(w => w.workoutDate === todayStr);
  const workoutCaloriesToday = todayWorkouts.reduce((sum, w) => sum + (w.caloriesBurned || 0), 0);
  const activeMinutesToday = todayWorkouts.reduce((sum, w) => sum + (w.estimatedDuration || w.durationMinutes || 0), 0);

  // Safe Math Helpers for All Time
  const totalWorkoutCalories = workouts.reduce((sum, w) => sum + (w.caloriesBurned || 0), 0);

  // ----- Form Helpers -----
  const groupedExercises: Record<WorkoutCategory, {title: string, data: WorkoutExercise[]}[]> = {
    STRENGTH: [
      {
        title: 'CHEST',
        data: ['BENCH_PRESS', 'INCLINE_BENCH_PRESS', 'DECLINE_BENCH_PRESS', 'CHEST_FLY', 'PUSH_UPS']
      },
      {
        title: 'BACK',
        data: ['DEADLIFT', 'LAT_PULLDOWN', 'SEATED_ROW', 'BARBELL_ROW', 'PULL_UPS']
      },
      {
        title: 'SHOULDERS',
        data: ['ARNOLD_PRESS', 'LATERAL_RAISE', 'FRONT_RAISE', 'REAR_DELT_FLY', 'SHOULDER_PRESS']
      },
      {
        title: 'ARMS',
        data: ['BARBELL_CURL', 'DUMBBELL_CURL', 'HAMMER_CURL', 'CABLE_CURL', 'PUSHDOWN', 'SKULL_CRUSHERS', 'OVERHEAD_EXTENSION', 'BENCH_DIPS']
      },
      {
        title: 'LEGS',
        data: ['SQUATS', 'LEG_PRESS', 'LUNGES', 'ROMANIAN_DEADLIFT', 'LEG_EXTENSION', 'HAMSTRING_CURL', 'CALF_RAISE']
      },
      {
        title: 'CORE',
        data: ['CRUNCHES', 'HANGING_LEG_RAISE', 'RUSSIAN_TWIST', 'PLANK']
      }
    ],
    CARDIO: [
      {
        title: 'CARDIO',
        data: ['WALKING', 'JOGGING', 'RUNNING', 'CYCLING', 'STATIONARY_BIKE', 'ELLIPTICAL', 'SKIPPING_ROPE', 'SWIMMING', 'STAIR_CLIMBER', 'ROWING', 'TREADMILL']
      }
    ],
    YOGA: [
      {
        title: 'YOGA',
        data: ['HATHA_YOGA', 'POWER_YOGA', 'VINYASA', 'ASHTANGA', 'SURYA_NAMASKAR', 'STRETCHING']
      }
    ]
  };

  const exercisesByCategory: Record<WorkoutCategory, WorkoutExercise[]> = {
    STRENGTH: groupedExercises.STRENGTH.flatMap(g => g.data),
    CARDIO: groupedExercises.CARDIO.flatMap(g => g.data),
    YOGA: groupedExercises.YOGA.flatMap(g => g.data)
  };

  const intensitiesByCategory: Record<WorkoutCategory, WorkoutIntensity[]> = {
    STRENGTH: ['LIGHT', 'MODERATE', 'HEAVY', 'VERY_HEAVY'],
    CARDIO: ['LIGHT', 'MODERATE', 'VIGOROUS', 'VERY_VIGOROUS'],
    YOGA: ['LIGHT', 'MODERATE', 'ADVANCED']
  };

  const handleCategoryChange = (newCat: WorkoutCategory) => {
    setCategory(newCat);
    setExercise(exercisesByCategory[newCat][0]);
    setIntensity(intensitiesByCategory[newCat][1]); // default to moderate
  };

  const formatEnumStr = (str: string) => str.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase());

  // Live calorie estimation
  const getEstimatedCalories = () => {
    let met = 5.0; // default
    if (category === 'STRENGTH') {
      if (intensity === 'LIGHT') met = 3.5;
      else if (intensity === 'MODERATE') met = 5.0;
      else if (intensity === 'HEAVY') met = 6.0;
      else if (intensity === 'VERY_HEAVY') met = 8.0;
    } else if (category === 'YOGA') {
      if (exercise === 'POWER_YOGA') met = 5.5;
      else if (exercise === 'SURYA_NAMASKAR') met = 6.5;
      else if (intensity === 'LIGHT') met = 2.5;
      else if (intensity === 'ADVANCED') met = 5.5;
      else met = 3.5;
    } else if (category === 'CARDIO') {
      let baseMet = 7.0;
      if (exercise === 'WALKING') baseMet = 3.5;
      if (exercise === 'RUNNING') baseMet = 10.0;
      if (exercise === 'CYCLING') baseMet = 8.0;
      if (exercise === 'SKIPPING_ROPE') baseMet = 12.0;
      if (exercise === 'SWIMMING') baseMet = 8.0;
      if (exercise === 'STAIR_CLIMBER') baseMet = 9.0;
      if (exercise === 'ROWING') baseMet = 7.0;
      if (exercise === 'TREADMILL') baseMet = 9.0;
      if (exercise === 'ELLIPTICAL') baseMet = 5.0;
      if (exercise === 'STATIONARY_BIKE') baseMet = 7.0;

      if (intensity === 'LIGHT') met = baseMet * 0.8;
      else if (intensity === 'MODERATE') met = baseMet;
      else if (intensity === 'VIGOROUS') met = baseMet * 1.2;
      else if (intensity === 'VERY_VIGOROUS') met = baseMet * 1.4;
    }

    let estimatedMins = 0;
    if (category === 'STRENGTH') {
      const s = parseInt(sets) || 0;
      const r = parseInt(reps) || 0;
      const activeTimeSeconds = s * r * 4;
      let restTimeSecondsPerSet = 60;
      if (intensity === 'LIGHT') restTimeSecondsPerSet = 45;
      if (intensity === 'HEAVY') restTimeSecondsPerSet = 90;
      if (intensity === 'VERY_HEAVY') restTimeSecondsPerSet = 120;
      
      const restTimeSeconds = Math.max(0, s - 1) * restTimeSecondsPerSet;
      estimatedMins = Math.ceil((activeTimeSeconds + restTimeSeconds) / 60.0);
    } else {
      estimatedMins = parseInt(duration) || 0;
    }

    return met * userWeight * (estimatedMins / 60.0);
  };

  return (
    <AppLayout>
      <View style={styles.container}>
        <SectionHeader title="Workouts" subtitle="Training Home" />

        {error && <Text style={styles.errorText}>{error}</Text>}

        <ScrollView
          contentContainerStyle={styles.scroll}
          refreshControl={
            <RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor={'#FFFFFF'} />
          }
        >
          {/* Today's Summary */}
          <GlassCard style={styles.summaryCard}>
            <Text style={styles.cardHeading}>TODAY'S ACTIVITY</Text>
            <View style={styles.workoutSummaryRow}>
              <View style={styles.summaryItem}>
                <Text style={styles.summaryVal}>{activeMinutesToday}</Text>
                <Text style={styles.summaryLabel}>Minutes</Text>
              </View>
              <View style={styles.verticalDivider} />
              <View style={styles.summaryItem}>
                <Text style={styles.summaryVal}>{workoutCaloriesToday.toFixed(0)}</Text>
                <Text style={styles.summaryLabel}>kcal Burned</Text>
              </View>
              <View style={styles.verticalDivider} />
              <View style={styles.summaryItem}>
                <Text style={styles.summaryVal}>{todayWorkouts.length}</Text>
                <Text style={styles.summaryLabel}>Sessions</Text>
              </View>
            </View>
          </GlassCard>

          {/* Historical Summary */}
          <GlassCard style={styles.summaryCard}>
            <Text style={styles.cardHeading}>ALL-TIME SUMMARY</Text>
            <View style={styles.workoutSummaryRow}>
              <View style={styles.summaryItem}>
                <Text style={styles.summaryValWhite}>{workouts.length}</Text>
                <Text style={styles.summaryLabel}>Total Sessions</Text>
              </View>
              <View style={styles.verticalDivider} />
              <View style={styles.summaryItem}>
                <Text style={styles.summaryValWhite}>{totalWorkoutCalories.toFixed(0)}</Text>
                <Text style={styles.summaryLabel}>Total kcal Burned</Text>
              </View>
            </View>
          </GlassCard>

          <View style={styles.logActionContainer}>
            <PrimaryButton title="Log Workout Session" onPress={() => openLogModal(null)} />
          </View>

          <Text style={styles.sectionTitle}>WORKOUT HISTORY</Text>
          {workouts.length === 0 ? (
            <Text style={styles.emptyText}>No workouts recorded yet.</Text>
          ) : (
            workouts.map((w) => (
              <GlassCard key={w.id} style={styles.workoutCard}>
                <View style={styles.cardHeader}>
                  <View>
                    <Text style={styles.workoutTitle}>{formatEnumStr(w.exercise)}</Text>
                    <Text style={styles.workoutDate}>{w.workoutDate} • {formatEnumStr(w.category)} • {formatEnumStr(w.intensity)}</Text>
                  </View>
                  <Text style={styles.workoutBurn}>{w.caloriesBurned.toFixed(0)} kcal</Text>
                </View>
                <View style={styles.cardFooter}>
                  <Text style={styles.workoutDuration}>
                    {w.category === 'STRENGTH' 
                      ? `${w.sets} Sets × ${w.reps} Reps (~${w.estimatedDuration} min)` 
                      : `${w.durationMinutes} minutes`
                    }
                  </Text>
                  <View style={styles.actionRow}>
                    <TouchableOpacity onPress={() => openLogModal(w)} style={styles.actionBtn}>
                      <Text style={styles.actionTextEdit}>Edit</Text>
                    </TouchableOpacity>
                    <TouchableOpacity onPress={() => handleDeleteWorkout(w.id)} style={styles.actionBtn}>
                      <Text style={styles.actionTextDelete}>Delete</Text>
                    </TouchableOpacity>
                  </View>
                </View>
              </GlassCard>
            ))
          )}
        </ScrollView>

        {/* Modal Entry Form */}
        <Modal visible={modalVisible} transparent animationType="slide">
          <View style={styles.modalOverlay}>
            <ScrollView contentContainerStyle={styles.modalScroll}>
              <GlassCard style={styles.modalCard}>
                <Text style={styles.modalTitle}>
                  {editingWorkout ? 'Edit Workout Session' : 'Log Workout Session'}
                </Text>

                <Text style={styles.fieldLabel}>Category</Text>
                <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.pillContainer}>
                  {(['STRENGTH', 'CARDIO', 'YOGA'] as WorkoutCategory[]).map(c => (
                    <TouchableOpacity 
                      key={c} 
                      style={[styles.pill, category === c && styles.pillActive]}
                      onPress={() => handleCategoryChange(c)}
                    >
                      <Text style={[styles.pillText, category === c && styles.pillTextActive]}>{formatEnumStr(c)}</Text>
                    </TouchableOpacity>
                  ))}
                </ScrollView>

                <Text style={styles.fieldLabel}>Exercise</Text>
                <TouchableOpacity 
                  style={styles.selectorField}
                  onPress={() => {
                    setSearchQuery('');
                    setExerciseModalVisible(true);
                  }}
                >
                  <Text style={styles.selectorText}>
                    🔍 {formatEnumStr(exercise)}
                  </Text>
                  <Text style={styles.selectorArrow}>▼</Text>
                </TouchableOpacity>

                {category === 'STRENGTH' ? (
                  <View style={styles.row}>
                    <View style={{flex: 1, marginRight: Spacing.xs}}>
                      <GlassInput
                        label="Sets"
                        placeholder="e.g. 3"
                        keyboardType="numeric"
                        value={sets}
                        onChangeText={setSets}
                      />
                    </View>
                    <View style={{flex: 1, marginLeft: Spacing.xs}}>
                      <GlassInput
                        label="Reps"
                        placeholder="e.g. 10"
                        keyboardType="numeric"
                        value={reps}
                        onChangeText={setReps}
                      />
                    </View>
                  </View>
                ) : (
                  <GlassInput
                    label="Duration (Minutes)"
                    placeholder="e.g. 45"
                    keyboardType="numeric"
                    value={duration}
                    onChangeText={setDuration}
                  />
                )}

                <Text style={styles.fieldLabel}>Intensity</Text>
                <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.pillContainer}>
                  {intensitiesByCategory[category].map(i => (
                    <TouchableOpacity 
                      key={i} 
                      style={[styles.pill, intensity === i && styles.pillActive]}
                      onPress={() => setIntensity(i)}
                    >
                      <Text style={[styles.pillText, intensity === i && styles.pillTextActive]}>{formatEnumStr(i)}</Text>
                    </TouchableOpacity>
                  ))}
                </ScrollView>

                <GlassInput
                  label="Workout Date (YYYY-MM-DD)"
                  placeholder="e.g. 2026-07-01"
                  value={workoutDate}
                  onChangeText={setWorkoutDate}
                />

                <View style={styles.estimateContainer}>
                  <Text style={styles.estimateLabel}>Estimated Calories</Text>
                  <Text style={styles.estimateValue}>{getEstimatedCalories().toFixed(0)} kcal</Text>
                </View>

                <View style={styles.modalButtonRow}>
                  <View style={{ flex: 1, marginRight: Spacing.sm }}>
                    <SecondaryButton title="Cancel" onPress={() => setModalVisible(false)} />
                  </View>
                  <View style={{ flex: 1, marginLeft: Spacing.sm }}>
                    <PrimaryButton
                      title={editingWorkout ? 'Update' : 'Save'}
                      loading={formLoading}
                      onPress={handleFormSubmit}
                    />
                  </View>
                </View>
              </GlassCard>
            </ScrollView>
          </View>
        </Modal>

        {/* Exercise Selection Bottom Sheet */}
        <Modal
          visible={exerciseModalVisible}
          transparent
          animationType="slide"
          onRequestClose={() => setExerciseModalVisible(false)}
        >
          <View style={styles.bottomSheetOverlay}>
            <View style={styles.bottomSheetContainer}>
              <View style={styles.bottomSheetHeader}>
                <TextInput
                  style={styles.searchBar}
                  placeholder="Search exercise..."
                  placeholderTextColor="#666666"
                  value={searchQuery}
                  onChangeText={setSearchQuery}
                />
                <TouchableOpacity onPress={() => setExerciseModalVisible(false)} style={styles.closeBtn}>
                  <Text style={styles.closeBtnText}>Close</Text>
                </TouchableOpacity>
              </View>

              <ScrollView style={styles.exerciseListScroll}>
                {recentExercises.length > 0 && !searchQuery && (
                  <View>
                    <Text style={styles.groupTitle}>RECENT</Text>
                    {recentExercises.map(ex => (
                      <TouchableOpacity
                        key={`recent-${ex}`}
                        style={[styles.exerciseRow, exercise === ex && styles.exerciseRowSelected]}
                        onPress={() => handleSelectExercise(ex)}
                      >
                        <Text style={[styles.exerciseRowText, exercise === ex && styles.exerciseRowTextSelected]}>
                          {formatEnumStr(ex)}
                        </Text>
                      </TouchableOpacity>
                    ))}
                  </View>
                )}

                {groupedExercises[category].map(group => {
                  const filtered = group.data.filter(ex => formatEnumStr(ex).toLowerCase().includes(searchQuery.toLowerCase()));
                  if (filtered.length === 0) return null;
                  
                  return (
                    <View key={group.title}>
                      <Text style={styles.groupTitle}>{group.title}</Text>
                      {filtered.map(ex => (
                         <TouchableOpacity
                           key={ex}
                           style={[styles.exerciseRow, exercise === ex && styles.exerciseRowSelected]}
                           onPress={() => handleSelectExercise(ex)}
                         >
                           <Text style={[styles.exerciseRowText, exercise === ex && styles.exerciseRowTextSelected]}>
                             {formatEnumStr(ex)}
                           </Text>
                         </TouchableOpacity>
                      ))}
                    </View>
                  );
                })}
              </ScrollView>
            </View>
          </View>
        </Modal>
      </View>
    </AppLayout>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: Spacing.lg,
  },
  scroll: {
    paddingBottom: Spacing.xl,
  },
  errorText: {
    color: ThemeColors.error,
    fontSize: 14,
    textAlign: 'center',
    marginBottom: Spacing.md,
  },
  summaryCard: {
    marginBottom: Spacing.md,
  },
  cardHeading: {
    ...Typography.subheader,
    marginBottom: Spacing.md,
  },
  workoutSummaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    paddingBottom: Spacing.sm,
  },
  summaryItem: {
    flex: 1,
    alignItems: 'center',
  },
  summaryLabel: {
    ...Typography.bodySmall,
    color: ThemeColors.textSecondary,
    textAlign: 'center',
    marginTop: 4,
  },
  summaryVal: {
    ...Typography.headingMedium,
    color: ThemeColors.textPrimary,
    textAlign: 'center',
    fontWeight: '600',
  },
  summaryValWhite: {
    ...Typography.headingMedium,
    color: '#FFFFFF',
    textAlign: 'center',
    fontWeight: '600',
  },
  verticalDivider: {
    width: 1,
    backgroundColor: ThemeColors.border,
    height: '100%',
  },
  logActionContainer: {
    marginVertical: Spacing.md,
  },
  sectionTitle: {
    ...Typography.subheader,
    marginTop: Spacing.md,
    marginBottom: Spacing.sm,
  },
  emptyText: {
    ...Typography.bodyMedium,
    color: ThemeColors.textMuted,
    textAlign: 'center',
    marginVertical: Spacing.xl,
  },
  workoutCard: {
    marginVertical: 4,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  workoutTitle: {
    ...Typography.bodyLarge,
    fontWeight: 'bold',
  },
  workoutDate: {
    fontSize: 11,
    color: ThemeColors.textMuted,
    marginTop: 2,
  },
  workoutBurn: {
    ...Typography.bodyLarge,
    color: '#FFFFFF',
    fontWeight: 'bold',
  },
  cardFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: 'rgba(255, 255, 255, 0.04)',
    marginTop: Spacing.sm,
    paddingTop: Spacing.sm,
  },
  workoutDuration: {
    ...Typography.bodySmall,
    color: ThemeColors.textSecondary,
  },
  actionRow: {
    flexDirection: 'row',
  },
  actionBtn: {
    paddingLeft: Spacing.md,
  },
  actionTextEdit: {
    color: '#FFFFFF',
    fontSize: 13,
    fontWeight: '600',
  },
  actionTextDelete: {
    color: ThemeColors.error,
    fontSize: 13,
    fontWeight: '600',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.85)',
    justifyContent: 'center',
  },
  modalScroll: {
    flexGrow: 1,
    justifyContent: 'center',
    padding: Spacing.lg,
  },
  modalCard: {
    padding: Spacing.lg,
  },
  modalTitle: {
    ...Typography.headingMedium,
    color: '#FFFFFF',
    fontWeight: 'bold',
    marginBottom: Spacing.md,
    textAlign: 'center',
  },
  fieldLabel: {
    ...Typography.bodySmall,
    color: ThemeColors.textSecondary,
    marginBottom: Spacing.xs,
    marginLeft: 4,
  },
  pillContainer: {
    flexDirection: 'row',
    marginBottom: Spacing.md,
  },
  pill: {
    paddingHorizontal: Spacing.lg,
    paddingVertical: 10,
    borderRadius: 20,
    backgroundColor: '#262626',
    marginRight: Spacing.sm,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.05)',
  },
  pillActive: {
    backgroundColor: '#FFFFFF',
    borderColor: '#FFFFFF',
  },
  pillText: {
    color: '#B8B8B8',
    fontSize: 13,
    fontWeight: '600',
  },
  pillTextActive: {
    color: '#000000',
  },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  estimateContainer: {
    backgroundColor: '#262626',
    borderRadius: 12,
    padding: Spacing.md,
    alignItems: 'center',
    marginTop: Spacing.sm,
  },
  estimateLabel: {
    ...Typography.bodySmall,
    color: ThemeColors.textSecondary,
    marginBottom: 4,
  },
  estimateValue: {
    ...Typography.headingLarge,
    color: '#FFFFFF',
  },
  modalButtonRow: {
    flexDirection: 'row',
    marginTop: Spacing.xl,
  },
  selectorField: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: '#111111',
    borderRadius: 12,
    padding: Spacing.md,
    marginBottom: Spacing.md,
  },
  selectorText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '500',
  },
  selectorArrow: {
    color: '#666666',
    fontSize: 12,
  },
  bottomSheetOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'flex-end',
  },
  bottomSheetContainer: {
    backgroundColor: '#111111',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    height: '90%',
    padding: Spacing.md,
  },
  bottomSheetHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: Spacing.md,
  },
  searchBar: {
    flex: 1,
    backgroundColor: '#262626',
    borderRadius: 12,
    padding: Spacing.md,
    color: '#FFFFFF',
    fontSize: 16,
  },
  closeBtn: {
    marginLeft: Spacing.md,
  },
  closeBtnText: {
    color: '#FFFFFF',
    fontSize: 16,
  },
  exerciseListScroll: {
    flex: 1,
  },
  groupTitle: {
    color: '#666666',
    fontSize: 12,
    fontWeight: 'bold',
    marginTop: Spacing.lg,
    marginBottom: Spacing.sm,
    letterSpacing: 1,
  },
  exerciseRow: {
    paddingVertical: Spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255,255,255,0.05)',
  },
  exerciseRowSelected: {
    backgroundColor: '#FFFFFF',
    borderRadius: 8,
    borderBottomWidth: 0,
    paddingHorizontal: Spacing.sm,
  },
  exerciseRowText: {
    color: '#FFFFFF',
    fontSize: 16,
  },
  exerciseRowTextSelected: {
    color: '#000000',
    fontWeight: 'bold',
  },
});
