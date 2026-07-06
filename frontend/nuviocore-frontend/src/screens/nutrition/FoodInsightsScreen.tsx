import React, { useState, useEffect, useRef } from 'react';
import {
  StyleSheet,
  Text,
  View,
  ScrollView,
  TouchableOpacity,
  Animated,
  ActivityIndicator,
  LayoutAnimation,
  Platform,
  UIManager,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { AppLayout } from '../../components/AppLayout';
import { useNavigation } from '../../context/NavigationContext';
import { useNutrition, SourceItem } from '../../context/NutritionContext';
import { ThemeColors, Spacing } from '../../theme/Theme';

if (Platform.OS === 'android' && UIManager.setLayoutAnimationEnabledExperimental) {
  UIManager.setLayoutAnimationEnabledExperimental(true);
}

// ─── Color helpers ────────────────────────────────────────────────────────

const getBarColor = (rawPct: number): string => {
  if (rawPct > 1.1)  return '#FF453A'; // >110 % — red
  if (rawPct > 1.0)  return '#FF9500'; // 100–110 % — orange
  if (rawPct >= 0.8) return '#30D158'; // 80–100 % — green
  if (rawPct >= 0.4) return '#FFD60A'; // 40–80 % — yellow
  return '#FFD60A';                    // 0–40 % — yellow (user override)
};

// ─── Smart Animated Bar ───────────────────────────────────────────────────

const SmartBar: React.FC<{ value: number; max: number; delay?: number }> = ({
  value, max, delay = 0,
}) => {
  const rawPct  = max > 0 ? value / max : 0;
  const fillPct = Math.min(1, rawPct);
  const color   = getBarColor(rawPct);
  const wAnim   = useRef(new Animated.Value(0)).current;

  useEffect(() => {
    const t = setTimeout(() => {
      Animated.timing(wAnim, { toValue: fillPct, duration: 700, useNativeDriver: false }).start();
    }, delay);
    return () => clearTimeout(t);
  }, [fillPct, delay]);

  return (
    <View style={barS.track}>
      <Animated.View
        style={[barS.fill, {
          backgroundColor: color,
          width: wAnim.interpolate({ inputRange: [0, 1], outputRange: ['0%', '100%'] }),
        }]}
      />
    </View>
  );
};

const barS = StyleSheet.create({
  track: { height: 4, backgroundColor: '#262626', borderRadius: 99, overflow: 'hidden', marginTop: 8 },
  fill:  { height: 4, borderRadius: 99 },
});

// ─── Source Row ───────────────────────────────────────────────────────────

const SourceRow: React.FC<{ foodName: string; amount: string; isLast: boolean }> = ({
  foodName, amount, isLast,
}) => (
  <View style={[srcS.row, isLast && { borderBottomWidth: 0 }]}>
    <Text style={srcS.name} numberOfLines={1}>{foodName}</Text>
    <View style={srcS.dots} />
    <Text style={srcS.amount}>{amount}</Text>
  </View>
);

const srcS = StyleSheet.create({
  row: {
    flexDirection: 'row', alignItems: 'center',
    paddingVertical: 9,
    borderBottomWidth: 1, borderBottomColor: 'rgba(255,255,255,0.04)',
  },
  name:   { fontSize: 13, color: ThemeColors.textSecondary, flex: 1, marginRight: 6 },
  dots: {
    flex: 1, height: 1,
    borderBottomWidth: 1, borderBottomColor: 'rgba(255,255,255,0.08)',
    borderStyle: 'dotted', marginHorizontal: 6,
  },
  amount: { fontSize: 13, fontWeight: '600', color: ThemeColors.textPrimary, minWidth: 52, textAlign: 'right' },
});

// ─── Contributor Section (reusable inside any card) ───────────────────────

const VIEW_ALL_THRESHOLD = 5;
const TOP_COUNT = 3;

interface ContribSectionProps {
  sources:       SourceItem[];
  sourcesLoading: boolean;
  /** For calories: Math.round(amount) + 'kcal'. For macros: amount.toFixed(1) + unit. */
  formatAmount:  (amount: number) => string;
}

const ContribSection: React.FC<ContribSectionProps> = ({ sources, sourcesLoading, formatAmount }) => {
  const [showAll, setShowAll] = useState(false);

  const toggleShowAll = () => {
    LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);
    setShowAll(v => !v);
  };

  if (sourcesLoading) {
    return (
      <View style={{ alignItems: 'center', paddingVertical: 12 }}>
        <ActivityIndicator size="small" color={ThemeColors.textMuted} />
      </View>
    );
  }

  if (sources.length === 0) {
    return <Text style={csS.empty}>No source data available</Text>;
  }

  const topItems = sources.slice(0, TOP_COUNT);
  const allFirst = sources.slice(0, VIEW_ALL_THRESHOLD);
  const allExtra = sources.slice(VIEW_ALL_THRESHOLD);
  const hasMore  = sources.length > VIEW_ALL_THRESHOLD;

  return (
    <>
      {/* ── Top Contributors ── */}
      <Text style={csS.subTitle}>TOP CONTRIBUTORS</Text>
      {topItems.map((s, i) => (
        <SourceRow
          key={s.foodName + '-top'}
          foodName={s.foodName}
          amount={formatAmount(s.amount)}
          isLast={i === topItems.length - 1}
        />
      ))}

      {/* ── All Contributors ── */}
      <View style={csS.divider} />
      <Text style={csS.subTitle}>ALL CONTRIBUTORS</Text>
      {allFirst.map((s, i) => (
        <SourceRow
          key={s.foodName + '-all'}
          foodName={s.foodName}
          amount={formatAmount(s.amount)}
          isLast={!hasMore && i === allFirst.length - 1 && !showAll}
        />
      ))}
      {showAll && allExtra.map((s, i) => (
        <SourceRow
          key={s.foodName + '-extra'}
          foodName={s.foodName}
          amount={formatAmount(s.amount)}
          isLast={i === allExtra.length - 1}
        />
      ))}
      {hasMore && (
        <TouchableOpacity style={csS.viewAllBtn} onPress={toggleShowAll} activeOpacity={0.75}>
          <Text style={csS.viewAllText}>{showAll ? 'Collapse' : `View All (${sources.length})`}</Text>
          <Ionicons
            name={showAll ? 'chevron-up' : 'chevron-down'}
            size={13} color={ThemeColors.textMuted}
          />
        </TouchableOpacity>
      )}
    </>
  );
};

