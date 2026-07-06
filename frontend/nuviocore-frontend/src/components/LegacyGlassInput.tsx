import React, { useState } from 'react';
import { StyleSheet, Text, TextInput, TextInputProps, View } from 'react-native';
import { LegacyThemeColors as ThemeColors, Spacing, Typography } from '../theme/Theme';

interface GlassInputProps extends TextInputProps {
  label?: string;
  error?: string | null;
}

export const GlassInput: React.FC<GlassInputProps> = ({ label, error, style, onFocus, onBlur, ...props }) => {
  const [isFocused, setIsFocused] = useState(false);

  return (
    <View style={styles.container}>
      {label && <Text style={styles.label}>{label}</Text>}
      <TextInput
        style={[
          styles.input,
          isFocused && styles.inputFocused,
          error ? styles.inputError : null,
          style,
        ]}
        placeholderTextColor={ThemeColors.textMuted}
        onFocus={(e) => {
          setIsFocused(true);
          if (onFocus) onFocus(e);
        }}
        onBlur={(e) => {
          setIsFocused(false);
          if (onBlur) onBlur(e);
        }}
        {...props}
      />
      {error && <Text style={styles.errorText}>{error}</Text>}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginVertical: Spacing.sm,
    width: '100%',
  },
  label: {
    ...Typography.subheader,
    marginBottom: Spacing.xs,
  },
  input: {
    backgroundColor: ThemeColors.surfaceGlass,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: ThemeColors.border,
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.md,
    color: ThemeColors.textPrimary,
    fontSize: 16,
    width: '100%',
  },
  inputFocused: {
    borderColor: ThemeColors.accentBronze,
  },
  inputError: {
    borderColor: ThemeColors.error,
  },
  errorText: {
    color: ThemeColors.error,
    fontSize: 12,
    marginTop: Spacing.xs,
    marginLeft: Spacing.xs,
  },
});
