import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Typography, Spacing } from '../theme/Theme';

interface SectionHeaderProps {
  title: string;
  subtitle?: string;
}

export const SectionHeader: React.FC<SectionHeaderProps> = ({ title, subtitle }) => {
  return (
    <View style={styles.container}>
      {subtitle && <Text style={styles.subtitle}>{subtitle}</Text>}
      <Text style={styles.title}>{title}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginVertical: Spacing.md,
    alignItems: 'flex-start',
    width: '100%',
  },
  subtitle: {
    ...Typography.subheader,
    marginBottom: Spacing.xs,
  },
  title: {
    ...Typography.headingMedium,
  },
});