const csS = StyleSheet.create({
  subTitle: {
    fontSize: 9, fontWeight: '700', color: ThemeColors.textMuted,
    letterSpacing: 1.5, textTransform: 'uppercase', marginBottom: 4, marginTop: 2,
  },
  divider: {
    height: 1, backgroundColor: 'rgba(255,255,255,0.06)', marginVertical: 10,
  },
  empty: { fontSize: 12, color: ThemeColors.textMuted, paddingVertical: 6 },
  viewAllBtn: {
    flexDirection: 'row', alignItems: 'center', gap: 4,
    paddingVertical: 8, marginTop: 2,
  },
  viewAllText: { fontSize: 12, color: ThemeColors.textMuted, fontWeight: '600' },
});

// ─── Calorie Card (expandable) ─────────────────────────────────────────────

interface CalorieCardProps {
  consumed:       number;   // already rounded integer
  target:         number;
  sources:        SourceItem[];
  sourcesLoading: boolean;
  animDelay?:     number;
}

const CalorieCard: React.FC<CalorieCardProps> = ({
  consumed, target, sources, sourcesLoading, animDelay = 0,
}) => {
  const [expanded, setExpanded] = useState(false);
  const rawPct    = target > 0 ? consumed / target : 0;
  const pctDisplay = Math.round(rawPct * 100);
  const isOver    = rawPct > 1;
  const excess    = consumed - target;
  const remaining = target - consumed;
  const barColor  = getBarColor(rawPct);
  const toggle = () => {
    LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);
    setExpanded(v => !v);
  };

  return (
    <TouchableOpacity style={ccS.card} onPress={toggle} activeOpacity={0.85}>
      <View style={ccS.header}>
        <View style={ccS.iconWrap}>
          <Ionicons name="flame-outline" size={15} color={ThemeColors.textMuted} />
        </View>
        <Text style={ccS.label}>Calories</Text>
        <Text style={[ccS.pct, { color: barColor }]}>
          {isOver ? `+${pctDisplay - 100}% over` : `${pctDisplay}%`}
        </Text>
        <Ionicons
          name={expanded ? 'chevron-up' : 'chevron-down'}
          size={13} color={ThemeColors.textMuted} style={{ marginLeft: 6 }}
        />
      </View>

      <View style={ccS.valueRow}>
        <Text style={ccS.consumed}>
          {consumed.toLocaleString()}<Text style={ccS.unit}> kcal</Text>
        </Text>
        <Text style={ccS.target}>/ {target.toLocaleString()} kcal</Text>
      </View>

      <SmartBar value={consumed} max={target} delay={animDelay} />

      {isOver
        ? <Text style={ccS.overText}>+{excess.toLocaleString()} kcal over target</Text>
        : <Text style={ccS.remainText}>{remaining.toLocaleString()} kcal remaining</Text>}

      {expanded && (
        <View style={ccS.expandedSection}>
          <ContribSection
            sources={sources}
            sourcesLoading={sourcesLoading}
            formatAmount={amt => `${Math.round(amt).toLocaleString()} kcal`}
          />
        </View>
      )}
    </TouchableOpacity>
  );
};

