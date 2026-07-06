import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { ThemeColors, Spacing, Typography } from '../theme/Theme';

interface ProgressRingProps {
  value: number;
  max: number;
  label: string;
  size?: number;
}

export const ProgressRing: React.FC<ProgressRingProps> = ({ value, max, label, size = 150 }) => {
  const percentage = Math.min(1, max > 0 ? value / max : 0);
  const percentText = `${(percentage * 100).toFixed(0)}%`;
  
  return (
    <View style={styles.container}>
      <View style={[styles.circle, { width: size, height: size, borderRadius: size / 2 }]}>
        {/* Subtly colored border indicator based on progress */}
        <View style={[styles.activeBorderOverlay, { 
          width: size, 
          height: size, 
          borderRadius: size / 2,
          borderColor: percentage > 0.8 ? '#FFFFFF' : '#262626'
        }]} />
        <View style={styles.content}>
          <Text style={styles.percentageText}>{percentText}</Text>
          <Text style={styles.labelText}>{label}</Text>
          <Text style={styles.subText}>{value.toFixed(0)} / {max.toFixed(0)} kcal</Text>
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: Spacing.md,
  },
  circle: {
    borderWidth: 6,
    borderColor: '#262626',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'transparent',
    position: 'relative',
  },
  activeBorderOverlay: {
    position: 'absolute',
    borderWidth: 6,
    borderColor: 'transparent',
    opacity: 0.7,
  },
  content: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  percentageText: {
    ...Typography.headingLarge,
    color: '#FFFFFF',
    fontWeight: 'bold',
  },
  labelText: {
    ...Typography.subheader,
    fontSize: 10,
    marginTop: Spacing.xs,
    color: ThemeColors.textSecondary,
  },
  subText: {
    ...Typography.bodySmall,
    marginTop: 4,
    color: ThemeColors.textMuted,
  },
});
