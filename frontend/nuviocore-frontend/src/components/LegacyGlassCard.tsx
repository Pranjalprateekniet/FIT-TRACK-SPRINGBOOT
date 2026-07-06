import React from 'react';
import { StyleSheet, View, ViewProps } from 'react-native';
import { LegacyThemeColors as ThemeColors, Spacing } from '../theme/Theme';

interface GlassCardProps extends ViewProps {
  children: React.ReactNode;
}

export const GlassCard: React.FC<GlassCardProps> = ({ children, style, ...props }) => {
  return (
    <View style={[styles.card, style]} {...props}>
      {children}
    </View>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: ThemeColors.surfaceGlass,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: ThemeColors.border,
    padding: Spacing.lg,
    marginVertical: Spacing.sm,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 12,
    elevation: 3,
  },
});