const ccS = StyleSheet.create({
  card: {
    backgroundColor: '#181818', borderRadius: 16,
    borderWidth: 1, borderColor: 'rgba(255,255,255,0.06)',
    padding: 14, marginBottom: 8,
  },
  header:   { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 6 },
  iconWrap: {
    width: 26, height: 26, borderRadius: 8,
    backgroundColor: 'rgba(255,255,255,0.06)',
    alignItems: 'center', justifyContent: 'center',
  },
  label:     { flex: 1, fontSize: 13, fontWeight: '600', color: ThemeColors.textSecondary },
  pct:       { fontSize: 12, fontWeight: '600' },
  valueRow:  { flexDirection: 'row', alignItems: 'baseline', gap: 4 },
  consumed:  { fontSize: 22, fontWeight: '700', color: ThemeColors.textPrimary, letterSpacing: -0.3 },
  unit:      { fontSize: 12, fontWeight: '400', color: ThemeColors.textMuted },
  target:    { fontSize: 13, color: ThemeColors.textMuted },
  overText:  { fontSize: 11, fontWeight: '600', color: '#FF453A', marginTop: 6 },
  remainText:{ fontSize: 11, color: ThemeColors.textMuted, marginTop: 6 },
  expandedSection: {
    marginTop: 14, borderTopWidth: 1,
    borderTopColor: 'rgba(255,255,255,0.06)', paddingTop: 12,
  },
});

// ─── Macro Card (expandable) ───────────────────────────────────────────────

interface MacroCardProps {
  label:          string;
  consumed:       number;
  target:         number;
  unit?:          string;
  icon:           keyof typeof Ionicons.glyphMap;
  animDelay?:     number;
  sources:        SourceItem[];
  sourcesLoading: boolean;
}

