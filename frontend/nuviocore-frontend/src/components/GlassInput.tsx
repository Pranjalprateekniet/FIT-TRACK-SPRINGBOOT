import React, { useState } from 'react';
import { StyleSheet, Text, TextInput, TextInputProps, View } from 'react-native';
import { ThemeColors, Spacing, Typography } from '../theme/Theme';

interface GlassInputProps extends TextInputProps {
  label?: string;
  error?: string;
}

export const GlassInput: React.FC<GlassInputProps> = ({ label, error, style, ...props }) => {
  const [isFocused, setIsFocused] = useState(false);

  return (
    <View style={styles.container}>
      {label && <Text style={styles.label}>{label}</Text>}
      <TextInput
        style={[
          styles.input,
          isFocused && styles.inputFocused,
          error && styles.inputError,
          style
        ]}
        placeholderTextColor="#7C7C7C"
        onFocus={() => setIsFocused(true)}
        onBlur={() => setIsFocused(false)}
        {...props}
      />
      {error && <Text style={styles.errorText}>{error}</Text>}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginBottom: Spacing.md,
  },
  label: {
    ...Typography.bodySmall,
    color: ThemeColors.textSecondary,
    marginBottom: Spacing.xs,
    marginLeft: 4,
  },
  input: {
    ...Typography.bodyLarge,
    color: '#FFFFFF',
    backgroundColor: '#181818',
    borderRadius: 16,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.08)',
    paddingHorizontal: Spacing.lg,
    paddingVertical: 14,
    minHeight: 52,
  },
  inputFocused: {
    borderColor: '#FFFFFF',
  },
  inputError: {
    borderColor: ThemeColors.error,
  },
  errorText: {
    ...Typography.bodySmall,
    color: ThemeColors.error,
    marginTop: Spacing.xs,
    marginLeft: 4,
  },
});
