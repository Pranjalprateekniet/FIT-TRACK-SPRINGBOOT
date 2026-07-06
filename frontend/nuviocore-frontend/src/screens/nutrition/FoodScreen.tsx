import React, { useState, useEffect, useRef } from 'react';
import {
  StyleSheet,
  Text,
  View,
  ScrollView,
  TouchableOpacity,
  RefreshControl,
  Alert,
  Animated,
  LayoutAnimation,
  Platform,
  UIManager,
  Modal,
  ActivityIndicator,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppLayout } from '../../components/AppLayout';
import { GlassInput } from '../../components/GlassInput';
import { PrimaryButton, SecondaryButton } from '../../components/Buttons';
import { fetchClient } from '../../api/FetchClient';
import { useNavigation } from '../../context/NavigationContext';
import { useNutrition } from '../../context/NutritionContext';
import { ThemeColors, Spacing } from '../../theme/Theme';
import { FoodLogResponse, FoodNutritionEntity, MealType } from '../../types';

if (Platform.OS === 'android' && UIManager.setLayoutAnimationEnabledExperimental) {
  UIManager.setLayoutAnimationEnabledExperimental(true);
}

// ─── Animated Progress Bar ────────────────────────────────────────────────

const AnimatedBar: React.FC<{ value: number; max: number; height?: number }> = ({
  value, max, height = 3,
}) => {
  const anim = useRef(new Animated.Value(0)).current;
  const pct = max > 0 ? Math.min(1, value / max) : 0;
  useEffect(() => {
    Animated.timing(anim, { toValue: pct, duration: 700, useNativeDriver: false }).start();
  }, [pct]);
  return (
    <View style={[barS.track, { height }]}>
      <Animated.View
        style={[barS.fill, { height, width: anim.interpolate({ inputRange: [0, 1], outputRange: ['0%', '100%'] }) }]}
      />
    </View>
  );
};
const barS = StyleSheet.create({
  track: { backgroundColor: '#262626', borderRadius: 99, width: '100%', overflow: 'hidden' },
  fill:  { backgroundColor: '#FFFFFF', borderRadius: 99 },
});

// ─── Quick Action Card ────────────────────────────────────────────────────

const QuickAction: React.FC<{ icon: keyof typeof Ionicons.glyphMap; label: string; onPress: () => void }> = ({
  icon, label, onPress,
}) => {
  const scale = useRef(new Animated.Value(1)).current;
  const pressIn  = () => Animated.spring(scale, { toValue: 0.93, useNativeDriver: true, speed: 50 }).start();
  const pressOut = () => Animated.spring(scale, { toValue: 1,    useNativeDriver: true, speed: 50 }).start();
  return (
    <TouchableOpacity onPress={onPress} onPressIn={pressIn} onPressOut={pressOut} activeOpacity={1}>
      <Animated.View style={[qaS.card, { transform: [{ scale }] }]}>
        <Ionicons name={icon} size={22} color="#FFFFFF" />
        <Text style={qaS.label}>{label}</Text>
      </Animated.View>
    </TouchableOpacity>
  );
};
const qaS = StyleSheet.create({
  card: {
    backgroundColor: '#181818',
    borderRadius: 16,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.06)',
    width: 76, height: 76,
    alignItems: 'center', justifyContent: 'center', gap: 6,
  },
  label: { fontSize: 10, fontWeight: '600', color: ThemeColors.textSecondary, letterSpacing: 0.2, textAlign: 'center' },
});

// ─── Meal Section ─────────────────────────────────────────────────────────

const MEAL_META: Record<MealType, { label: string; icon: keyof typeof Ionicons.glyphMap }> = {
  BREAKFAST:      { label: 'Breakfast',      icon: 'sunny-outline' },
  MORNING_SNACK:  { label: 'Morning Snack',  icon: 'cafe-outline' },
  LUNCH:          { label: 'Lunch',          icon: 'restaurant-outline' },
  EVENING_SNACK:  { label: 'Evening Snack',  icon: 'nutrition-outline' },
  DINNER:         { label: 'Dinner',         icon: 'moon-outline' },
};

interface MealSectionProps {
  mealType:  MealType;
  logs:      FoodLogResponse[];
  onAdd:     () => void;
  onDelete:  (logId: number) => void;
  onEdit:    (log: FoodLogResponse) => void;
}

