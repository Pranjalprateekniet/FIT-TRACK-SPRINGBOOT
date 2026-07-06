import React, { useState } from 'react';
import { StyleSheet, Text, View, ScrollView } from 'react-native';
import { useAuth } from '../../context/AuthContext';
import { useNavigation } from '../../context/NavigationContext';
import { GlassCard } from '../../components/GlassCard';
import { GlassInput } from '../../components/GlassInput';
import { PrimaryButton, SecondaryButton } from '../../components/Buttons';
import { ThemeColors, Spacing, Typography } from '../../theme/Theme';

export const LoginScreen = () => {
  const { login, error, clearError } = useAuth();
  const { navigateTo } = useNavigation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    if (!email || !password) return;
    setLoading(true);
    try {
      await login(email, password);
    } catch (err: any) {
      console.log("Login error:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container} keyboardShouldPersistTaps="handled">
      <Text style={styles.appName}>NUVIOCORE</Text>
      <GlassCard style={styles.card}>
        <Text style={styles.title}>Welcome Back</Text>
        <Text style={styles.subtitle}>Sign in to track your fitness metrics.</Text>
        
        {error && <Text style={styles.errorText}>{error}</Text>}
        
        <GlassInput
          label="Email Address"
          placeholder="email@example.com"
          keyboardType="email-address"
          autoCapitalize="none"
          value={email}
          onChangeText={(val) => {
            setEmail(val);
            if (error) clearError();
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
          }}
        />
        
        <PrimaryButton title="Login" loading={loading} onPress={handleLogin} />
        
        <Text style={styles.registerLabel}>Don't have an account?</Text>
        <SecondaryButton title="Register" onPress={() => navigateTo('REGISTER')} />
        
        <Text style={styles.verifyLink} onPress={() => navigateTo('VERIFY')}>
          Need to verify email? Click here
        </Text>
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
    color: '#FFFFFF',
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
  registerLabel: {
    ...Typography.bodySmall,
    color: ThemeColors.textMuted,
    textAlign: 'center',
    marginTop: Spacing.lg,
    marginBottom: Spacing.xs,
  },
  verifyLink: {
    ...Typography.bodySmall,
    color: '#FFFFFF',
    textAlign: 'center',
    marginTop: Spacing.md,
    textDecorationLine: 'underline',
  },
});