const MacroCard: React.FC<MacroCardProps> = ({
  label, consumed, target, unit = 'g', icon, animDelay = 0, sources, sourcesLoading,
}) => {
  const [expanded, setExpanded] = useState(false);
  const rawPct     = target > 0 ? consumed / target : 0;
  const pctDisplay = Math.round(rawPct * 100);
  const isOver     = rawPct > 1;
  const excess     = consumed - target;
  const remaining  = target - consumed;
  const barColor   = getBarColor(rawPct);

  const toggle = () => {
    LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);
    setExpanded(v => !v);
  };

  return (
    <TouchableOpacity style={mcS.card} onPress={toggle} activeOpacity={0.85}>
      <View style={mcS.header}>
        <View style={mcS.iconWrap}>
          <Ionicons name={icon} size={15} color={ThemeColors.textMuted} />
        </View>
        <Text style={mcS.label}>{label}</Text>
        <Text style={[mcS.pct, { color: barColor }]}>
          {isOver ? `+${pctDisplay - 100}% over` : `${pctDisplay}%`}
        </Text>
        <Ionicons
          name={expanded ? 'chevron-up' : 'chevron-down'}
          size={13} color={ThemeColors.textMuted} style={{ marginLeft: 6 }}
        />
      </View>

      <View style={mcS.valueRow}>
        <Text style={mcS.consumed}>
          {consumed.toFixed(1)}<Text style={mcS.unitText}>{unit}</Text>
        </Text>
        <Text style={mcS.target}>/ {target.toFixed(0)}{unit}</Text>
      </View>

      <SmartBar value={consumed} max={target} delay={animDelay} />

      {isOver
        ? <Text style={mcS.overText}>+{excess.toFixed(1)}{unit} over target</Text>
        : <Text style={mcS.remainText}>{remaining.toFixed(1)}{unit} remaining</Text>}

      {expanded && (
        <View style={mcS.expandedSection}>
          <ContribSection
            sources={sources}
            sourcesLoading={sourcesLoading}
            formatAmount={amt => `${amt.toFixed(1)}${unit}`}
          />
        </View>
      )}
    </TouchableOpacity>
  );
};

const mcS = StyleSheet.create({
  card: {
    backgroundColor: '#181818', borderRadius: 16,
    borderWidth: 1, borderColor: 'rgba(255,255,255,0.06)',
    padding: 14, marginBottom: 8,
  },
  header:   { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 6 },
  iconWrap: {
    width: 26, height: 26, borderRadius: 8,
    backgroundColor: 'rgba(255,255,255,0.06)',
    alignItems: 'center', justifyContent: 'center',
  },
  label:         { flex: 1, fontSize: 13, fontWeight: '600', color: ThemeColors.textSecondary },
  pct:           { fontSize: 12, fontWeight: '600' },
  valueRow:      { flexDirection: 'row', alignItems: 'baseline', gap: 4 },
  consumed:      { fontSize: 22, fontWeight: '700', color: ThemeColors.textPrimary, letterSpacing: -0.3 },
  unitText:      { fontSize: 12, fontWeight: '400', color: ThemeColors.textMuted },
  target:        { fontSize: 13, color: ThemeColors.textMuted },
  overText:      { fontSize: 11, fontWeight: '600', color: '#FF453A', marginTop: 6 },
  remainText:    { fontSize: 11, color: ThemeColors.textMuted, marginTop: 6 },
  expandedSection: {
    marginTop: 14, borderTopWidth: 1,
    borderTopColor: 'rgba(255,255,255,0.06)', paddingTop: 12,
  },
});

// ─── Macro Distribution ───────────────────────────────────────────────────

