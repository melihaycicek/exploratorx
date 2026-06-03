'use client';

import { useEffect, useRef, useState } from 'react';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, ReferenceLine
} from 'recharts';
import { getDecisionColor } from '@/lib/utils';
import { formatTime } from '@/lib/utils';

interface RiskPoint {
  time: string;
  score: number;
  decision: string;
}

interface RiskChartProps {
  mode: 'CDR' | 'PAYMENT';
  /** Incoming risk score updates — append externally */
  points: RiskPoint[];
}

const MAX_POINTS = 30;

function CustomDot(props: any) {
  const { cx, cy, payload } = props;
  const color = getDecisionColor(payload.decision);
  const critical = ['IMPOSSIBLE_SIGNAL', 'SPLIT_SIGNAL', 'BLOCKED'].includes(payload.decision);
  return (
    <circle
      cx={cx}
      cy={cy}
      r={critical ? 5 : 3}
      fill={color}
      stroke={critical ? 'rgba(255,255,255,0.4)' : 'none'}
      strokeWidth={critical ? 2 : 0}
    />
  );
}

function CustomTooltip({ active, payload }: any) {
  if (!active || !payload?.length) return null;
  const p = payload[0].payload as RiskPoint;
  const color = getDecisionColor(p.decision);
  return (
    <div className="glass-strong rounded-lg p-2 text-[11px] shadow-xl">
      <div style={{ color }} className="font-bold">{p.decision.replace(/_/g, ' ')}</div>
      <div className="text-slate-400 mt-0.5">Score: <span className="text-white font-mono">{p.score}</span></div>
      <div className="text-slate-600 font-mono">{p.time}</div>
    </div>
  );
}

export default function RiskChart({ mode, points }: RiskChartProps) {
  const displayPoints = points.slice(-MAX_POINTS);

  const modeColor = mode === 'CDR' ? '#22c55e' : '#a855f7';
  const modeLabel = mode === 'CDR' ? 'CDR Risk Scores' : 'Payment Risk Scores';

  return (
    <div id="risk-chart-panel" className="glass rounded-xl p-4 flex flex-col gap-2">
      <div className="flex items-center justify-between">
        <h3 className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">
          {modeLabel}
        </h3>
        <span className="text-[10px] font-mono text-slate-600">
          last {MAX_POINTS} events
        </span>
      </div>

      {displayPoints.length === 0 ? (
        <div className="flex items-center justify-center h-24 text-slate-600 text-xs">
          No data yet — trigger a scenario
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={120}>
          <LineChart data={displayPoints} margin={{ top: 4, right: 8, bottom: 0, left: -20 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
            <XAxis
              dataKey="time"
              tick={{ fill: '#475569', fontSize: 9, fontFamily: 'JetBrains Mono' }}
              tickLine={false}
              axisLine={false}
            />
            <YAxis
              domain={[0, 100]}
              tick={{ fill: '#475569', fontSize: 9 }}
              tickLine={false}
              axisLine={false}
            />
            <Tooltip content={<CustomTooltip />} />
            {/* Threshold lines */}
            <ReferenceLine y={30} stroke="rgba(34,197,94,0.3)" strokeDasharray="4 4" />
            <ReferenceLine y={60} stroke="rgba(234,179,8,0.3)" strokeDasharray="4 4" />
            <ReferenceLine y={80} stroke="rgba(249,115,22,0.3)" strokeDasharray="4 4" />
            <Line
              type="monotone"
              dataKey="score"
              stroke={modeColor}
              strokeWidth={1.5}
              dot={<CustomDot />}
              activeDot={false}
              isAnimationActive={false}
            />
          </LineChart>
        </ResponsiveContainer>
      )}

      {/* Legend */}
      <div className="flex gap-3 text-[9px] text-slate-600">
        <span className="flex items-center gap-1"><span className="w-4 h-px inline-block" style={{ background: 'rgba(34,197,94,0.3)' }} /> 30 NORMAL</span>
        <span className="flex items-center gap-1"><span className="w-4 h-px inline-block" style={{ background: 'rgba(234,179,8,0.3)' }} /> 60 SUSPICIOUS</span>
        <span className="flex items-center gap-1"><span className="w-4 h-px inline-block" style={{ background: 'rgba(249,115,22,0.3)' }} /> 80 CHALLENGE</span>
      </div>
    </div>
  );
}
