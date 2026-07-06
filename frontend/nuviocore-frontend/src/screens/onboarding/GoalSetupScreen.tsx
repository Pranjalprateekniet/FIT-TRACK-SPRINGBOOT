import React, { useState } from 'react';
import { StyleSheet, Text, View, ScrollView, TouchableOpacity, Alert } from 'react-native';
import { useNavigation } from '../../context/NavigationContext';
import { fetchClient } from '../../api/FetchClient';
import { GlassCard } from '../../components/LegacyGlassCard';
import { GlassInput } from '../../components/LegacyGlassInput';
import { PrimaryButton, SecondaryButton } from '../../components/LegacyButtons';
import { LegacyThemeColors as ThemeColors, Spacing, Typography } from '../../theme/Theme';
import { CreateGoalRequest, GoalResponse, GoalType, GoalPace, Gender, ActivityLevel } from '../../types';

export const GoalSetupScreen = () => {
  const { navigateTo } = useNavigation();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Form Fields
  const [goalType, setGoalType] = useState<GoalType>('WEIGHT_LOSS');
  const [goalPace, setGoalPace] = useState<GoalPace>('MODERATE');
  const [gender, setGender] = useState<Gender>('MALE');
  const [activityLevel, setActivityLevel] = useState<ActivityLevel>('MODERATELY_ACTIVE');
  const [weightKg, setWeightKg] = useState('70');
  const [heightCm, setHeightCm] = useState('170');
  const [targetWeightKg, setTargetWeightKg] = useState('65');
  const [age, setAge] = useState('25');

  // Preview Result State
  const [preview, setPreview] = useState<GoalResponse | null>(null);

  // Validate current step fields
  const validateStep = (): boolean => {
    setError(null);
    const weightNum = parseFloat(weightKg);
    const targetWeightNum = parseFloat(targetWeightKg);
    const heightNum = parseFloat(heightCm);
    const ageNum = parseInt(age, 10);

    if (step === 3) {
      if (isNaN(weightNum) || weightNum < 30 || weightNum > 300) {
        setError("Weight must be between 30 and 300 kg");
        return false;
      }
      if (isNaN(heightNum) || heightNum < 100 || heightNum > 250) {
        setError("Height must be between 100 and 250 cm");
        return false;
      }
      if (isNaN(targetWeightNum) || targetWeightNum < 30 || targetWeightNum > 300) {
        setError("Target weight must be between 30 and 300 kg");
        return false;
      }
      if (isNaN(ageNum) || ageNum < 13 || ageNum > 100) {
        setError("Age must be between 13 and 100 years");
        return false;
      }

      // Backend logic validation mirrors
      if (goalType === 'WEIGHT_LOSS' && targetWeightNum >= weightNum) {
        setError("For weight loss, target weight must be less than current weight");
        return false;
      }
      if (goalType === 'MUSCLE_GAIN' && targetWeightNum <= weightNum) {
        setError("For muscle gain, target weight must be greater than current weight");
        return false;
      }
      if (goalType === 'MAINTENANCE' && weightNum !== targetWeightNum) {
        setError("For maintenance, current and target weight must be identical");
        return false;
      }
      if (goalType === 'MUSCLE_GAIN' && (targetWeightNum - weightNum) > 30) {
        setError("Unrealistic target: weight gain goal should be under 30kg");
        return false;
      }
      const weightDiff = Math.abs(weightNum - targetWeightNum);
      if (weightDiff < 5 && goalPace === 'EXTREME') {
        setError("Extreme pace is not recommended for small weight changes (<5kg)");
        return false;
      }
      const targetHeightM = heightNum / 100;
      const targetBMI = targetWeightNum / (targetHeightM * targetHeightM);
      if (targetBMI < 18.5) {
        setError(`Target BMI (${targetBMI.toFixed(1)}) is unhealthy (<18.5). Increase target weight.`);
        return false;
      }
    }
    return true;
  };

  const handleNext = () => {
    if (validateStep()) {
      setStep(step + 1);
    }
  };

  const handleBack = () => {
    setError(null);
    setStep(step - 1);
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    const payload: CreateGoalRequest = {
      goalType,
      goalPace,
      gender,
      activityLevel,
      weightKg: parseFloat(weightKg),
      heightCm: parseFloat(heightCm),
      targetWeightKg: parseFloat(targetWeightKg),
      age: parseInt(age, 10),
    };

    try {
      const response: GoalResponse = await fetchClient('/api/goals', {
        method: 'POST',
        body: payload
      });
      setPreview(response);
      setStep(4);
    } catch (err: any) {
      setError(err.message || 'Failed to calculate goal');
      // If it throws extreme muscle gain exception, handle it
      Alert.alert("Calculation Error", err.message || "Failed to save goal configuration.");
    } finally {
      setLoading(false);
    }
  };

  const renderStep = () => {
    switch (step) {
      case 1:
        return (
          <View>
            <Text style={styles.sectionTitle}>What is your objective?</Text>
            <View style={styles.optionRow}>
              {(['WEIGHT_LOSS', 'MAINTENANCE', 'MUSCLE_GAIN'] as GoalType[]).map((t) => (
                <TouchableOpacity
                  key={t}
                  style={[styles.optionCard, goalType === t && styles.optionCardActive]}
                  onPress={() => {
                    setGoalType(t);
                    // Autofill target weight logically
                    if (t === 'MAINTENANCE') setTargetWeightKg(weightKg);
                  }}
                >
                  <Text style={[styles.optionText, goalType === t && styles.optionTextActive]}>
                    {t.replace('_', ' ')}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            {goalType !== 'MAINTENANCE' && (
              <>
                <Text style={[styles.sectionTitle, { marginTop: Spacing.xl }]}>Select your pace</Text>
                <View style={styles.optionRowVertical}>
                  {(['EASY', 'MODERATE', 'AGGRESSIVE', 'EXTREME'] as GoalPace[]).map((p) => {
                    if (goalType === 'MUSCLE_GAIN' && p === 'EXTREME') return null; // Backend throws exception
                    return (
                      <TouchableOpacity
                        key={p}
                        style={[styles.optionCardHorizontal, goalPace === p && styles.optionCardActive]}
                        onPress={() => setGoalPace(p)}
                      >
                        <Text style={[styles.optionText, goalPace === p && styles.optionTextActive]}>
                          {p}
                        </Text>
                        <Text style={styles.optionSubtext}>
                          {p === 'EASY' && '0.25 kg/week - Sustainable'}
                          {p === 'MODERATE' && '0.50 kg/week - Recommended'}
                          {p === 'AGGRESSIVE' && '0.75 kg/week - Hard'}
                          {p === 'EXTREME' && '1.00 kg/week - Aggressive'}
                        </Text>
                      </TouchableOpacity>
                    );
                  })}
                </View>
              </>
            )}
            <PrimaryButton title="Continue" onPress={handleNext} />
          </View>
        );
      case 2:
        return (
          <View>
            <Text style={styles.sectionTitle}>Biological Information</Text>
            <View style={styles.optionRow}>
              {(['MALE', 'FEMALE'] as Gender[]).map((g) => (
                <TouchableOpacity
                  key={g}
                  style={[styles.optionCard, gender === g && styles.optionCardActive]}
                  onPress={() => setGender(g)}
                >
                  <Text style={[styles.optionText, gender === g && styles.optionTextActive]}>{g}</Text>
                </TouchableOpacity>
              ))}
            </View>

            <Text style={[styles.sectionTitle, { marginTop: Spacing.xl }]}>Activity Profile</Text>
            <View style={styles.optionRowVertical}>
              {(['SEDENTARY', 'LIGHTLY_ACTIVE', 'MODERATELY_ACTIVE', 'VERY_ACTIVE', 'HYPER_ACTIVE'] as ActivityLevel[]).map((a) => (
                <TouchableOpacity
                  key={a}
                  style={[styles.optionCardHorizontal, activityLevel === a && styles.optionCardActive]}
                  onPress={() => setActivityLevel(a)}
                >
                  <Text style={[styles.optionText, activityLevel === a && styles.optionTextActive]}>
                    {a.replace('_', ' ')}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            <View style={styles.buttonRow}>
              <View style={{ flex: 1, marginRight: Spacing.sm }}>
                <SecondaryButton title="Back" onPress={handleBack} />
              </View>
              <View style={{ flex: 1, marginLeft: Spacing.sm }}>
                <PrimaryButton title="Continue" onPress={handleNext} />
              </View>
            </View>
          </View>
        );
      case 3:
        return (
          <View>
            <Text style={styles.sectionTitle}>Measurements</Text>
            {error && <Text style={styles.errorText}>{error}</Text>}
            
            <GlassInput
              label="Age (Years)"
              placeholder="e.g. 25"
              keyboardType="number-pad"
              value={age}
              onChangeText={(val) => {
                setAge(val);
                setError(null);
              }}
            />

            <GlassInput
              label="Height (cm)"
              placeholder="e.g. 175"
              keyboardType="number-pad"
              value={heightCm}
              onChangeText={(val) => {
                setHeightCm(val);
                setError(null);
              }}
            />

            <GlassInput
              label="Current Weight (kg)"
              placeholder="e.g. 78.5"
              keyboardType="decimal-pad"
              value={weightKg}
              onChangeText={(val) => {
                setWeightKg(val);
                setError(null);
                if (goalType === 'MAINTENANCE') setTargetWeightKg(val);
              }}
            />

            {goalType !== 'MAINTENANCE' && (
              <GlassInput
                label="Target Weight (kg)"
                placeholder="e.g. 70"
                keyboardType="decimal-pad"
                value={targetWeightKg}
                onChangeText={(val) => {
                  setTargetWeightKg(val);
                  setError(null);
                }}
              />
            )}

            <View style={styles.buttonRow}>
              <View style={{ flex: 1, marginRight: Spacing.sm }}>
                <SecondaryButton title="Back" onPress={handleBack} />
              </View>
              <View style={{ flex: 1, marginLeft: Spacing.sm }}>
                <PrimaryButton title="Calculate Plan" loading={loading} onPress={handleSubmit} />
              </View>
            </View>
          </View>
        );
      case 4:
        return (
          <View>
            <Text style={styles.sectionTitle}>Your Personalized Plan</Text>
            <Text style={styles.sectionSubtitle}>Calculated successfully from physical parameters.</Text>
            
            {preview && (
              <GlassCard style={styles.previewCard}>
                <Text style={styles.previewHeading}>Daily Targets</Text>
                
                <View style={styles.macroRow}>
                  <Text style={styles.macroLabel}>Calories</Text>
                  <Text style={styles.macroValueBronze}>{preview.targetCalories} kcal</Text>
                </View>
                
                <View style={styles.divider} />
                
                <View style={styles.macroRow}>
                  <Text style={styles.macroLabel}>Protein</Text>
                  <Text style={styles.macroValue}>{preview.targetProtein} g</Text>
                </View>
                <View style={styles.macroRow}>
                  <Text style={styles.macroLabel}>Carbohydrates</Text>
                  <Text style={styles.macroValue}>{preview.targetCarbohydrates} g</Text>
                </View>
                <View style={styles.macroRow}>
                  <Text style={styles.macroLabel}>Fat</Text>
                  <Text style={styles.macroValue}>{preview.targetFat} g</Text>
                </View>

                <View style={styles.divider} />

                <View style={styles.macroRow}>
                  <Text style={styles.macroLabel}>Calculated BMI</Text>
                  <Text style={styles.macroValue}>
                    {preview.bmi.toFixed(1)} ({preview.bmiCategory})
                  </Text>
                </View>
                {preview.estimatedWeeksToGoal > 0 && (
                  <View style={styles.macroRow}>
                    <Text style={styles.macroLabel}>Est. Weeks to Goal</Text>
                    <Text style={styles.macroValue}>{preview.estimatedWeeksToGoal} weeks</Text>
                  </View>
                )}
              </GlassCard>
            )}

            <PrimaryButton title="Launch Dashboard" onPress={() => navigateTo('FOOD')} />
          </View>
        );
      default:
        return null;
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>PERSONAL PLAN</Text>
        <Text style={styles.stepIndicator}>Step {step} of 4</Text>
      </View>
      <GlassCard style={styles.card}>
        {renderStep()}
      </GlassCard>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    backgroundColor: ThemeColors.background,
    padding: Spacing.lg,
    paddingTop: 50,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: Spacing.lg,
  },
  headerTitle: {
    ...Typography.subheader,
    color: ThemeColors.accentBronze,
  },
  stepIndicator: {
    ...Typography.bodySmall,
    color: ThemeColors.textSecondary,
  },
  card: {
    padding: Spacing.lg,
  },
  sectionTitle: {
    ...Typography.headingMedium,
    marginBottom: Spacing.md,
  },
  sectionSubtitle: {
    ...Typography.bodySmall,
    color: ThemeColors.textSecondary,
    marginBottom: Spacing.lg,
  },
  optionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: Spacing.xs,
  },
  optionCard: {
    flex: 1,
    borderWidth: 1,
    borderColor: ThemeColors.border,
    borderRadius: 8,
    paddingVertical: Spacing.md,
    alignItems: 'center',
    justifyContent: 'center',
    marginHorizontal: 4,
    backgroundColor: 'rgba(255, 255, 255, 0.02)',
  },
  optionCardActive: {
    borderColor: ThemeColors.accentBronze,
    backgroundColor: ThemeColors.accentBronzeGlass,
  },
  optionText: {
    ...Typography.bodyMedium,
    color: ThemeColors.textSecondary,
    textTransform: 'capitalize',
  },
  optionTextActive: {
    color: ThemeColors.textPrimary,
    fontWeight: 'bold',
  },
  optionRowVertical: {
    marginVertical: Spacing.xs,
  },
  optionCardHorizontal: {
    borderWidth: 1,
    borderColor: ThemeColors.border,
    borderRadius: 8,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.md,
    marginVertical: 4,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.02)',
  },
  optionSubtext: {
    fontSize: 10,
    color: ThemeColors.textSecondary,
  },
  buttonRow: {
    flexDirection: 'row',
    marginTop: Spacing.lg,
  },
  errorText: {
    color: ThemeColors.error,
    fontSize: 14,
    textAlign: 'center',
    marginBottom: Spacing.md,
  },
  previewCard: {
    marginVertical: Spacing.md,
    borderColor: ThemeColors.accentBronze,
  },
  previewHeading: {
    ...Typography.subheader,
    color: ThemeColors.textSecondary,
    marginBottom: Spacing.md,
  },
  macroRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: Spacing.xs,
  },
  macroLabel: {
    ...Typography.bodyLarge,
    color: ThemeColors.textSecondary,
  },
  macroValue: {
    ...Typography.bodyLarge,
    fontWeight: '600',
  },
  macroValueBronze: {
    ...Typography.bodyLarge,
    color: ThemeColors.accentBronze,
    fontWeight: 'bold',
  },
  divider: {
    height: 1,
    backgroundColor: ThemeColors.border,
    marginVertical: Spacing.sm,
  },
});