const MealSection: React.FC<MealSectionProps> = ({ mealType, logs, onAdd, onDelete, onEdit }) => {
  const [expanded, setExpanded] = useState(logs.length > 0);
  const meta = MEAL_META[mealType];

  const toggle = () => {
    LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);
    setExpanded(v => !v);
  };

  return (
    <View style={mealS.wrapper}>
      <TouchableOpacity style={mealS.header} onPress={toggle} activeOpacity={0.75}>
        <View style={mealS.headerLeft}>
          <Ionicons name={meta.icon} size={16} color={ThemeColors.textMuted} />
          <Text style={mealS.mealLabel}>{meta.label}</Text>
          {logs.length > 0 && (
            <Text style={mealS.badge}>{logs.length} item{logs.length !== 1 ? 's' : ''}</Text>
          )}
        </View>
        <View style={mealS.headerRight}>
          <TouchableOpacity
            style={mealS.addBtn}
            onPress={onAdd}
            hitSlop={{ top: 8, bottom: 8, left: 8, right: 8 }}
          >
            <Ionicons name="add" size={16} color="#FFFFFF" />
          </TouchableOpacity>
          <Ionicons
            name={expanded ? 'chevron-up' : 'chevron-down'}
            size={14} color={ThemeColors.textMuted}
            style={{ marginLeft: 6 }}
          />
        </View>
      </TouchableOpacity>

      {expanded && (
        <View style={mealS.foodList}>
          {logs.length === 0 ? (
            <TouchableOpacity style={mealS.emptyRow} onPress={onAdd}>
              <Ionicons name="add-circle-outline" size={14} color={ThemeColors.textMuted} />
              <Text style={mealS.emptyText}>Add food to {meta.label.toLowerCase()}</Text>
            </TouchableOpacity>
          ) : (
            logs.map((log, idx) => (
              <View
                key={log.logId}
                style={[mealS.foodRow, idx === logs.length - 1 && { borderBottomWidth: 0 }]}
              >
                <View style={mealS.foodInfo}>
                  <Text style={mealS.foodName} numberOfLines={1}>{log.foodName}</Text>
                  <Text style={mealS.foodGrams}>{log.gramsConsumed}g</Text>
                </View>
                {/* Edit button */}
                <TouchableOpacity
                  onPress={() => onEdit(log)}
                  hitSlop={{ top: 6, bottom: 6, left: 6, right: 6 }}
                  style={mealS.actionBtn}
                >
                  <Ionicons name="pencil-outline" size={14} color={ThemeColors.textMuted} />
                </TouchableOpacity>
                {/* Delete button */}
                <TouchableOpacity
                  onPress={() => onDelete(log.logId)}
                  hitSlop={{ top: 6, bottom: 6, left: 6, right: 6 }}
                  style={mealS.actionBtn}
                >
                  <Ionicons name="trash-outline" size={14} color="rgba(255,69,58,0.7)" />
                </TouchableOpacity>
              </View>
            ))
          )}
          {logs.length > 0 && (
            <TouchableOpacity style={mealS.addMoreRow} onPress={onAdd}>
              <Ionicons name="add" size={13} color={ThemeColors.textMuted} />
              <Text style={mealS.addMoreText}>Add food</Text>
            </TouchableOpacity>
          )}
        </View>
      )}
    </View>
  );
};

const mealS = StyleSheet.create({
  wrapper: {
    backgroundColor: '#181818',
    borderRadius: 16,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.06)',
    marginBottom: 8,
    overflow: 'hidden',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 14,
    paddingVertical: 14,
  },
  headerLeft:  { flexDirection: 'row', alignItems: 'center', gap: 8, flex: 1 },
  mealLabel:   { fontSize: 14, fontWeight: '600', color: ThemeColors.textPrimary },
  badge: {
    fontSize: 11,
    color: ThemeColors.textMuted,
    backgroundColor: 'rgba(255,255,255,0.06)',
    paddingHorizontal: 6, paddingVertical: 2,
    borderRadius: 6,
  },
  headerRight: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  addBtn: {
    width: 26, height: 26, borderRadius: 13,
    backgroundColor: 'rgba(255,255,255,0.1)',
    alignItems: 'center', justifyContent: 'center',
  },
  foodList:    { borderTopWidth: 1, borderTopColor: 'rgba(255,255,255,0.04)' },
  foodRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255,255,255,0.04)',
    gap: 4,
  },
  foodInfo:    { flex: 1 },
  foodName:    { fontSize: 13, fontWeight: '500', color: ThemeColors.textPrimary },
  foodGrams:   { fontSize: 11, color: ThemeColors.textMuted, marginTop: 1 },
  actionBtn:   { padding: 4 },
  emptyRow:    { flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 14, paddingVertical: 12 },
  emptyText:   { fontSize: 12, color: ThemeColors.textMuted },
  addMoreRow: {
    flexDirection: 'row', alignItems: 'center', gap: 4,
    paddingHorizontal: 14, paddingVertical: 10,
    borderTopWidth: 1, borderTopColor: 'rgba(255,255,255,0.04)',
  },
  addMoreText: { fontSize: 12, color: ThemeColors.textMuted },
});

