import React, { useEffect } from 'react';
import { StyleSheet, View, Text, ActivityIndicator } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { AuthProvider, useAuth } from './src/context/AuthContext';
import { NavigationProvider, useNavigation } from './src/context/NavigationContext';
import { NutritionProvider } from './src/context/NutritionContext';
import { ThemeColors } from './src/theme/Theme';

// Import Screens
import { LoginScreen } from './src/screens/auth/LoginScreen';
import { RegisterScreen } from './src/screens/auth/RegisterScreen';
import { VerifyEmailScreen } from './src/screens/auth/VerifyEmailScreen';
import { GoalSetupScreen } from './src/screens/onboarding/GoalSetupScreen';
import { FoodScreen } from './src/screens/nutrition/FoodScreen';
import { FoodSearchScreen } from './src/screens/nutrition/FoodSearchScreen';
import { FoodInsightsScreen } from './src/screens/nutrition/FoodInsightsScreen';
import { WorkoutsScreen } from './src/screens/workouts/WorkoutsScreen';
import { ProfileScreen } from './src/screens/profile/ProfileScreen';
import { fetchClient } from './src/api/FetchClient';

const AppContent = () => {
  const { isAuthenticated, isLoading } = useAuth();
  const { currentScreen, navigateTo } = useNavigation();

  // Route protection sync
  useEffect(() => {
    if (isLoading) return;

    if (!isAuthenticated) {
      if (currentScreen !== 'LOGIN' && currentScreen !== 'REGISTER' && currentScreen !== 'VERIFY') {
        navigateTo('LOGIN');
      }
    } else {
      // Check active goal onboarding requirement
      const verifyUserGoal = async () => {
        try {
          await fetchClient('/api/goals');
          if (
            currentScreen === 'SPLASH' ||
            currentScreen === 'LOGIN' ||
            currentScreen === 'REGISTER' ||
            currentScreen === 'VERIFY'
          ) {
            navigateTo('FOOD');
          }
        } catch (err: any) {
          console.log("Goal verification failure (user has no goal set):", err);
          navigateTo('GOAL_SETUP');
        }
      };
      verifyUserGoal();
    }
  }, [isAuthenticated, isLoading, currentScreen]);

  if (isLoading || currentScreen === 'SPLASH') {
    return (
      <View style={styles.splashContainer}>
        <Text style={styles.logoText}>NUVIOCORE</Text>
        <ActivityIndicator color={'#FFFFFF'} size="large" style={{ marginTop: 24 }} />
        <StatusBar style="light" />
      </View>
    );
  }

  // State Routing Map
  switch (currentScreen) {
    case 'LOGIN':
      return <LoginScreen />;
    case 'REGISTER':
      return <RegisterScreen />;
    case 'VERIFY':
      return <VerifyEmailScreen />;
    case 'GOAL_SETUP':
      return <GoalSetupScreen />;
    case 'FOOD':
      return <FoodScreen />;
    case 'FOOD_SEARCH':
      return <FoodSearchScreen />;
    case 'FOOD_INSIGHTS':
      return <FoodInsightsScreen />;
    case 'WORKOUTS':
      return <WorkoutsScreen />;
    case 'PROFILE':
      return <ProfileScreen />;
    default:
      return <LoginScreen />;
  }
};

export default function App() {
  return (
    <NavigationProvider>
      <AuthProvider>
        <NutritionProvider>
          <AppContent />
        </NutritionProvider>
      </AuthProvider>
    </NavigationProvider>
  );
}

const styles = StyleSheet.create({
  splashContainer: {
    flex: 1,
    backgroundColor: ThemeColors.background,
    alignItems: 'center',
    justifyContent: 'center',
  },
  logoText: {
    fontSize: 32,
    fontWeight: '300',
    color: '#FFFFFF',
    letterSpacing: 6,
  },
});
