import React, { useState, useEffect } from 'react';
import { StyleSheet, Text, View, ScrollView, Alert } from 'react-native';
import { AppLayout } from '../../components/AppLayout';
import { GlassCard } from '../../components/GlassCard';
import { PrimaryButton, SecondaryButton } from '../../components/Buttons';
import { SectionHeader } from '../../components/SectionHeader';
import { useAuth } from '../../context/AuthContext';
import { useNavigation } from '../../context/NavigationContext';
import { fetchClient } from '../../api/FetchClient';
import { ThemeColors, Spacing, Typography } from '../../theme/Theme';
import { GoalResponse } from '../../types';

export const ProfileScreen = () => {
  const { user, logout } = useAuth();
  const { navigateTo } = useNavigation();
  const [goal, setGoal] = useState<GoalResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchProfileGoal = async () => {
    try {
      const goalData: GoalResponse = await fetchClient('/api/goals');
      setGoal(goalData);
    } catch (err: any) {
      console.log("Profile goal load failure (might not have goal setup):", err);
      // If goal not found (e.g. 500 error / not found), redirect to Goal Setup
      if (err.message.includes("Goal not found")) {
        navigateTo('GOAL_SETUP');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfileGoal();
  }, []);

  const handleResetGoal = () => {
    Alert.alert(
      "Reset Target Goals",
      "This will permanently delete your calculated target macros and physical statistics. You will need to complete onboarding setup again.",
      [
        { text: "Cancel", style: "cancel" },
        {
          text: "Reset Plan",
          style: "destructive",
          onPress: async () => {
            try {
              await fetchClient('/api/goals', { method: 'DELETE' });
              setGoal(null);
              navigateTo('GOAL_SETUP');
            } catch (err: any) {
              Alert.alert("Reset Failed", err.message || "Failed to remove active plan.");
            }
          }
        }
      ]
    );
  };

  const handleLogout = async () => {
    Alert.alert("Sign Out", "Are you sure you want to log out of Nuviocore?", [
      { text: "Cancel", style: "cancel" },
      {
        text: "Log Out",
        style: "destructive",
        onPress: async () => {
          await logout();
          navigateTo('LOGIN');
        }
      }
    ]);
  };

  return (
    <AppLayout>
      <ScrollView contentContainerStyle={styles.container}>
        <SectionHeader title="Profile" subtitle="Settings & Parameters" />

        <GlassCard>
          <Text style={styles.sectionHeading}>ACCOUNT INFO</Text>
          <View style={styles.detailRow}>
            <Text style={styles.detailLabel}>Email Address</Text>
            <Text style={styles.detailValue}>{user || 'N/A'}</Text>
          </View>
        </GlassCard>

        {goal ? (
          <GlassCard>
            <Text style={styles.sectionHeading}>ACTIVE FITNESS TARGETS</Text>
            
            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>Goal Type</Text>
              <Text style={styles.detailValueBronze}>{goal.goalType.replace('_', ' ')}</Text>
            </View>

            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>Pace Type</Text>
              <Text style={styles.detailValue}>{goal.goalPace}</Text>
            </View>
            
            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>Energy Allowance</Text>
              <Text style={styles.detailValue}>{goal.targetCalories} kcal</Text>
            </View>

            <View style={styles.divider} />
            
            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>Daily Protein</Text>
              <Text style={styles.detailValue}>{goal.targetProtein}g</Text>
            </View>
            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>Daily Carbs</Text>
              <Text style={styles.detailValue}>{goal.targetCarbohydrates}g</Text>
            </View>
            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>Daily Fats</Text>
              <Text style={styles.detailValue}>{goal.targetFat}g</Text>
            </View>

            <View style={styles.divider} />

            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>Weight Targets</Text>
              <Text style={styles.detailValue}>{goal.currentWeightKg}kg {"→"} {goal.targetWeightKg}kg</Text>
            </View>
            <View style={styles.detailRow}>
              <Text style={styles.detailLabel}>Active BMI</Text>
              <Text style={styles.detailValue}>{goal.bmi.toFixed(1)} ({goal.bmiCategory})</Text>
            </View>

            <SecondaryButton
              style={styles.resetBtn}
              title="Reset Calculated Goals"
              onPress={handleResetGoal}
            />
          </GlassCard>
        ) : (
          !loading && (
            <GlassCard>
              <Text style={styles.emptyText}>No calculated physical targets logged yet.</Text>
              <PrimaryButton title="Setup Fitness Goal" onPress={() => navigateTo('GOAL_SETUP')} />
            </GlassCard>
          )
        )}

        <View style={styles.spacer} />
        
        <SecondaryButton title="Log Out" onPress={handleLogout} />
      </ScrollView>
    </AppLayout>
  );
};

const styles = StyleSheet.create({
  container: {
    padding: Spacing.lg,
    paddingBottom: Spacing.xxl,
  },
  sectionHeading: {
    ...Typography.subheader,
    marginBottom: Spacing.md,
  },
  detailRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: Spacing.xs,
  },
  detailLabel: {
    ...Typography.bodyLarge,
    color: ThemeColors.textSecondary,
  },
  detailValue: {
    ...Typography.bodyLarge,
    color: ThemeColors.textPrimary,
    fontWeight: '600',
  },
  detailValueBronze: {
    ...Typography.bodyLarge,
    color: '#FFFFFF',
    fontWeight: 'bold',
    textTransform: 'uppercase',
  },
  divider: {
    height: 1,
    backgroundColor: ThemeColors.border,
    marginVertical: Spacing.sm,
  },
  resetBtn: {
    marginTop: Spacing.lg,
    borderColor: ThemeColors.error,
  },
  emptyText: {
    ...Typography.bodyMedium,
    color: ThemeColors.textMuted,
    textAlign: 'center',
    marginBottom: Spacing.md,
  },
  spacer: {
    height: Spacing.xl,
  },
});
