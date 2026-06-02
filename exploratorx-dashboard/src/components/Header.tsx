'use client';

import { Activity, Zap } from 'lucide-react';
import type { DashboardMode } from '@/lib/types';
import type { ConnectionStatus } from '@/lib/websocket';

interface HeaderProps {
  mode: DashboardMode;
  onModeChange: (mode: DashboardMode) => void;
  onReset: () => void;
  status: ConnectionStatus;
}

export default function Header({ mode, onModeChange, onReset, status }: HeaderProps) {
  return (
    <header
      id="dashboard-header"
      className="glass-strong flex items-center justify-between px-6 py-3 border-b border-white/10 z-50 relative"
    >
      {/* Logo */}
      <div className="flex items-center gap-3">
        <div className="flex items-center justify-center w-9 h-9 rounded-xl bg-sky-500/20 border border-sky-500/30">
          <Zap size={18} className="text-sky-400" />
        </div>
        <div>
          <h1 className="text-base font-bold text-white tracking-tight leading-none">
            ExploratorX
          </h1>
          <p className="text-[10px] text-slate-500 leading-none mt-0.5">DuruGörü · Anomaly Engine</p>
        </div>
      </div>

      {/* Mode Switcher */}
      <div className="flex items-center gap-1 glass rounded-lg p-1">
        <button
          id="btn-mode-cdr"
          onClick={() => onModeChange('CDR')}
          className={`px-4 py-1.5 rounded-md text-sm font-medium transition-all duration-200 ${
            mode === 'CDR'
              ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          CDR
        </button>
        <button
          id="btn-mode-payment"
          onClick={() => onModeChange('PAYMENT')}
          className={`px-4 py-1.5 rounded-md text-sm font-medium transition-all duration-200 ${
            mode === 'PAYMENT'
              ? 'bg-violet-500/20 text-violet-400 border border-violet-500/30'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          Payment
        </button>
      </div>

      {/* Right side */}
      <div className="flex items-center gap-4">
        {/* Connection status */}
        <div className="flex items-center gap-2">
          <span
            className={`w-2 h-2 rounded-full ${
              status === 'connected'
                ? 'bg-emerald-400 live-dot'
                : status === 'connecting'
                ? 'bg-yellow-400 live-dot'
                : 'bg-red-400'
            }`}
          />
          <span className="text-xs text-slate-400 uppercase tracking-wider font-medium">
            {status === 'connected' ? 'LIVE' : status === 'connecting' ? 'CONNECTING' : 'OFFLINE'}
          </span>
        </div>

        {/* Reset button */}
        <button
          id="btn-reset-demo"
          onClick={onReset}
          className="btn btn-danger text-xs"
        >
          <Activity size={13} />
          Reset Demo
        </button>
      </div>
    </header>
  );
}