const MacroDistribution: React.FC<{ protein: number; carbs: number; fat: number }> = ({
  protein, carbs, fat,
}) => {
  const total = protein * 4 + carbs * 4 + fat * 9;
  const pPct = total > 0 ? (protein * 4 / total) * 100 : 33;
  const cPct = total > 0 ? (carbs   * 4 / total) * 100 : 34;
  const fPct = total > 0 ? (fat     * 9 / total) * 100 : 33;
  const anim = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    Animated.timing(anim, { toValue: 1, duration: 900, delay: 200, useNativeDriver: false }).start();
  }, [total]);
  const sP = anim.interpolate({ inputRange: [0, 1], outputRange: [0, pPct] });
  const sC = anim.interpolate({ inputRange: [0, 1], outputRange: [0, cPct] });
  const sF = anim.interpolate({ inputRange: [0, 1], outputRange: [0, fPct] });

  return (
    <View style={distS.card}>
      <Text style={distS.title}>MACRO DISTRIBUTION</Text>
      {total === 0 ? <Text style={distS.empty}>No data yet</Text> : (
        <>
          <View style={distS.bar}>
            <Animated.View style={[distS.seg, { backgroundColor: '#E0E0E0', flex: sP }]} />
            <Animated.View style={[distS.seg, { backgroundColor: '#888888', flex: sC }]} />
            <Animated.View style={[distS.seg, { backgroundColor: '#444444', flex: sF }]} />
          </View>
          <View style={distS.legend}>
            {[
              { label: `Protein ${pPct.toFixed(0)}%`, color: '#E0E0E0' },
              { label: `Carbs ${cPct.toFixed(0)}%`,   color: '#888888' },
              { label: `Fat ${fPct.toFixed(0)}%`,     color: '#444444' },
            ].map(item => (
              <View key={item.label} style={distS.legendItem}>
                <View style={[distS.dot, { backgroundColor: item.color }]} />
                <Text style={distS.legendText}>{item.label}</Text>
              </View>
            ))}
          </View>
        </>
      )}
    </View>
  );
};

const distS = StyleSheet.create({
  card: {
    backgroundColor: '#181818', borderRadius: 16,
    borderWidth: 1, borderColor: 'rgba(255,255,255,0.06)',
    padding: 14, marginBottom: 8,
  },
  title:  { fontSize: 10, fontWeight: '700', color: ThemeColors.textMuted, letterSpacing: 1.5, textTransform: 'uppercase', marginBottom: 12 },
  empty:  { fontSize: 13, color: ThemeColors.textMuted, textAlign: 'center', paddingVertical: 12 },
  bar:    { flexDirection: 'row', height: 10, borderRadius: 99, overflow: 'hidden', gap: 2, marginBottom: 12 },
  seg:    { borderRadius: 99 },
  legend: { flexDirection: 'row', justifyContent: 'space-around' },
  legendItem: { flexDirection: 'row', alignItems: 'center', gap: 5 },
  dot:    { width: 8, height: 8, borderRadius: 4 },
  legendText: { fontSize: 11, color: ThemeColors.textSecondary },
});

// ─── Macro Insights (top single sources) ─────────────────────────────────

const MacroInsights: React.FC<{
  sources: ReturnType<typeof useNutrition>['macroSources'];
  loading: boolean;
}> = ({ sources, loading }) => {
  const rows = [
    { label: 'Highest Protein Source', data: sources.protein[0], unit: 'g', icon: 'fitness-outline' as const },
    { label: 'Highest Carb Source',    data: sources.carbs[0],   unit: 'g', icon: 'leaf-outline' as const },
    { label: 'Highest Fat Source',     data: sources.fat[0],     unit: 'g', icon: 'water-outline' as const },
    { label: 'Highest Fiber Source',   data: sources.fiber[0],   unit: 'g', icon: 'apps-outline' as const },
  ].filter(r => r.data);

  if (loading) {
    return (
      <View style={hiS.card}>
        <Text style={hiS.title}>MACRO INSIGHTS</Text>
        <ActivityIndicator size="small" color={ThemeColors.textMuted} style={{ marginVertical: 12 }} />
      </View>
    );
  }
  if (rows.length === 0) return null;

  return (
    <View style={hiS.card}>
      <Text style={hiS.title}>MACRO INSIGHTS</Text>
      {rows.map((r, idx) => (
        <View key={r.label} style={[hiS.row, idx === rows.length - 1 && { borderBottomWidth: 0 }]}>
          <View style={hiS.iconWrap}>
            <Ionicons name={r.icon} size={14} color={ThemeColors.textMuted} />
          </View>
          <View style={{ flex: 1 }}>
            <Text style={hiS.rowLabel}>{r.label}</Text>
            <Text style={hiS.rowFood}>{r.data!.foodName}</Text>
          </View>
          <Text style={hiS.rowValue}>{r.data!.amount.toFixed(1)}{r.unit}</Text>
        </View>
      ))}
    </View>
  );
};

