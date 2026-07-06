import React, { useState } from 'react';
import { StyleSheet, Text, View, ScrollView } from 'react-native';
import { useAuth } from '../../context/AuthContext';
import { useNavigation } from '../../context/NavigationContext';
import { GlassCard } from '../../components/LegacyGlassCard';
import { GlassInput } from '../../components/LegacyGlassInput';
import { PrimaryButton, SecondaryButton } from '../../components/LegacyButtons';
import { LegacyThemeColors as ThemeColors, Spacing, Typography } from '../../theme/Theme';

export const VerifyEmailScreen = () => {
  const { verifyEmail, error, clearError } = useAuth();
  const { navigateTo } = useNavigation();
  const [token, setToken] = useState('');
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const handleVerify = async () => {
    if (!token) return;
    setLoading(true);
    try {
      const msg = await verifyEmail(token);
      setSuccessMessage(msg || "Email verified successfully!");
      setToken('');
    } catch (err: any) {
      console.log("Verification error:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">
      <Text style={styles.appName}>NUVIOCORE</Text>
      <GlassCard style={styles.card}>
        <Text style={styles.title}>Email Verification</Text>
        <Text style={styles.subtitle}>Enter the token sent to your registered email.</Text>
        
        {error && <Text style={styles.errorText}>{error}</Text>}
        {successMessage && <Text style={styles.successText}>{successMessage}</Text>}
        
        <GlassInput
          label="Verification Token"
          placeholder="Enter UUID token"
          autoCapitalize="none"
          autoCorrect={false}
          value={token}
          onChangeText={(val) => {
            setToken(val);
            if (error) clearError();
            if (successMessage) setSuccessMessage(null);
          }}
        />
        
        <PrimaryButton title="Verify Account" loading={loading} onPress={handleVerify} />
        
        <Text style={styles.loginLabel}>Verified or need to sign in?</Text>
        <SecondaryButton title="Go to Login" onPress={() => navigateTo('LOGIN')} />
      </GlassCard>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    backgroundColor: ThemeColors.background,
    justifyContent: 'center',
    padding: Spacing.lg,
  },
  appName: {
    ...Typography.headingLarge,
    color: ThemeColors.accentBronze,
    textAlign: 'center',
    fontWeight: '300',
    letterSpacing: 4,
    marginBottom: Spacing.xl,
  },
  card: {
    padding: Spacing.xl,
  },
  title: {
    ...Typography.headingLarge,
    marginBottom: Spacing.xs,
  },
  subtitle: {
    ...Typography.bodyMedium,
    color: ThemeColors.textSecondary,
    marginBottom: Spacing.lg,
  },
  errorText: {
    color: ThemeColors.error,
    fontSize: 14,
    textAlign: 'center',
    marginBottom: Spacing.md,
  },
  successText: {
    color: ThemeColors.accentBronze,
    fontSize: 14,
    textAlign: 'center',
    marginBottom: Spacing.md,
  },
  loginLabel: {
    ...Typography.bodySmall,
    color: ThemeColors.textMuted,
    textAlign: 'center',
    marginTop: Spacing.lg,
    marginBottom: Spacing.xs,
  },
});
