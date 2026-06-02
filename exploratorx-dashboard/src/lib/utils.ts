import type { CdrDecision, FraudDecision } from './types';

// ─── Decision → color mapping (exact hex from spec) ───────────────────────────

const CDR_COLORS: Record<CdrDecision, string> = {
  NORMAL:                  '#22c55e',
  SUSPICIOUS_MOVEMENT:     '#eab308',
  SUSPICIOUS_MOVEMENT_HIGH:'#eab308',
  IMPOSSIBLE_SIGNAL:       '#ef4444',
  SPLIT_SIGNAL:            '#a855f7',
  OUT_OF_ORDER_EVENT:      '#94a3b8',
};

const PAYMENT_COLORS: Record<FraudDecision, string> = {
  APPROVED:           '#22c55e',
  REVIEW_REQUIRED:    '#eab308',
  CHALLENGE_REQUIRED: '#f97316',
  BLOCKED:            '#ef4444',
  DUPLICATE_IGNORED:  '#6b7280',
};

export function getDecisionColor(decision: string): string {
  return (
    CDR_COLORS[decision as CdrDecision] ??
    PAYMENT_COLORS[decision as FraudDecision] ??
    '#94a3b8'
  );
}

export function getDecisionBadgeClass(decision: string): string {
  const map: Record<string, string> = {
    NORMAL:                   'badge-normal',
    SUSPICIOUS_MOVEMENT:      'badge-suspicious',
    SUSPICIOUS_MOVEMENT_HIGH: 'badge-high',
    IMPOSSIBLE_SIGNAL:        'badge-impossible',
    SPLIT_SIGNAL:             'badge-split',
    OUT_OF_ORDER_EVENT:       'badge-duplicate',
    APPROVED:                 'badge-approved',
    REVIEW_REQUIRED:          'badge-review',
    CHALLENGE_REQUIRED:       'badge-challenge',
    BLOCKED:                  'badge-blocked',
    DUPLICATE_IGNORED:        'badge-duplicate',
  };
  return map[decision] ?? 'badge-duplicate';
}

export function isCritical(decision: string): boolean {
  return ['IMPOSSIBLE_SIGNAL', 'SPLIT_SIGNAL', 'BLOCKED'].includes(decision);
}

export function isSplitSignal(decision: string): boolean {
  return decision === 'SPLIT_SIGNAL';
}

// ─── German city coordinates ──────────────────────────────────────────────────

export const GERMANY_CITIES: Record<string, [number, number]> = {
  'Berlin':     [52.5200, 13.4050],
  'Hamburg':    [53.5511,  9.9937],
  'Munich':     [48.1351, 11.5820],
  'Frankfurt':  [50.1109,  8.6821],
  'Cologne':    [50.9333,  6.9500],
  'Stuttgart':  [48.7758,  9.1829],
  'Leipzig':    [51.3397, 12.3731],
  'Düsseldorf': [51.2217,  6.7762],
  'Bremen':     [53.0793,  8.8017],
  'Hannover':   [52.3759,  9.7320],
};

// ─── Time formatting ──────────────────────────────────────────────────────────

export function formatTime(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

export function formatDateTime(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleString('de-DE', {
    day: '2-digit', month: '2-digit', year: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
  });
}

// ─── Risk score → display ─────────────────────────────────────────────────────

export function riskLabel(score: number): string {
  if (score >= 90) return 'CRITICAL';
  if (score >= 61) return 'HIGH';
  if (score >= 31) return 'SUSPICIOUS';
  return 'NORMAL';
}

export function formatKmh(kmh: number | undefined): string {
  if (!kmh || kmh === Number.MAX_VALUE) return '∞';
  if (kmh > 100_000) return '>100k km/h';
  return `${Math.round(kmh).toLocaleString()} km/h`;
}