const hiS = StyleSheet.create({
  card: {
    backgroundColor: '#181818', borderRadius: 16,
    borderWidth: 1, borderColor: 'rgba(255,255,255,0.06)',
    padding: 14, marginBottom: 8,
  },
  title:   { fontSize: 10, fontWeight: '700', color: ThemeColors.textMuted, letterSpacing: 1.5, textTransform: 'uppercase', marginBottom: 10 },
  row: {
    flexDirection: 'row', alignItems: 'center',
    paddingVertical: 10, gap: 10,
    borderBottomWidth: 1, borderBottomColor: 'rgba(255,255,255,0.04)',
  },
  iconWrap: {
    width: 28, height: 28, borderRadius: 8,
    backgroundColor: 'rgba(255,255,255,0.05)',
    alignItems: 'center', justifyContent: 'center',
  },
  rowLabel: { fontSize: 10, color: ThemeColors.textMuted, textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 2 },
  rowFood:  { fontSize: 13, fontWeight: '600', color: ThemeColors.textPrimary },
  rowValue: { fontSize: 15, fontWeight: '700', color: ThemeColors.textPrimary },
});

// ─── Bullet summary ───────────────────────────────────────────────────────

const Bullet: React.FC<{ text: string; positive: boolean }> = ({ text, positive }) => (
  <View style={bullS.row}>
    <Ionicons
      name={positive ? 'checkmark-circle' : 'alert-circle'}
      size={16}
      color={positive ? ThemeColors.success : ThemeColors.textMuted}
    />
    <Text style={bullS.text}>{text}</Text>
  </View>
);

const bullS = StyleSheet.create({
  row: { flexDirection: 'row', alignItems: 'flex-start', gap: 8, paddingVertical: 6, borderBottomWidth: 1, borderBottomColor: 'rgba(255,255,255,0.04)' },
  text: { fontSize: 13, color: ThemeColors.textSecondary, flex: 1, lineHeight: 18 },
});

// ─── Empty State ──────────────────────────────────────────────────────────

const EmptyState = () => (
  <View style={emptyS.wrap}>
    <Ionicons name="restaurant-outline" size={36} color={ThemeColors.textMuted} />
    <Text style={emptyS.heading}>No meals logged today</Text>
    <Text style={emptyS.sub}>Log a meal to see your nutrition insights and source breakdown.</Text>
  </View>
);

const emptyS = StyleSheet.create({
  wrap:    { alignItems: 'center', paddingTop: 48, paddingBottom: 24, gap: 10 },
  heading: { fontSize: 15, fontWeight: '600', color: ThemeColors.textSecondary },
  sub:     { fontSize: 13, color: ThemeColors.textMuted, textAlign: 'center', lineHeight: 19, paddingHorizontal: 16 },
});

// ─── Main FoodInsightsScreen ──────────────────────────────────────────────

