import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  Text,
  View,
  ScrollView,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
  Modal,
  TextInput,
  Platform,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppLayout } from '../../components/AppLayout';
import { GlassInput } from '../../components/GlassInput';
import { PrimaryButton, SecondaryButton } from '../../components/Buttons';
import { fetchClient } from '../../api/FetchClient';
import { useNavigation } from '../../context/NavigationContext';
import { useNutrition } from '../../context/NutritionContext';
import { ThemeColors, Spacing, Typography } from '../../theme/Theme';
import { FoodNutritionEntity, MealType, CreateFoodLogRequest } from '../../types';

const RECENT_SUGGESTIONS = ['Eggs', 'Chicken breast', 'Oatmeal', 'Brown rice', 'Banana'];

export const FoodSearchScreen = () => {
  const { navigateTo } = useNavigation();
  const nutrition = useNutrition();
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<FoodNutritionEntity[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Log Modal State
  const [selectedFood, setSelectedFood] = useState<FoodNutritionEntity | null>(null);
  const [grams, setGrams] = useState('100');
  const [mealType, setMealType] = useState<MealType>('LUNCH');
  const [logLoading, setLogLoading] = useState(false);

  // Debounced search
  useEffect(() => {
    if (query.trim().length < 3) {
      setResults([]);
      return;
    }
    const timer = setTimeout(async () => {
      setLoading(true);
      setError(null);
      try {
        const data: FoodNutritionEntity[] = await fetchClient(
          `/api/nutrition/search?query=${encodeURIComponent(query.trim())}`
        );
        setResults(data);
      } catch (err: any) {
        setError(err.message || 'Food search failed');
      } finally {
        setLoading(false);
      }
    }, 600);
    return () => clearTimeout(timer);
  }, [query]);

  const handleLogFoodSubmit = async () => {
    if (!selectedFood) return;
    const gramsNum = parseFloat(grams);
    if (isNaN(gramsNum) || gramsNum <= 0) {
      Alert.alert('Invalid Input', 'Grams consumed must be a positive number.');
      return;
    }
    setLogLoading(true);
    const todayStr = new Date().toISOString().split('T')[0];
    const payload: CreateFoodLogRequest = {
      foodId: selectedFood.id,
      gramsConsumed: gramsNum,
      mealType,
      logDate: todayStr,
    };
    try {
      await fetchClient('/api/food/log', { method: 'POST', body: payload });
      setSelectedFood(null);
      // Refresh context so FoodScreen + FoodInsightsScreen update immediately
      await nutrition.refresh();
      Alert.alert('Logged!', 'Food has been added to your log.', [
        { text: 'Done', onPress: () => navigateTo('FOOD') },
      ]);
    } catch (err: any) {
      Alert.alert('Failed to Log', err.message || 'Failed to submit log entry.');
    } finally {
      setLogLoading(false);
    }
  };

  const getPreviewMacro = (baseValue: number | null | undefined) => {
    if (!baseValue) return '0.0';
    const gramsNum = parseFloat(grams) || 0;
    const servingSize = selectedFood?.servingSizeG || 100.0;
    return ((baseValue / servingSize) * gramsNum).toFixed(1);
  };

  const showEmpty = !loading && results.length === 0 && query.trim().length >= 3;
  const showSuggestions = query.trim().length === 0;

  return (
    <AppLayout>
      <View style={styles.root}>

        {/* ── Top Bar ──────────────────────────────────────────── */}
        <View style={styles.topBar}>
          <TouchableOpacity
            style={styles.backBtn}
            onPress={() => navigateTo('FOOD')}
            activeOpacity={0.7}
          >
            <Ionicons name="arrow-back" size={20} color={ThemeColors.textSecondary} />
          </TouchableOpacity>
          <Text style={styles.topTitle}>Search Food</Text>
        </View>

        {/* ── Search Bar ───────────────────────────────────────── */}
        <View style={styles.searchWrap}>
          <Ionicons name="search" size={16} color={ThemeColors.textMuted} style={styles.searchIcon} />
          <TextInput
            style={styles.searchInput}
            placeholder="Search foods, e.g. eggs, chicken, rice..."
            placeholderTextColor={ThemeColors.textMuted}
            value={query}
            onChangeText={setQuery}
            autoFocus
            returnKeyType="search"
            autoCapitalize="none"
            autoCorrect={false}
          />
          {query.length > 0 && (
            <TouchableOpacity onPress={() => setQuery('')} hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}>
              <Ionicons name="close-circle" size={16} color={ThemeColors.textMuted} />
            </TouchableOpacity>
          )}
        </View>

        {/* ── Content ──────────────────────────────────────────── */}
        <ScrollView
          style={styles.scroll}
          contentContainerStyle={styles.scrollContent}
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          {/* Recent / suggestions */}
          {showSuggestions && (
            <>
              <Text style={styles.sectionLabel}>FREQUENT FOODS</Text>
              {RECENT_SUGGESTIONS.map((s) => (
                <TouchableOpacity
                  key={s}
                  style={styles.suggestionRow}
                  onPress={() => setQuery(s)}
                  activeOpacity={0.75}
                >
                  <Ionicons name="time-outline" size={15} color={ThemeColors.textMuted} />
                  <Text style={styles.suggestionText}>{s}</Text>
                  <Ionicons name="arrow-up-outline" size={14} color="rgba(255,255,255,0.15)" style={{ transform: [{ rotate: '45deg' }] }} />
                </TouchableOpacity>
              ))}
            </>
          )}

          {/* Loading */}
          {loading && (
            <View style={styles.centered}>
              <ActivityIndicator color="#FFFFFF" size="small" />
              <Text style={styles.loadingText}>Searching database…</Text>
            </View>
          )}

          {/* Error */}
          {error && <Text style={styles.errorText}>{error}</Text>}

          {/* No results */}
          {showEmpty && (
            <View style={styles.centered}>
              <Ionicons name="search-outline" size={32} color={ThemeColors.textMuted} />
              <Text style={styles.noResultText}>No foods found for "{query}"</Text>
            </View>
          )}

          {/* Results */}
          {!loading && results.length > 0 && (
            <>
              <Text style={styles.sectionLabel}>{results.length} RESULTS</Text>
              {results.map((food) => (
                <TouchableOpacity
                  key={food.id}
                  style={styles.foodCard}
                  onPress={() => setSelectedFood(food)}
                  activeOpacity={0.75}
                >
                  <View style={styles.foodCardTop}>
                    <Text style={styles.foodName} numberOfLines={1}>{food.foodName}</Text>
                    <Text style={styles.foodCal}>{food.calories} kcal</Text>
                  </View>
                  <View style={styles.foodCardBottom}>
                    <Text style={styles.macroText}>
                      P {food.proteinG?.toFixed(1)}g · C {food.carbsG?.toFixed(1)}g · F {food.fatG?.toFixed(1)}g
                    </Text>
                    <Text style={styles.sourceText}>{food.source || 'DB'}</Text>
                  </View>
                </TouchableOpacity>
              ))}
            </>
          )}
        </ScrollView>

        {/* ── Log Modal ────────────────────────────────────────── */}
        {selectedFood && (
          <Modal visible={!!selectedFood} transparent animationType="slide">
            <View style={styles.modalOverlay}>
              <View style={styles.modalSheet}>
                {/* Handle */}
                <View style={styles.modalHandle} />

                <Text style={styles.modalSuper}>Log Food</Text>
                <Text style={styles.modalFoodName} numberOfLines={2}>{selectedFood.foodName}</Text>
                <Text style={styles.modalBase}>
                  Per {selectedFood.servingSizeG || 100}g — {selectedFood.calories} kcal
                  · P {selectedFood.proteinG}g · C {selectedFood.carbsG}g · F {selectedFood.fatG}g
                </Text>

                {/* Grams Input */}
                <Text style={styles.fieldLabel}>AMOUNT (grams)</Text>
                <GlassInput
                  placeholder="e.g. 150"
                  keyboardType="numeric"
                  value={grams}
                  onChangeText={setGrams}
                />

                {/* Live macro preview */}
                <View style={styles.previewRow}>
                  {[
                    { label: 'kcal', val: getPreviewMacro(selectedFood.calories) },
                    { label: 'Protein', val: `${getPreviewMacro(selectedFood.proteinG)}g` },
                    { label: 'Carbs',   val: `${getPreviewMacro(selectedFood.carbsG)}g` },
                    { label: 'Fat',     val: `${getPreviewMacro(selectedFood.fatG)}g` },
                  ].map((m) => (
                    <View key={m.label} style={styles.previewCell}>
                      <Text style={styles.previewVal}>{m.val}</Text>
                      <Text style={styles.previewLabel}>{m.label}</Text>
                    </View>
                  ))}
                </View>

                {/* Meal type selector */}
                <Text style={styles.fieldLabel}>MEAL</Text>
                <View style={styles.mealOptions}>
                  {(['BREAKFAST', 'MORNING_SNACK', 'LUNCH', 'EVENING_SNACK', 'DINNER'] as MealType[]).map((m) => (
                    <TouchableOpacity
                      key={m}
                      style={[styles.mealChip, mealType === m && styles.mealChipActive]}
                      onPress={() => setMealType(m)}
                    >
                      <Text style={[styles.mealChipText, mealType === m && styles.mealChipTextActive]}>
                        {m.replace('_', ' ')}
                      </Text>
                    </TouchableOpacity>
                  ))}
                </View>

                {/* Buttons */}
                <View style={styles.modalBtns}>
                  <View style={{ flex: 1, marginRight: 8 }}>
                    <SecondaryButton title="Cancel" onPress={() => setSelectedFood(null)} />
                  </View>
                  <View style={{ flex: 1, marginLeft: 8 }}>
                    <PrimaryButton title="Log Food" loading={logLoading} onPress={handleLogFoodSubmit} />
                  </View>
                </View>
              </View>
            </View>
          </Modal>
        )}
      </View>
    </AppLayout>
  );
};

