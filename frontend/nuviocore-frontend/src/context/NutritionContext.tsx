import React, { createContext, useContext, useState, useCallback } from 'react';
import { fetchClient } from '../api/FetchClient';
import {
  FoodLogResponse,
  DailyNutritionSummaryResponse,
  GoalResponse,
  FoodNutritionEntity,
} from '../types';

// ── Exported types ─────────────────────────────────────────────────────────

export interface SourceItem {
  foodName: string;
  amount: number;
}

export interface MacroSources {
  calories: SourceItem[];
  protein:  SourceItem[];
  carbs:    SourceItem[];
  fat:      SourceItem[];
  fiber:    SourceItem[];
  sugar:    SourceItem[];
}

interface FoodContrib {
  foodName: string;
  calories: number;
  protein:  number;
  carbs:    number;
  fat:      number;
  fiber:    number;
  sugar:    number;
}

const EMPTY_SOURCES: MacroSources = {
  calories: [], protein: [], carbs: [], fat: [], fiber: [], sugar: [],
};

// ── Source computation (uses existing search API — no backend changes) ──────

async function computeSources(logs: FoodLogResponse[]): Promise<MacroSources> {
  if (logs.length === 0) return EMPTY_SOURCES;

  const uniqueNames = [...new Set(logs.map(l => l.foodName))];
  const map: Record<string, FoodNutritionEntity> = {};

  await Promise.allSettled(
    uniqueNames.map(async name => {
      try {
        const results: FoodNutritionEntity[] = await fetchClient(
          `/api/nutrition/search?query=${encodeURIComponent(name)}`
        );
        if (results.length > 0) map[name] = results[0];
      } catch { /* silently skip unresolved foods */ }
    })
  );

  // Aggregate — handles same food logged multiple times
  const agg: Record<string, FoodContrib> = {};
  for (const log of logs) {
    const n = map[log.foodName];
    if (!n) continue;
    const ratio = log.gramsConsumed / (n.servingSizeG || 100);
    if (!agg[log.foodName]) {
      agg[log.foodName] = {
        foodName: log.foodName,
        calories: 0, protein: 0, carbs: 0, fat: 0, fiber: 0, sugar: 0,
      };
    }
    agg[log.foodName].calories += (n.calories    || 0) * ratio;
    agg[log.foodName].protein  += (n.proteinG    || 0) * ratio;
    agg[log.foodName].carbs    += (n.carbsG      || 0) * ratio;
    agg[log.foodName].fat      += (n.fatG        || 0) * ratio;
    agg[log.foodName].fiber    += (n.fiberG      || 0) * ratio;
    agg[log.foodName].sugar    += (n.freeSugarG  || 0) * ratio;
  }

  const entries = Object.values(agg);
  const toSorted = (key: keyof Omit<FoodContrib, 'foodName'>) =>
    entries
      .map(e => ({ foodName: e.foodName, amount: e[key] }))
      .filter(x => x.amount > 0.01)
      .sort((a, b) => b.amount - a.amount);

  return {
    calories: toSorted('calories'),
    protein:  toSorted('protein'),
    carbs:    toSorted('carbs'),
    fat:      toSorted('fat'),
    fiber:    toSorted('fiber'),
    sugar:    toSorted('sugar'),
  };
}

// ── Context definition ─────────────────────────────────────────────────────

interface NutritionContextType {
  logs:           FoodLogResponse[];
  summary:        DailyNutritionSummaryResponse | null;
  goal:           GoalResponse | null;
  macroSources:   MacroSources;
  sourcesLoading: boolean;
  loading:        boolean;
  /** Call after any add / edit / delete to keep all screens in sync. */
  refresh:        () => Promise<void>;
}

const NutritionContext = createContext<NutritionContextType | undefined>(undefined);

// ── Provider ───────────────────────────────────────────────────────────────

export const NutritionProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [logs, setLogs]                     = useState<FoodLogResponse[]>([]);
  const [summary, setSummary]               = useState<DailyNutritionSummaryResponse | null>(null);
  const [goal, setGoal]                     = useState<GoalResponse | null>(null);
  const [macroSources, setMacroSources]     = useState<MacroSources>(EMPTY_SOURCES);
  const [sourcesLoading, setSourcesLoading] = useState(false);
  const [loading, setLoading]               = useState(false);

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const [g, s, l]: [GoalResponse, DailyNutritionSummaryResponse, FoodLogResponse[]] =
        await Promise.all([
          fetchClient('/api/goals'),
          fetchClient('/api/food/summary/today'),
          fetchClient('/api/food/logs/today'),
        ]);
      setGoal(g);
      setSummary(s);
      setLogs(l);

      // Compute sources in background — does not block the UI update
      setSourcesLoading(true);
      computeSources(l)
        .then(setMacroSources)
        .catch(() => {})
        .finally(() => setSourcesLoading(false));
    } catch (err: any) {
      // Screens handle their own error display; context silently handles failures
      console.log('[NutritionContext] refresh error:', err?.message);
    } finally {
      setLoading(false);
    }
  }, []);

  return (
    <NutritionContext.Provider
      value={{ logs, summary, goal, macroSources, sourcesLoading, loading, refresh }}
    >
      {children}
    </NutritionContext.Provider>
  );
};

// ── Hook ───────────────────────────────────────────────────────────────────

export const useNutrition = (): NutritionContextType => {
  const ctx = useContext(NutritionContext);
  if (!ctx) throw new Error('useNutrition must be used inside <NutritionProvider>');
  return ctx;
};