export const FoodInsightsScreen = () => {
  const { navigateTo } = useNavigation();
  const {
    logs, summary, goal,
    macroSources, sourcesLoading, loading,
  } = useNutrition();

  // Derived values
  const cal   = Math.round(summary?.totalCalories  ?? 0);
  const pro   = summary?.totalProtein              ?? 0;
  const carb  = summary?.totalCarbs                ?? 0;
  const fat   = summary?.totalFat                  ?? 0;
  const fiber = summary?.totalFiber                ?? 0;
  const sugar = summary?.totalFreeSugar            ?? 0;

  const tCal   = Math.round(goal?.targetCalories      ?? 2000);
  const tPro   = goal?.targetProtein                  ?? 120;
  const tCarb  = goal?.targetCarbohydrates            ?? 200;
  const tFat   = goal?.targetFat                      ?? 70;
  const tFiber = 25;
  const tSugar = 50;

  const calRemaining = tCal - cal;
  const calExcess    = cal - tCal;
  const noLogs = !loading && logs.length === 0;

  // Bullets
  const pctOf = (v: number, t: number) => t > 0 ? v / t : 0;
  const bullets: { text: string; positive: boolean }[] = [];
  if (!noLogs) {
    if (pctOf(pro, tPro) >= 0.8)        bullets.push({ text: 'Protein intake is on track.', positive: true });
    else if (pctOf(pro, tPro) < 0.4)    bullets.push({ text: 'Protein intake is low. Add lean meats, eggs or legumes.', positive: false });
    else                                  bullets.push({ text: 'Protein intake is moderate — keep going.', positive: true });

    if (pctOf(carb, tCarb) > 1.1)       bullets.push({ text: 'Carbohydrate intake has exceeded the daily target.', positive: false });
    else if (pctOf(carb, tCarb) >= 0.7) bullets.push({ text: 'Carbohydrate intake is on track.', positive: true });
    else                                  bullets.push({ text: 'Carbohydrate intake is below target.', positive: false });

    if (pctOf(fat, tFat) > 1.1)         bullets.push({ text: 'Fat intake has exceeded the daily target.', positive: false });
    else                                  bullets.push({ text: 'Fat intake is within range.', positive: true });

    if (pctOf(fiber, tFiber) < 0.5)     bullets.push({ text: 'Fiber intake is low. Add vegetables, fruits or whole grains.', positive: false });
    else                                  bullets.push({ text: 'Fiber intake is adequate.', positive: true });

    if (calRemaining > 0)                bullets.push({ text: `You have ${calRemaining.toLocaleString()} kcal remaining today.`, positive: true });
    else                                  bullets.push({ text: 'Daily calorie target has been reached.', positive: false });
  }

  return (
    <AppLayout>
      <ScrollView
        contentContainerStyle={s.container}
        showsVerticalScrollIndicator={false}
      >
        {/* Back Row */}
        <TouchableOpacity style={s.backRow} onPress={() => navigateTo('FOOD')} activeOpacity={0.75}>
          <Ionicons name="arrow-back" size={18} color={ThemeColors.textSecondary} />
          <Text style={s.backText}>Nutrition Insights</Text>
        </TouchableOpacity>

        {loading ? (
          <View style={s.centered}>
            <ActivityIndicator color="#FFFFFF" />
          </View>
        ) : noLogs ? (
          <EmptyState />
        ) : (
          <>
            {/* ── Calorie Banner ── */}
            <View style={s.calCard}>
              <View>
                <Text style={s.calLabel}>TODAY'S CALORIES</Text>
                <Text style={s.calValue}>{cal.toLocaleString()} kcal</Text>
                <Text style={s.calTargetText}>Target: {tCal.toLocaleString()} kcal</Text>
              </View>
              <View style={s.calRight}>
                {calRemaining > 0 ? (
                  <>
                    <Text style={s.calRightLabel}>Remaining</Text>
                    <Text style={s.calRightValue}>{calRemaining.toLocaleString()}</Text>
                    <Text style={s.calRightUnit}>kcal</Text>
                  </>
                ) : (
                  <>
                    <Text style={s.calRightLabel}>Over by</Text>
                    <Text style={[s.calRightValue, { color: '#FF453A' }]}>+{calExcess.toLocaleString()}</Text>
                    <Text style={s.calRightUnit}>kcal</Text>
                  </>
                )}
              </View>
            </View>

            {/* ── Calorie Breakdown ── */}
            <Text style={s.sectionLabel}>CALORIE BREAKDOWN</Text>
            <CalorieCard
              consumed={cal}
              target={tCal}
              sources={macroSources.calories}
              sourcesLoading={sourcesLoading}
              animDelay={0}
            />

            {/* ── Macronutrients ── */}
            <Text style={s.sectionLabel}>MACRONUTRIENTS</Text>
            <Text style={s.sectionHint}>Tap a card to see food sources</Text>

            <MacroCard label="Protein" consumed={pro}   target={tPro}   unit="g" icon="fitness-outline" animDelay={0}   sources={macroSources.protein} sourcesLoading={sourcesLoading} />
            <MacroCard label="Carbs"   consumed={carb}  target={tCarb}  unit="g" icon="leaf-outline"    animDelay={80}  sources={macroSources.carbs}   sourcesLoading={sourcesLoading} />
            <MacroCard label="Fat"     consumed={fat}   target={tFat}   unit="g" icon="water-outline"   animDelay={160} sources={macroSources.fat}     sourcesLoading={sourcesLoading} />
            <MacroCard label="Fiber"   consumed={fiber} target={tFiber} unit="g" icon="apps-outline"    animDelay={240} sources={macroSources.fiber}   sourcesLoading={sourcesLoading} />
            <MacroCard label="Sugar"   consumed={sugar} target={tSugar} unit="g" icon="grid-outline"    animDelay={320} sources={macroSources.sugar}   sourcesLoading={sourcesLoading} />

            {/* ── Distribution ── */}
            <Text style={s.sectionLabel}>DISTRIBUTION</Text>
            <MacroDistribution protein={pro} carbs={carb} fat={fat} />

            {/* ── Top Contributors ── */}
            <Text style={s.sectionLabel}>TOP CONTRIBUTORS</Text>
            <MacroInsights sources={macroSources} loading={sourcesLoading} />

            {/* ── Summary ── */}
            <Text style={s.sectionLabel}>SUMMARY</Text>
            <View style={s.summaryCard}>
              {bullets.map((b, i) => <Bullet key={i} text={b.text} positive={b.positive} />)}
            </View>
          </>
        )}
      </ScrollView>
    </AppLayout>
  );
};

