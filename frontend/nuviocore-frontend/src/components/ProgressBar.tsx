import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { ThemeColors, Spacing, Typography } from '../theme/Theme';

interface ProgressBarProps {
  label: string;
  value: number;
  max: number;
  unit?: string;
}

export const ProgressBar: React.FC<ProgressBarProps> = ({ label, value, max, unit = 'g' }) => {
  const percentage = Math.min(1, max > 0 ? value / max : 0);
  
  return (
    <View style={styles.container}>
      <View style={styles.labelRow}>
        <Text style={styles.label}>{label}</Text>
        <Text style={styles.value}>
          {value.toFixed(0)} / {max.toFixed(0)} {unit}
        </Text>
      </View>
      <View style={styles.track}>
        <View style={[styles.progress, { width: `${percentage * 100}%` }]} />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginVertical: Spacing.sm,
    width: '100%',
  },
  labelRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: Spacing.xs,
  },
  label: {
    ...Typography.bodyMedium,
    color: ThemeColors.textSecondary,
    fontWeight: '600',
  },
  value: {
    ...Typography.bodyMedium,
    color: ThemeColors.textPrimary,
  },
  track: {
    height: 6,
    backgroundColor: '#262626',
    borderRadius: 3,
    width: '100%',
    overflow: 'hidden',
  },
  progress: {
    height: '100%',
    backgroundColor: '#FFFFFF',
    borderRadius: 3,
  },
});
