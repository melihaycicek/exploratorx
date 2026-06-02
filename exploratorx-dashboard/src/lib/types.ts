// ─── Decision enums (must match Java enums) ──────────────────────────────────

export type CdrDecision =
  | 'NORMAL'
  | 'SUSPICIOUS_MOVEMENT'
  | 'SUSPICIOUS_MOVEMENT_HIGH'
  | 'IMPOSSIBLE_SIGNAL'
  | 'SPLIT_SIGNAL'
  | 'OUT_OF_ORDER_EVENT';

export type FraudDecision =
  | 'APPROVED'
  | 'REVIEW_REQUIRED'
  | 'CHALLENGE_REQUIRED'
  | 'BLOCKED'
  | 'DUPLICATE_IGNORED';

export type PayloadType =
  | 'CDR_SIGNAL'
  | 'CDR_ANOMALY'
  | 'PAYMENT_SIGNAL'
  | 'PAYMENT_FRAUD'
  | 'STATS_UPDATE'
  | 'DEMO_STARTED'
  | 'DEMO_RESET';

// ─── CDR Models ───────────────────────────────────────────────────────────────

export interface CdrSignal {
  id: number;
  subscriberId: string;
  eventTime: string;
  city: string;
  latitude: number;
  longitude: number;
  cellId: string | null;
  signalType: 'VOICE' | 'SMS' | 'DATA' | 'ROAMING' | 'HANDOVER';
  createdAt: string;
}

export interface CdrAnomalyEvent {
  anomalyId: string;
  mode: 'CDR';
  sourceId: string;
  entityId: string;
  fromCity: string;
  toCity: string;
  fromLatitude: number;
  fromLongitude: number;
  toLatitude: number;
  toLongitude: number;
  distanceKm: number;
  timeDiffMinutes: number;
  requiredSpeedKmh: number;
  riskScore: number;
  decision: string;
  cdrDecision: CdrDecision;
  reason: string;
  detectedAt: string;
  splitSignal: boolean;
  cellId: string | null;
  previousCellId: string | null;
}

// ─── Payment Models ───────────────────────────────────────────────────────────

export interface PaymentTransaction {
  id: number;
  transactionId: string;
  cardToken: string;
  maskedPan: string | null;
  last4: string | null;
  customerId: string;
  merchantId: string | null;
  merchantName: string | null;
  channel: 'POS' | 'ONLINE' | 'ATM' | 'CONTACTLESS' | 'MOBILE_WALLET';
  amount: number;
  currency: string;
  city: string;
  country: string;
  latitude: number;
  longitude: number;
  eventTime: string;
  paymentStatus: string;
  threeDsStatus: string | null;
  ipCountry: string | null;
  idempotencyKey: string | null;
}

export interface FraudAlertEvent {
  anomalyId: string;
  mode: 'PAYMENT';
  sourceId: string;
  entityId: string;
  fromCity: string | null;
  toCity: string;
  distanceKm: number;
  timeDiffMinutes: number;
  riskScore: number;
  decision: string;
  fraudDecision: FraudDecision;
  reason: string;
  detectedAt: string;
  transactionId: string;
  amount: number;
  currency: string;
  merchantName: string | null;
  impossibleTravel: boolean;
  velocityFraud: boolean;
  duplicatePayment: boolean;
  geoMismatch: boolean;
  velocityCount: number;
  threeDsStatus: string | null;
}

// ─── Dashboard Envelope ───────────────────────────────────────────────────────

export interface DashboardEnvelope {
  type: PayloadType;
  payload: CdrSignal | CdrAnomalyEvent | PaymentTransaction | FraudAlertEvent | StatsPayload;
  timestamp: string;
}

// ─── Stats ────────────────────────────────────────────────────────────────────

export interface StatsPayload {
  cdr: {
    totalEvents: number;
    impossibleSignals: number;
    splitSignals: number;
    suspiciousMovements: number;
  };
  payment: {
    totalEvents: number;
    blockedTransactions: number;
    challengeRequired: number;
    duplicateIgnored: number;
  };
}

// ─── Anomaly Log (from REST) ──────────────────────────────────────────────────

export interface AnomalyLogEntry {
  id: number;
  mode: 'CDR' | 'PAYMENT';
  sourceId: string;
  entityId: string;
  fromCity: string | null;
  toCity: string | null;
  fromLat: number | null;
  fromLon: number | null;
  toLat: number | null;
  toLon: number | null;
  timeDiffMinutes: number | null;
  distanceKm: number | null;
  requiredSpeedKmh: number | null;
  riskScore: number | null;
  decision: string;
  reason: string | null;
  detectedAt: string;
}

// ─── Map Route ────────────────────────────────────────────────────────────────

export interface MapRoute {
  id: string;
  from: [number, number];
  to: [number, number];
  fromCity: string;
  toCity: string;
  decision: string;
  riskScore: number;
  timestamp: string;
  mode: 'CDR' | 'PAYMENT';
}

export type DashboardMode = 'CDR' | 'PAYMENT';
