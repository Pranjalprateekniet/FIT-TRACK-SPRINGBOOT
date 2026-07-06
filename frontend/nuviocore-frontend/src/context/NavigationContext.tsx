import React, { createContext, useContext, useState } from 'react';

export type ScreenType = 
  | 'SPLASH' 
  | 'LOGIN' 
  | 'REGISTER' 
  | 'VERIFY' 
  | 'GOAL_SETUP' 
  | 'FOOD' 
  | 'FOOD_SEARCH' 
  | 'FOOD_INSIGHTS'
  | 'WORKOUTS' 
  | 'PROFILE';

interface NavigationContextType {
  currentScreen: ScreenType;
  navigateTo: (screen: ScreenType) => void;
}

const NavigationContext = createContext<NavigationContextType | undefined>(undefined);

export const NavigationProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [currentScreen, setCurrentScreen] = useState<ScreenType>('SPLASH');

  const navigateTo = (screen: ScreenType) => {
    setCurrentScreen(screen);
  };

  return (
    <NavigationContext.Provider value={{ currentScreen, navigateTo }}>
      {children}
    </NavigationContext.Provider>
  );
};

export const useNavigation = () => {
  const context = useContext(NavigationContext);
  if (!context) {
    throw new Error('useNavigation must be used within a NavigationProvider');
  }
  return context;
};
