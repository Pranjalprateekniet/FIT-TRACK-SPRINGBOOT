export const ThemeColors = {
  background: '#0A0A0A',
  surface: '#121212',
  surfaceGlass: '#1A1A1A',
  border: 'rgba(255, 255, 255, 0.08)',
  
  textPrimary: '#FFFFFF',
  textSecondary: '#B8B8B8',
  textMuted: '#808080',
  
  error: '#FF453A',
  success: '#30D158',
};

// Preserved for Onboarding/Sign Up flows to maintain their existing visual identity
export const LegacyThemeColors = {
  background: '#0E0E10',
  surface: '#1A1A1E',
  surfaceGlass: 'rgba(26, 26, 30, 0.75)',
  border: 'rgba(255, 255, 255, 0.08)',
  
  textPrimary: '#F2F2F7',
  textSecondary: '#8E8E93',
  textMuted: '#48484A',
  
  accentBronze: '#C5A073',
  accentBronzeGlass: 'rgba(197, 160, 115, 0.15)',
  
  error: '#FF453A',
  success: '#30D158',
};

export const Spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  xxl: 32,
  xxxl: 48,
};

export const Typography = {
  headingLarge: {
    fontSize: 28,
    fontWeight: 'bold' as const,
    letterSpacing: -0.5,
    color: ThemeColors.textPrimary,
  },
  headingMedium: {
    fontSize: 20,
    fontWeight: '600' as const,
    letterSpacing: -0.2,
    color: ThemeColors.textPrimary,
  },
  subheader: {
    fontSize: 12,
    fontWeight: '600' as const,
    letterSpacing: 1.5,
    textTransform: 'uppercase' as const,
    color: ThemeColors.textSecondary,
  },
  bodyLarge: {
    fontSize: 16,
    fontWeight: 'normal' as const,
    letterSpacing: 0,
    color: ThemeColors.textPrimary,
  },
  bodyMedium: {
    fontSize: 14,
    fontWeight: 'normal' as const,
    letterSpacing: 0,
    color: ThemeColors.textPrimary,
  },
  bodySmall: {
    fontSize: 12,
    fontWeight: 'normal' as const,
    letterSpacing: 0,
    color: ThemeColors.textSecondary,
  },
};
