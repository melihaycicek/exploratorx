'use client';

import { getDecisionBadgeClass, getDecisionColor, formatTime, formatKmh } from '@/lib/utils';
import type { CdrAnomalyEvent, FraudAlertEvent } from '@/lib/types';
import { MapPin, Clock, Gauge } from 'lucide-react';
import clsx from 'clsx';

type AnomalyEvent = CdrAnomalyEvent | FraudAlertEvent;

interface LiveFeedProps {
  events: AnomalyEvent[];
  mode: 'CDR' | 'PAYMENT';
}

function CdrEventRow({ ev }: { ev: CdrAnomalyEvent }) {
  const color = getDecisionColor(ev.cdrDecision);
  const isSplit = ev.cdrDecision === 'SPLIT_SIGNAL';
  return (
    <div
      className={clsx(
        'animate-slide-in glass rounded-lg p-3 border flex flex-col gap-2 transition-all',
        isSplit ? 'pulse-split' : '',
      )}
      style={{ borderColor: color + '33' }}
    >
      <div className="flex items-center justify-between gap-2">
        <span
          className={clsx('px-2 py-0.5 rounded text-[10px] font-bold border', getDecisionBadgeClass(ev.cdrDecision))}
        >
          {ev.cdrDecision.replace(/_/g, ' ')}
        </span>
        <span className="text-[10px] text-slate-500 font-mono">{formatTime(ev.detectedAt)}</span>
      </div>

      <div className="flex items-center gap-1.5 text-xs text-slate-300">
        <MapPin size={11} className="text-slate-500 shrink-0" />
        <span className="truncate font-medium">
          {ev.fromCity || '?'} → {ev.toCity}
        </span>
      </div>

      <div className="flex items-center gap-3 text-[10px] text-slate-500">
        <span className="flex items-center gap-1">
          <Gauge size={10} />
          {formatKmh(ev.requiredSpeedKmh)}
        </span>
        <span className="flex items-center gap-1">
          <span className="font-mono" style={{ color }}>{ev.distanceKm?.toFixed(0)} km</span>
        </span>
        <span className="ml-auto font-mono text-slate-400">
          SUB: {ev.entityId?.slice(-6)}
        </span>
      </div>

      {ev.riskScore >= 0 && (
        <div className="w-full bg-white/5 rounded-full h-1">
          <div
            className="h-1 rounded-full transition-all duration-500"
            style={{ width: `${Math.min(ev.riskScore, 100)}%`, background: color }}
          />
        </div>
      )}
    </div>
  );
}

function PaymentEventRow({ ev }: { ev: FraudAlertEvent }) {
  const color = getDecisionColor(ev.fraudDecision);
  return (
    <div
      className={clsx('animate-slide-in glass rounded-lg p-3 border flex flex-col gap-2')}
      style={{ borderColor: color + '33' }}
    >
      <div className="flex items-center justify-between gap-2">
        <span
          className={clsx('px-2 py-0.5 rounded text-[10px] font-bold border', getDecisionBadgeClass(ev.fraudDecision))}
        >
          {ev.fraudDecision.replace(/_/g, ' ')}
        </span>
        <span className="text-[10px] text-slate-500 font-mono">{formatTime(ev.detectedAt)}</span>
      </div>

      <div className="flex items-center gap-1.5 text-xs text-slate-300">
        <span className="font-semibold" style={{ color }}>
          {ev.amount ? `${ev.amount.toFixed(2)} ${ev.currency}` : '—'}
        </span>
        <span className="text-slate-500">·</span>
        <span className="truncate">{ev.merchantName || 'Unknown merchant'}</span>
      </div>

      <div className="flex flex-wrap items-center gap-1.5 text-[10px]">
        {ev.impossibleTravel && (
          <span className="badge-impossible px-1.5 py-0.5 rounded border text-[9px]">🔴 Travel</span>
        )}
        {ev.velocityFraud && (
          <span className="badge-review px-1.5 py-0.5 rounded border text-[9px]">🟡 Velocity</span>
        )}
        {ev.duplicatePayment && (
          <span className="badge-duplicate px-1.5 py-0.5 rounded border text-[9px]">⬜ Dup</span>
        )}
        {ev.geoMismatch && (
          <span className="badge-challenge px-1.5 py-0.5 rounded border text-[9px]">🟠 GeoMismatch</span>
        )}
        <span className="ml-auto font-mono text-slate-500">{ev.entityId?.slice(-8)}</span>
      </div>

      <div className="w-full bg-white/5 rounded-full h-1">
        <div
          className="h-1 rounded-full transition-all duration-500"
          style={{ width: `${Math.min(ev.riskScore, 100)}%`, background: color }}
        />
      </div>
    </div>
  );
}

export default function LiveFeed({ events, mode }: LiveFeedProps) {
  return (
    <div id="live-feed-panel" className="flex flex-col gap-2 overflow-y-auto" style={{ maxHeight: 'calc(100vh - 300px)' }}>
      {events.length === 0 ? (
        <div className="glass rounded-xl p-8 text-center">
          <p className="text-slate-500 text-sm">No events yet.</p>
          <p className="text-slate-600 text-xs mt-1">Trigger a scenario to see live anomalies.</p>
        </div>
      ) : (
        events.map((ev) =>
          mode === 'CDR' ? (
            <CdrEventRow key={(ev as CdrAnomalyEvent).anomalyId} ev={ev as CdrAnomalyEvent} />
          ) : (
            <PaymentEventRow key={(ev as FraudAlertEvent).anomalyId} ev={ev as FraudAlertEvent} />
          )
        )
      )}
    </div>
  );
}
