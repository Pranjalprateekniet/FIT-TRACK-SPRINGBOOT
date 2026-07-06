import React from 'react';
import { StyleSheet, Text, TouchableOpacity, TouchableOpacityProps, ActivityIndicator } from 'react-native';
import { LegacyThemeColors as ThemeColors, Spacing, Typography } from '../theme/Theme';

interface ButtonProps extends TouchableOpacityProps {
  title: string;
  loading?: boolean;
}

export const PrimaryButton: React.FC<ButtonProps> = ({ title, loading, style, disabled, ...props }) => {
  return (
    <TouchableOpacity
      style={[
        styles.primaryButton,
        disabled && styles.disabledButton,
        style
      ]}
      disabled={disabled || loading}
      activeOpacity={0.8}
      {...props}
    >
      {loading ? (
        <ActivityIndicator color="#0E0E10" size="small" />
      ) : (
        <Text style={styles.primaryText}>{title}</Text>
      )}
    </TouchableOpacity>
  );
};

export const SecondaryButton: React.FC<ButtonProps> = ({ title, loading, style, disabled, ...props }) => {
  return (
    <TouchableOpacity
      style={[
        styles.secondaryButton,
        disabled && styles.disabledButton,
        style
      ]}
      disabled={disabled || loading}
      activeOpacity={0.8}
      {...props}
    >
      {loading ? (
        <ActivityIndicator color={ThemeColors.textPrimary} size="small" />
      ) : (
        <Text style={styles.secondaryText}>{title}</Text>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  primaryButton: {
    backgroundColor: ThemeColors.accentBronze,
    borderRadius: 12,
    paddingVertical: Spacing.md,
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: Spacing.sm,
    width: '100%',
    height: 50,
  },
  primaryText: {
    color: '#0E0E10',
    fontSize: 16,
    fontWeight: 'bold',
    letterSpacing: 1.0,
    textTransform: 'uppercase',
  },
  secondaryButton: {
    backgroundColor: 'transparent',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: ThemeColors.border,
    paddingVertical: Spacing.md,
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: Spacing.sm,
    width: '100%',
    height: 50,
  },
  secondaryText: {
    color: ThemeColors.textPrimary,
    fontSize: 16,
    fontWeight: '600',
    letterSpacing: 1.0,
    textTransform: 'uppercase',
  },
  disabledButton: {
    opacity: 0.5,
  },
});
