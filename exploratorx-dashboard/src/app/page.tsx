'use client';

import { useState, useCallback, useRef, useEffect } from 'react';
import Header from '@/components/Header';
import StatsCards from '@/components/StatsCards';
import ScenarioButtons from '@/components/ScenarioButtons';
import LiveFeed from '@/components/LiveFeed';
import GermanyMap from '@/features/map/GermanyMap';
import { useWebSocket, DemoAPI, type ConnectionStatus } from '@/lib/websocket';
import type {
  DashboardMode,
  DashboardEnvelope,
  CdrAnomalyEvent,
  FraudAlertEvent,
  StatsPayload,
  MapRoute,
} from '@/lib/types';
import { getDecisionColor, GERMANY_CITIES } from '@/lib/utils';


const MAX_EVENTS = 50;
const MAX_ROUTES = 30;

function toRoute(ev: CdrAnomalyEvent | FraudAlertEvent): MapRoute | null {
  const fromCity = ev.fromCity;
  const toCity = ev.toCity;
  if (!fromCity || !toCity) return null;

  const from = GERMANY_CITIES[fromCity];
  const to = GERMANY_CITIES[toCity];
  if (!from || !to) return null;

  return {
    id: ev.anomalyId || Math.random().toString(),
    from,
    to,
    fromCity,
    toCity,
    decision: 'cdrDecision' in ev ? (ev as CdrAnomalyEvent).cdrDecision : (ev as FraudAlertEvent).fraudDecision,
    riskScore: ev.riskScore,
    timestamp: ev.detectedAt,
    mode: ev.mode as 'CDR' | 'PAYMENT',
  };
}

export default function DashboardPage() {
  const [mode, setMode] = useState<DashboardMode>('CDR');
  const [stats, setStats] = useState<StatsPayload | null>(null);
  const [cdrEvents, setCdrEvents] = useState<CdrAnomalyEvent[]>([]);
  const [payEvents, setPayEvents] = useState<FraudAlertEvent[]>([]);
  const [routes, setRoutes] = useState<MapRoute[]>([]);
  const [wsStatus, setWsStatus] = useState<ConnectionStatus>('connecting');

  // Load initial stats
  useEffect(() => {
    DemoAPI.stats().then(setStats).catch(console.error);
    const interval = setInterval(() => {
      DemoAPI.stats().then(setStats).catch(console.error);
    }, 5000);
    return () => clearInterval(interval);
  }, []);

  // WebSocket: live anomalies
  const anomalyStatus = useWebSocket('/topic/live-anomalies', useCallback((env: DashboardEnvelope) => {
    if (env.type === 'CDR_ANOMALY') {
      const ev = env.payload as CdrAnomalyEvent;
      setCdrEvents((prev) => [ev, ...prev].slice(0, MAX_EVENTS));
      const route = toRoute(ev);
      if (route) setRoutes((prev) => [route, ...prev].slice(0, MAX_ROUTES));
    }
    if (env.type === 'PAYMENT_FRAUD') {
      const ev = env.payload as FraudAlertEvent;
      setPayEvents((prev) => [ev, ...prev].slice(0, MAX_EVENTS));
      const route = toRoute(ev);
      if (route) setRoutes((prev) => [route, ...prev].slice(0, MAX_ROUTES));
    }
  }, []));

  // WebSocket: stats updates
  useWebSocket('/topic/stats', useCallback((env: DashboardEnvelope) => {
    if (env.type === 'STATS_UPDATE') {
      setStats(env.payload as StatsPayload);
    }
  }, []));

  useEffect(() => {
    setWsStatus(anomalyStatus);
  }, [anomalyStatus]);

  const handleReset = async () => {
    await DemoAPI.reset();
    setCdrEvents([]);
    setPayEvents([]);
    setRoutes([]);
    setStats(null);
    DemoAPI.stats().then(setStats).catch(console.error);
  };

  // Filter routes by mode
  const filteredRoutes = routes.filter((r) => r.mode === mode);
  const events = mode === 'CDR' ? cdrEvents : payEvents;

  return (
    <div className="flex flex-col h-full" style={{ background: 'var(--bg-900)' }}>
      <Header
        mode={mode}
        onModeChange={setMode}
        onReset={handleReset}
        status={wsStatus}
      />

      <div className="flex flex-1 overflow-hidden">
        {/* ─── Left sidebar ───────────────────────────────────────────── */}
        <aside className="w-72 shrink-0 flex flex-col gap-4 p-4 overflow-y-auto border-r border-white/8">
          <StatsCards stats={stats} mode={mode} />
          <ScenarioButtons mode={mode} />
        </aside>

        {/* ─── Center: Map ─────────────────────────────────────────────── */}
        <main className="flex-1 p-4 overflow-hidden">
          <div style={{ height: '100%' }}>
            <GermanyMap routes={filteredRoutes} />
          </div>
        </main>

        {/* ─── Right sidebar: Live feed ─────────────────────────────────── */}
        <aside className="w-80 shrink-0 flex flex-col gap-3 p-4 overflow-hidden border-l border-white/8">
          <div className="flex items-center justify-between">
            <h2 className="text-[11px] text-slate-400 font-semibold uppercase tracking-wider">
              Live Anomaly Feed
            </h2>
            <span className="text-[10px] font-mono text-slate-600">{events.length}/{MAX_EVENTS}</span>
          </div>
          <LiveFeed events={events} mode={mode} />
        </aside>
      </div>
    </div>
  );
}
