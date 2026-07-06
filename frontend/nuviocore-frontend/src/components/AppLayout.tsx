import React from 'react';
import { StyleSheet, View, TouchableOpacity, SafeAreaView, Platform, Text } from 'react-native';
import { useNavigation, ScreenType } from '../context/NavigationContext';
import { ThemeColors, Spacing, Typography } from '../theme/Theme';
import { Ionicons } from '@expo/vector-icons';

interface AppLayoutProps {
  children: React.ReactNode;
}

const FOOD_SCREENS: ScreenType[] = ['FOOD', 'FOOD_SEARCH', 'FOOD_INSIGHTS'];

const getDayLabel = () => {
  const now = new Date();
  const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  return `${days[now.getDay()]}, ${months[now.getMonth()]} ${now.getDate()}`;
};

export const AppLayout: React.FC<AppLayoutProps> = ({ children }) => {
  const { currentScreen, navigateTo } = useNavigation();

  const isFoodContext = FOOD_SCREENS.includes(currentScreen);

  const tabs: { icon: keyof typeof Ionicons.glyphMap; screen: ScreenType }[] = [
    { icon: 'restaurant-outline', screen: 'FOOD' },
    { icon: 'barbell-outline', screen: 'WORKOUTS' },
    { icon: 'person-outline', screen: 'PROFILE' },
  ];

  const renderHeader = () => {
    if (isFoodContext) {
      return (
        <View style={styles.header}>
          <View style={styles.foodHeaderLeft}>
            <Text style={styles.foodHeaderDate}>{getDayLabel()}</Text>
            <Text style={styles.foodHeaderTitle}>Food Dashboard</Text>
          </View>
          <TouchableOpacity style={styles.settingsButton} activeOpacity={0.7}>
            <Ionicons name="settings-outline" size={20} color={ThemeColors.textMuted} />
          </TouchableOpacity>
        </View>
      );
    }
    return (
      <View style={styles.header}>
        <Text style={styles.logoText}>NUVIOCORE</Text>
      </View>
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      {renderHeader()}
      
      <View style={styles.content}>
        {children}
      </View>

      <View style={styles.tabBar}>
        {tabs.map((tab) => {
          const isActive = currentScreen === tab.screen || 
            (tab.screen === 'FOOD' && FOOD_SCREENS.includes(currentScreen));
          return (
            <TouchableOpacity
              key={tab.screen}
              style={styles.tabItem}
              onPress={() => navigateTo(tab.screen)}
              activeOpacity={0.8}
            >
              <Ionicons 
                name={tab.icon} 
                size={24} 
                color={isActive ? '#FFFFFF' : '#6E6E6E'} 
              />
            </TouchableOpacity>
          );
        })}
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: ThemeColors.background,
    paddingTop: Platform.OS === 'android' ? 30 : 0,
  },
  header: {
    height: 56,
    borderBottomWidth: 1,
    borderBottomColor: ThemeColors.border,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#111111',
    flexDirection: 'row',
    paddingHorizontal: Spacing.lg,
  },
  // Default header — wordmark centred
  logoText: {
    ...Typography.subheader,
    color: '#FFFFFF',
    fontWeight: 'bold',
    letterSpacing: 2,
  },
  // Food context header
  foodHeaderLeft: {
    flex: 1,
    justifyContent: 'center',
  },
  foodHeaderDate: {
    fontSize: 11,
    fontWeight: '500',
    color: ThemeColors.textMuted,
    letterSpacing: 0.5,
    textTransform: 'uppercase',
  },
  foodHeaderTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: ThemeColors.textPrimary,
    letterSpacing: -0.2,
    marginTop: 1,
  },
  settingsButton: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: 'rgba(255,255,255,0.05)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  // Shared
  content: {
    flex: 1,
    backgroundColor: ThemeColors.background,
  },
  tabBar: {
    height: 60,
    flexDirection: 'row',
    borderTopWidth: 1,
    borderTopColor: 'rgba(255, 255, 255, 0.05)',
    backgroundColor: '#111111',
    alignItems: 'center',
    justifyContent: 'space-around',
    paddingBottom: Platform.OS === 'ios' ? 10 : 0,
  },
  tabItem: {
    alignItems: 'center',
    justifyContent: 'center',
    flex: 1,
    height: '100%',
  },
});