// ─── Macro Pill ───────────────────────────────────────────────────────────

const MacroPill: React.FC<{ label: string; consumed: number; target: number; unit?: string }> = ({
  label, consumed, target, unit = 'g',
}) => (
  <View style={pillS.pill}>
    <Text style={pillS.value}>{consumed.toFixed(0)}<Text style={pillS.unit}>{unit}</Text></Text>
    <Text style={pillS.label}>{label}</Text>
    <AnimatedBar value={consumed} max={target} height={2} />
  </View>
);
const pillS = StyleSheet.create({
  pill: {
    flex: 1,
    backgroundColor: '#181818',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.06)',
    paddingVertical: 12, paddingHorizontal: 10,
    alignItems: 'center', gap: 4,
  },
  value: { fontSize: 18, fontWeight: '700', color: ThemeColors.textPrimary },
  unit:  { fontSize: 11, fontWeight: '400', color: ThemeColors.textMuted },
  label: {
    fontSize: 10, fontWeight: '600', color: ThemeColors.textMuted,
    textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 4,
  },
});

// ─── Edit Food Modal ──────────────────────────────────────────────────────

interface EditModalProps {
  log:      FoodLogResponse | null;
  onClose:  () => void;
  onSaved:  () => void;
}

const MEAL_TYPES: MealType[] = ['BREAKFAST', 'MORNING_SNACK', 'LUNCH', 'EVENING_SNACK', 'DINNER'];

const EditFoodModal: React.FC<EditModalProps> = ({ log, onClose, onSaved }) => {
  const [grams, setGrams]         = useState('');
  const [mealType, setMealType]   = useState<MealType>('LUNCH');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (log) {
      setGrams(String(log.gramsConsumed));
      setMealType(log.mealType);
    }
  }, [log]);

  const handleSubmit = async () => {
    if (!log) return;
    const gramsNum = parseFloat(grams);
    if (isNaN(gramsNum) || gramsNum <= 0) {
      Alert.alert('Invalid Amount', 'Please enter a valid number of grams.');
      return;
    }
    setSubmitting(true);
    try {
      // Resolve foodId from food name using the existing search endpoint
      const results: FoodNutritionEntity[] = await fetchClient(
        `/api/nutrition/search?query=${encodeURIComponent(log.foodName)}`
      );
      if (results.length === 0) {
        Alert.alert('Not Found', 'Could not resolve this food in the database.');
        return;
      }
      const foodId = results[0].id;

      await fetchClient(`/api/food/log/${log.logId}`, {
        method: 'PUT',
        body: {
          foodId,
          gramsConsumed: gramsNum,
          mealType,
          logDate: log.logDate, // keep original date
        },
      });
      onSaved();
    } catch (err: any) {
      Alert.alert('Update Failed', err.message || 'Could not update this food log.');
    } finally {
      setSubmitting(false);
    }
  };

  if (!log) return null;

  return (
    <Modal visible={!!log} transparent animationType="slide">
      <View style={editS.overlay}>
        <View style={editS.sheet}>
          <View style={editS.handle} />

          <Text style={editS.super}>Edit Food Log</Text>
          <Text style={editS.foodName} numberOfLines={2}>{log.foodName}</Text>

          <Text style={editS.fieldLabel}>AMOUNT (grams)</Text>
          <GlassInput
            placeholder="e.g. 150"
            keyboardType="numeric"
            value={grams}
            onChangeText={setGrams}
          />

          <Text style={editS.fieldLabel}>MEAL</Text>
          <View style={editS.chipRow}>
            {MEAL_TYPES.map(m => (
              <TouchableOpacity
                key={m}
                style={[editS.chip, mealType === m && editS.chipActive]}
                onPress={() => setMealType(m)}
              >
                <Text style={[editS.chipText, mealType === m && editS.chipTextActive]}>
                  {m.replace('_', ' ')}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          <View style={editS.btnRow}>
            <View style={{ flex: 1, marginRight: 8 }}>
              <SecondaryButton title="Cancel" onPress={onClose} />
            </View>
            <View style={{ flex: 1, marginLeft: 8 }}>
              <PrimaryButton title="Update" loading={submitting} onPress={handleSubmit} />
            </View>
          </View>
        </View>
      </View>
    </Modal>
  );
};

const editS = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.8)',
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: '#181818',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    borderTopWidth: 1,
    borderColor: 'rgba(255,255,255,0.08)',
    padding: 20,
    paddingBottom: Platform.OS === 'ios' ? 36 : 24,
  },
  handle: {
    width: 36, height: 4,
    backgroundColor: 'rgba(255,255,255,0.15)',
    borderRadius: 2,
    alignSelf: 'center',
    marginBottom: 20,
  },
  super: {
    fontSize: 10, fontWeight: '700', color: ThemeColors.textMuted,
    letterSpacing: 1.5, textTransform: 'uppercase', marginBottom: 4,
  },
  foodName: {
    fontSize: 20, fontWeight: '700', color: ThemeColors.textPrimary,
    letterSpacing: -0.2, marginBottom: 16,
  },
  fieldLabel: {
    fontSize: 10, fontWeight: '700', color: ThemeColors.textMuted,
    letterSpacing: 1.2, textTransform: 'uppercase',
    marginBottom: 6, marginTop: 12,
  },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginBottom: 20 },
  chip: {
    borderWidth: 1, borderColor: 'rgba(255,255,255,0.1)',
    borderRadius: 8, paddingVertical: 6, paddingHorizontal: 12,
    backgroundColor: 'rgba(255,255,255,0.03)',
  },
  chipActive: { borderColor: '#FFFFFF', backgroundColor: '#262626' },
  chipText: { fontSize: 12, color: ThemeColors.textSecondary, textTransform: 'capitalize', fontWeight: '500' },
  chipTextActive: { color: '#FFFFFF', fontWeight: '700' },
  btnRow: { flexDirection: 'row', marginTop: 4 },
});

