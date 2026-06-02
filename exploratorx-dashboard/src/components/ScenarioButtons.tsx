'use client';

import type { DashboardMode } from '@/lib/types';
import { DemoAPI } from '@/lib/websocket';
import { useState } from 'react';

interface ScenarioButtonsProps {
  mode: DashboardMode;
}

interface ScenarioBtn {
  id: string;
  label: string;
  action: () => Promise<void>;
  variant: string;
}

export default function ScenarioButtons({ mode }: ScenarioButtonsProps) {
  const [loading, setLoading] = useState<string | null>(null);

  const run = async (id: string, action: () => Promise<void>) => {
    setLoading(id);
    try { await action(); } catch (e) { console.error(e); }
    finally { setTimeout(() => setLoading(null), 1500); }
  };

  const cdrButtons: ScenarioBtn[] = [
    { id: 'cdr-normal',     label: '🟢 Normal Flow',          action: DemoAPI.cdrNormal,     variant: 'btn-cdr' },
    { id: 'cdr-suspicious', label: '🟡 Suspicious Movement',  action: DemoAPI.cdrSuspicious, variant: 'btn-cdr' },
    { id: 'cdr-impossible', label: '🔴 Impossible Signal',    action: DemoAPI.cdrImpossible, variant: 'btn-cdr' },
    { id: 'cdr-split',      label: '🟣 Split Signal',         action: DemoAPI.cdrSplit,      variant: 'btn-cdr' },
    { id: 'cdr-backfill',   label: '📼 Historical Backfill',  action: DemoAPI.backfillCdr,   variant: 'btn-ghost' },
  ];

  const payButtons: ScenarioBtn[] = [
    { id: 'pay-normal',    label: '🟢 Normal Flow',              action: DemoAPI.payNormal,     variant: 'btn-pay' },
    { id: 'pay-impossible',label: '🔴 Impossible Transaction',   action: DemoAPI.payImpossible, variant: 'btn-pay' },
    { id: 'pay-duplicate', label: '⬜ Duplicate Payment',         action: DemoAPI.payDuplicate,  variant: 'btn-pay' },
    { id: 'pay-velocity',  label: '🟡 Velocity Fraud',           action: DemoAPI.payVelocity,   variant: 'btn-pay' },
    { id: 'pay-challenge', label: '🟠 3DS Challenge',            action: DemoAPI.payChallenge,  variant: 'btn-pay' },
    { id: 'pay-backfill',  label: '📼 Historical Backfill',      action: DemoAPI.backfillPay,   variant: 'btn-ghost' },
  ];

  const buttons = mode === 'CDR' ? cdrButtons : payButtons;
  const title = mode === 'CDR' ? 'CDR Scenarios' : 'Payment Scenarios';

  return (
    <div className="glass rounded-xl p-4 flex flex-col gap-3">
      <h3 className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">{title}</h3>
      <div className="flex flex-col gap-2">
        {buttons.map((btn) => (
          <button
            key={btn.id}
            id={btn.id}
            onClick={() => run(btn.id, btn.action)}
            disabled={loading !== null}
            className={`btn ${btn.variant} justify-start w-full transition-all ${
              loading === btn.id ? 'opacity-50 scale-95' : 'hover:scale-[1.02]'
            }`}
          >
            {loading === btn.id ? (
              <span className="inline-block w-3 h-3 border-2 border-current border-t-transparent rounded-full animate-spin" />
            ) : null}
            {btn.label}
          </button>
        ))}
      </div>
    </div>
  );
}
