'use client';

import type { StatsPayload } from '@/lib/types';
import { Activity, AlertTriangle, Zap, CreditCard } from 'lucide-react';

interface StatsCardsProps {
  stats: StatsPayload | null;
  mode: 'CDR' | 'PAYMENT';
}

interface StatCardProps {
  label: string;
  value: number | string;
  icon: React.ReactNode;
  color: string;
  bg: string;
}

function StatCard({ label, value, icon, color, bg }: StatCardProps) {
  return (
    <div className="glass rounded-xl p-4 flex flex-col gap-2">
      <div className="flex items-center justify-between">
        <span className="text-[11px] text-slate-400 uppercase tracking-wider font-medium">{label}</span>
        <div className={`${bg} ${color} p-1.5 rounded-lg`}>{icon}</div>
      </div>
      <span className="text-2xl font-bold text-white tabular-nums">{value ?? 0}</span>
    </div>
  );
}

export default function StatsCards({ stats, mode }: StatsCardsProps) {
  if (!stats) {
    return (
      <div className="grid grid-cols-2 gap-3">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="glass rounded-xl p-4 animate-pulse h-20" />
        ))}
      </div>
    );
  }

  if (mode === 'CDR') {
    return (
      <div className="grid grid-cols-2 gap-3">
        <StatCard
          label="CDR Events"
          value={stats.cdr.totalEvents}
          icon={<Activity size={14} />}
          color="text-sky-400"
          bg="bg-sky-500/15"
        />
        <StatCard
          label="Impossible"
          value={stats.cdr.impossibleSignals}
          icon={<Zap size={14} />}
          color="text-red-400"
          bg="bg-red-500/15"
        />
        <StatCard
          label="Split Signal"
          value={stats.cdr.splitSignals}
          icon={<AlertTriangle size={14} />}
          color="text-violet-400"
          bg="bg-violet-500/15"
        />
        <StatCard
          label="Suspicious"
          value={stats.cdr.suspiciousMovements}
          icon={<Activity size={14} />}
          color="text-yellow-400"
          bg="bg-yellow-500/15"
        />
      </div>
    );
  }

  return (
    <div className="grid grid-cols-2 gap-3">
      <StatCard
        label="Transactions"
        value={stats.payment.totalEvents}
        icon={<CreditCard size={14} />}
        color="text-sky-400"
        bg="bg-sky-500/15"
      />
      <StatCard
        label="Blocked"
        value={stats.payment.blockedTransactions}
        icon={<AlertTriangle size={14} />}
        color="text-red-400"
        bg="bg-red-500/15"
      />
      <StatCard
        label="Challenge"
        value={stats.payment.challengeRequired}
        icon={<Zap size={14} />}
        color="text-orange-400"
        bg="bg-orange-500/15"
      />
      <StatCard
        label="Duplicates"
        value={stats.payment.duplicateIgnored}
        icon={<Activity size={14} />}
        color="text-slate-400"
        bg="bg-slate-500/15"
      />
    </div>
  );
}