// ─── Main FoodScreen ──────────────────────────────────────────────────────

export const FoodScreen = () => {
  const { navigateTo }                          = useNavigation();
  const { logs, summary, goal, loading, refresh } = useNutrition();
  const [refreshing, setRefreshing]             = useState(false);
  const [error, setError]                       = useState<string | null>(null);
  const [editingLog, setEditingLog]             = useState<FoodLogResponse | null>(null);

  // Initial load on mount
  useEffect(() => {
    refresh().catch((err: any) => {
      if (err?.message === 'SESSION_EXPIRED') navigateTo('LOGIN');
      else setError(err?.message || 'Failed to load nutrition data');
    });
  }, []);

  const onRefresh = async () => {
    setRefreshing(true);
    await refresh();
    setRefreshing(false);
  };

  const handleDeleteLog = (logId: number) => {
    Alert.alert('Remove Food', 'Remove this item from your log?', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Remove', style: 'destructive',
        onPress: async () => {
          try {
            await fetchClient(`/api/food/log/${logId}`, { method: 'DELETE' });
            refresh(); // refresh context → all screens update
          } catch (err: any) {
            Alert.alert('Failed', err.message || 'Could not remove food entry.');
          }
        },
      },
    ]);
  };

  const handleEditSaved = async () => {
    setEditingLog(null);
    await refresh(); // refresh context → all screens update
  };

  const mealTypes: MealType[] = ['BREAKFAST', 'MORNING_SNACK', 'LUNCH', 'EVENING_SNACK', 'DINNER'];
  const groupedLogs = mealTypes.reduce((acc, type) => {
    acc[type] = logs.filter(l => l.mealType === type);
    return acc;
  }, {} as Record<MealType, FoodLogResponse[]>);

  // Derived values — calories always integer
  const caloriesConsumed = Math.round(summary?.totalCalories ?? 0);
  const targetCalories   = Math.round(goal?.targetCalories   ?? 2000);
  const remaining        = Math.max(0, targetCalories - caloriesConsumed);
  const proteinConsumed  = summary?.totalProtein         ?? 0;
  const targetProtein    = goal?.targetProtein            ?? 120;
  const carbsConsumed    = summary?.totalCarbs            ?? 0;
  const targetCarbs      = goal?.targetCarbohydrates      ?? 200;
  const fatConsumed      = summary?.totalFat              ?? 0;
  const targetFat        = goal?.targetFat                ?? 70;

  return (
    <AppLayout>
      <ScrollView
        contentContainerStyle={s.container}
        showsVerticalScrollIndicator={false}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor="#FFFFFF" />}
      >
        {error && <Text style={s.errorText}>{error}</Text>}

        {/* ── Calorie Summary Card ── */}
        <View style={s.calorieCard}>
          <Text style={s.calorieLabel}>CALORIES</Text>
          <Text style={s.calorieNumber}>
            {caloriesConsumed.toLocaleString()}
            <Text style={s.calorieTarget}> / {targetCalories.toLocaleString()} kcal</Text>
          </Text>
          <Text style={s.calorieRemaining}>{remaining.toLocaleString()} kcal remaining</Text>
          <AnimatedBar value={caloriesConsumed} max={targetCalories} height={4} />
        </View>

        {/* ── Macro Strip ── */}
        <View style={s.macroRow}>
          <MacroPill label="Protein" consumed={proteinConsumed} target={targetProtein} />
          <View style={{ width: 8 }} />
          <MacroPill label="Carbs"   consumed={carbsConsumed}   target={targetCarbs} />
          <View style={{ width: 8 }} />
          <MacroPill label="Fat"     consumed={fatConsumed}     target={targetFat} />
        </View>

        {/* ── Quick Actions ── */}
        <View style={s.quickActions}>
          <QuickAction icon="bar-chart-outline"  label="Insights"    onPress={() => navigateTo('FOOD_INSIGHTS')} />
          <QuickAction icon="add-circle-outline" label="Log Meal"    onPress={() => navigateTo('FOOD_SEARCH')} />
          <QuickAction icon="search-outline"     label="Search Food" onPress={() => navigateTo('FOOD_SEARCH')} />
          <QuickAction icon="time-outline"       label="History"     onPress={() => navigateTo('FOOD_SEARCH')} />
        </View>

        {/* ── Meal Sections ── */}
        <Text style={s.sectionLabel}>TODAY'S MEALS</Text>
        {loading && logs.length === 0 && (
          <ActivityIndicator color="#FFFFFF" style={{ marginTop: 24 }} />
        )}
        {mealTypes.map(type => (
          <MealSection
            key={type}
            mealType={type}
            logs={groupedLogs[type]}
            onAdd={() => navigateTo('FOOD_SEARCH')}
            onDelete={handleDeleteLog}
            onEdit={log => setEditingLog(log)}
          />
        ))}
      </ScrollView>

      {/* ── Edit Modal ── */}
      <EditFoodModal
        log={editingLog}
        onClose={() => setEditingLog(null)}
        onSaved={handleEditSaved}
      />
    </AppLayout>
  );
};