const styles = StyleSheet.create({
  root: {
    flex: 1,
  },
  // Top bar
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing.lg,
    paddingTop: 12,
    paddingBottom: 8,
    gap: 12,
  },
  backBtn: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: 'rgba(255,255,255,0.05)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  topTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: ThemeColors.textPrimary,
  },
  // Search bar
  searchWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#181818',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.08)',
    marginHorizontal: Spacing.lg,
    paddingHorizontal: 12,
    height: 44,
    marginBottom: 4,
  },
  searchIcon: {
    marginRight: 8,
  },
  searchInput: {
    flex: 1,
    fontSize: 14,
    color: ThemeColors.textPrimary,
    paddingVertical: 0,
    fontWeight: '400',
  },
  // Scroll
  scroll: {
    flex: 1,
  },
  scrollContent: {
    paddingHorizontal: Spacing.lg,
    paddingTop: 12,
    paddingBottom: 32,
  },
  sectionLabel: {
    fontSize: 10,
    fontWeight: '700',
    color: ThemeColors.textMuted,
    letterSpacing: 1.5,
    textTransform: 'uppercase',
    marginBottom: 8,
    marginTop: 4,
  },
  // Suggestions
  suggestionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255,255,255,0.04)',
  },
  suggestionText: {
    flex: 1,
    fontSize: 14,
    color: ThemeColors.textSecondary,
  },
  // States
  centered: {
    alignItems: 'center',
    marginTop: 32,
    gap: 10,
  },
  loadingText: {
    fontSize: 13,
    color: ThemeColors.textMuted,
  },
  errorText: {
    color: ThemeColors.error,
    fontSize: 13,
    textAlign: 'center',
    marginTop: 16,
  },
  noResultText: {
    fontSize: 14,
    color: ThemeColors.textMuted,
    textAlign: 'center',
  },
  // Food result cards
  foodCard: {
    backgroundColor: '#181818',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.06)',
    padding: 12,
    marginBottom: 6,
  },
  foodCardTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: 4,
  },
  foodName: {
    fontSize: 14,
    fontWeight: '600',
    color: ThemeColors.textPrimary,
    flex: 1,
    marginRight: 8,
  },
  foodCal: {
    fontSize: 14,
    fontWeight: '700',
    color: '#FFFFFF',
  },
  foodCardBottom: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  macroText: {
    fontSize: 12,
    color: ThemeColors.textSecondary,
  },
  sourceText: {
    fontSize: 10,
    color: ThemeColors.textMuted,
    textTransform: 'uppercase',
  },
  // Modal
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.8)',
    justifyContent: 'flex-end',
  },
  modalSheet: {
    backgroundColor: '#181818',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    borderTopWidth: 1,
    borderColor: 'rgba(255,255,255,0.08)',
    padding: 20,
    paddingBottom: Platform.OS === 'ios' ? 36 : 24,
  },
  modalHandle: {
    width: 36,
    height: 4,
    backgroundColor: 'rgba(255,255,255,0.15)',
    borderRadius: 2,
    alignSelf: 'center',
    marginBottom: 20,
  },
  modalSuper: {
    fontSize: 10,
    fontWeight: '700',
    color: ThemeColors.textMuted,
    letterSpacing: 1.5,
    textTransform: 'uppercase',
    marginBottom: 4,
  },
  modalFoodName: {
    fontSize: 20,
    fontWeight: '700',
    color: ThemeColors.textPrimary,
    letterSpacing: -0.2,
    marginBottom: 4,
  },
  modalBase: {
    fontSize: 12,
    color: ThemeColors.textMuted,
    marginBottom: 16,
    lineHeight: 18,
  },
  fieldLabel: {
    fontSize: 10,
    fontWeight: '700',
    color: ThemeColors.textMuted,
    letterSpacing: 1.2,
    textTransform: 'uppercase',
    marginBottom: 6,
    marginTop: 12,
  },
  previewRow: {
    flexDirection: 'row',
    backgroundColor: 'rgba(255,255,255,0.03)',
    borderRadius: 12,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.06)',
    padding: 12,
    marginVertical: 10,
  },
  previewCell: {
    flex: 1,
    alignItems: 'center',
  },
  previewVal: {
    fontSize: 15,
    fontWeight: '700',
    color: ThemeColors.textPrimary,
  },
  previewLabel: {
    fontSize: 10,
    color: ThemeColors.textMuted,
    marginTop: 2,
  },
  mealOptions: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
    marginBottom: 16,
  },
  mealChip: {
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.1)',
    borderRadius: 8,
    paddingVertical: 6,
    paddingHorizontal: 12,
    backgroundColor: 'rgba(255,255,255,0.03)',
  },
  mealChipActive: {
    borderColor: '#FFFFFF',
    backgroundColor: '#262626',
  },
  mealChipText: {
    fontSize: 12,
    color: ThemeColors.textSecondary,
    textTransform: 'capitalize',
    fontWeight: '500',
  },
  mealChipTextActive: {
    color: '#FFFFFF',
    fontWeight: '700',
  },
  modalBtns: {
    flexDirection: 'row',
    marginTop: 8,
  },
});
