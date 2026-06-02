'use client';

import dynamic from 'next/dynamic';
import type { MapRoute } from '@/lib/types';

// Leaflet cannot be server-rendered
const GermanyMapInner = dynamic(() => import('./GermanyMapInner'), {
  ssr: false,
  loading: () => (
    <div className="glass rounded-xl flex items-center justify-center" style={{ height: '100%' }}>
      <div className="text-slate-500 text-sm">Loading map…</div>
    </div>
  ),
});

interface GermanyMapProps {
  routes: MapRoute[];
}

export default function GermanyMap({ routes }: GermanyMapProps) {
  return <GermanyMapInner routes={routes} />;
}