const s = StyleSheet.create({
  container:   { padding: Spacing.lg, paddingBottom: 40 },
  centered:    { alignItems: 'center', marginTop: 48 },
  backRow:     { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 16 },
  backText:    { fontSize: 15, fontWeight: '600', color: ThemeColors.textPrimary },
  calCard: {
    backgroundColor: '#181818', borderRadius: 18,
    borderWidth: 1, borderColor: 'rgba(255,255,255,0.06)',
    padding: 16, flexDirection: 'row',
    justifyContent: 'space-between', alignItems: 'center',
    marginBottom: 16,
  },
  calLabel:      { fontSize: 10, fontWeight: '700', color: ThemeColors.textMuted, letterSpacing: 1.2, textTransform: 'uppercase', marginBottom: 4 },
  calValue:      { fontSize: 24, fontWeight: '700', color: ThemeColors.textPrimary, letterSpacing: -0.3 },
  calTargetText: { fontSize: 12, color: ThemeColors.textMuted, marginTop: 2 },
  calRight: {
    alignItems: 'flex-end', backgroundColor: 'rgba(255,255,255,0.04)',
    borderRadius: 12, padding: 12, minWidth: 88,
  },
  calRightLabel: { fontSize: 10, color: ThemeColors.textMuted, textTransform: 'uppercase', letterSpacing: 0.5 },
  calRightValue: { fontSize: 20, fontWeight: '700', color: ThemeColors.textPrimary, marginTop: 2 },
  calRightUnit:  { fontSize: 11, color: ThemeColors.textMuted },
  sectionLabel: {
    fontSize: 10, fontWeight: '700', color: ThemeColors.textMuted,
    letterSpacing: 1.5, textTransform: 'uppercase',
    marginBottom: 4, marginTop: 8,
  },
  sectionHint: { fontSize: 11, color: 'rgba(255,255,255,0.2)', marginBottom: 10 },
  summaryCard: {
    backgroundColor: '#181818', borderRadius: 16,
    borderWidth: 1, borderColor: 'rgba(255,255,255,0.06)',
    padding: 14, marginBottom: 8,
  },
});