const s = StyleSheet.create({
  container:        { padding: Spacing.lg, paddingBottom: 32 },
  errorText: {
    color: ThemeColors.error, fontSize: 13, textAlign: 'center',
    marginBottom: Spacing.md, backgroundColor: 'rgba(255,69,58,0.08)',
    padding: Spacing.sm, borderRadius: 10,
  },
  calorieCard: {
    backgroundColor: '#181818', borderRadius: 20,
    borderWidth: 1, borderColor: 'rgba(255,255,255,0.06)',
    padding: 18, marginBottom: 10,
  },
  calorieLabel: {
    fontSize: 10, fontWeight: '700', color: ThemeColors.textMuted,
    letterSpacing: 1.5, textTransform: 'uppercase', marginBottom: 6,
  },
  calorieNumber:    { fontSize: 28, fontWeight: '700', color: ThemeColors.textPrimary, letterSpacing: -0.5 },
  calorieTarget:    { fontSize: 14, fontWeight: '400', color: ThemeColors.textMuted },
  calorieRemaining: { fontSize: 13, color: ThemeColors.textSecondary, marginTop: 4, marginBottom: 12 },
  macroRow:         { flexDirection: 'row', marginBottom: 10 },
  quickActions:     { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 20 },
  sectionLabel: {
    fontSize: 10, fontWeight: '700', color: ThemeColors.textMuted,
    letterSpacing: 1.5, textTransform: 'uppercase', marginBottom: 10,
  },
});
