import React, { useState } from 'react';
import { StyleSheet, Text, View, ScrollView } from 'react-native';
import { useAuth } from '../../context/AuthContext';
import { useNavigation } from '../../context/NavigationContext';
import { GlassCard } from '../../components/LegacyGlassCard';
import { GlassInput } from '../../components/LegacyGlassInput';
import { PrimaryButton, SecondaryButton } from '../../components/LegacyButtons';
import { LegacyThemeColors as ThemeColors, Spacing, Typography } from '../../theme/Theme';

export const RegisterScreen = () => {
  const { register, error, clearError } = useAuth();
  const { navigateTo } = useNavigation();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const handleRegister = async () => {
    if (!name || !email || !password) return;
    setLoading(true);
    try {
      const msg = await register(name, email, password);
      setSuccessMessage(msg || "Registration successful. Please verify email.");
      setName('');
      setEmail('');
      setPassword('');
    } catch (err: any) {
      console.log("Registration error:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">
      <Text style={styles.appName}>NUVIOCORE</Text>
      <GlassCard style={styles.card}>
        <Text style={styles.title}>Join Us</Text>
        <Text style={styles.subtitle}>Create your profile to start tracking.</Text>
        
        {error && <Text style={styles.errorText}>{error}</Text>}
        {successMessage && <Text style={styles.successText}>{successMessage}</Text>}
        
        <GlassInput
          label="Full Name"
          placeholder="John Doe"
          value={name}
          onChangeText={(val) => {
            setName(val);
            if (error) clearError();
            if (successMessage) setSuccessMessage(null);
          }}
        />
        
        <GlassInput
          label="Email Address"
          placeholder="email@example.com"
          keyboardType="email-address"
          autoCapitalize="none"
          value={email}
          onChangeText={(val) => {
            setEmail(val);
            if (error) clearError();
            if (successMessage) setSuccessMessage(null);
          }}
        />
        
        <GlassInput
          label="Password"
          placeholder="••••••••"
          secureTextEntry
          value={password}
          onChangeText={(val) => {
            setPassword(val);
            if (error) clearError();
            if (successMessage) setSuccessMessage(null);
          }}
        />
        
        <PrimaryButton title="Register" loading={loading} onPress={handleRegister} />
        
        {successMessage ? (
          <SecondaryButton title="Go to Verification" onPress={() => navigateTo('VERIFY')} />
        ) : (
          <>
            <Text style={styles.loginLabel}>Already have an account?</Text>
            <SecondaryButton title="Login" onPress={() => navigateTo('LOGIN')} />
          </>
        )}
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
