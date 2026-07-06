import React from 'react';
import { StyleSheet, Text, TouchableOpacity, TouchableOpacityProps, ActivityIndicator } from 'react-native';
import { ThemeColors, Spacing, Typography } from '../theme/Theme';

interface ButtonProps extends TouchableOpacityProps {
  title: string;
  loading?: boolean;
}

export const PrimaryButton: React.FC<ButtonProps> = ({ title, loading, style, disabled, ...props }) => {
  return (
    <TouchableOpacity
      style={[
        styles.primaryBtn,
        disabled && styles.disabledBtn,
        style
      ]}
      activeOpacity={0.7}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? (
        <ActivityIndicator color="#000000" />
      ) : (
        <Text style={[styles.primaryBtnText, disabled && styles.disabledBtnText]}>{title}</Text>
      )}
    </TouchableOpacity>
  );
};

export const SecondaryButton: React.FC<ButtonProps> = ({ title, loading, style, disabled, ...props }) => {
  return (
    <TouchableOpacity
      style={[
        styles.secondaryBtn,
        disabled && styles.disabledSecondaryBtn,
        style
      ]}
      activeOpacity={0.7}
      disabled={disabled || loading}
      {...props}
    >
      {loading ? (
        <ActivityIndicator color="#FFFFFF" />
      ) : (
        <Text style={[styles.secondaryBtnText, disabled && styles.disabledSecondaryBtnText]}>{title}</Text>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  primaryBtn: {
    backgroundColor: '#FFFFFF',
    borderRadius: 16,
    paddingVertical: 16,
    paddingHorizontal: Spacing.xl,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 56,
  },
  primaryBtnText: {
    ...Typography.bodyLarge,
    color: '#000000',
    fontWeight: 'bold',
  },
  disabledBtn: {
    backgroundColor: '#333333',
  },
  disabledBtnText: {
    color: '#666666',
  },
  secondaryBtn: {
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.12)',
    borderRadius: 16,
    paddingVertical: 16,
    paddingHorizontal: Spacing.xl,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 56,
  },
  secondaryBtnText: {
    ...Typography.bodyLarge,
    color: '#FFFFFF',
    fontWeight: 'bold',
  },
  disabledSecondaryBtn: {
    borderColor: 'rgba(255, 255, 255, 0.05)',
  },
  disabledSecondaryBtnText: {
    color: '#666666',
  },
});
