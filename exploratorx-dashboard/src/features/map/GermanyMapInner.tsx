'use client';

import { useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Polyline, CircleMarker, Tooltip, useMap } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import type { MapRoute } from '@/lib/types';
import { getDecisionColor } from '@/lib/utils';

// Fix Leaflet icon issue with Next.js
import L from 'leaflet';
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: '/leaflet/marker-icon-2x.png',
  iconUrl: '/leaflet/marker-icon.png',
  shadowUrl: '/leaflet/marker-shadow.png',
});

interface GermanyMapInnerProps {
  routes: MapRoute[];
}

// Germany center and bounds
const GERMANY_CENTER: [number, number] = [51.1657, 10.4515];
const GERMANY_BOUNDS: [[number, number], [number, number]] = [
  [47.2, 5.5],
  [55.1, 15.5],
];

function isDashed(decision: string): boolean {
  return decision === 'OUT_OF_ORDER_EVENT';
}

export default function GermanyMapInner({ routes }: GermanyMapInnerProps) {
  return (
    <div className="rounded-xl overflow-hidden border border-white/10" style={{ height: '100%', minHeight: 400 }}>
      <MapContainer
        center={GERMANY_CENTER}
        zoom={6}
        minZoom={5}
        maxZoom={10}
        maxBounds={GERMANY_BOUNDS}
        maxBoundsViscosity={0.8}
        style={{ height: '100%', width: '100%', background: '#0a0e1a' }}
        zoomControl={true}
        attributionControl={true}
      >
        <TileLayer
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        />

        {routes.map((route) => {
          const color = getDecisionColor(route.decision);
          const dashed = isDashed(route.decision);
          const isCritical = ['IMPOSSIBLE_SIGNAL', 'SPLIT_SIGNAL', 'BLOCKED'].includes(route.decision);

          return (
            <div key={route.id}>
              {/* Route line */}
              <Polyline
                positions={[route.from, route.to]}
                pathOptions={{
                  color,
                  weight: isCritical ? 3 : 2,
                  opacity: 0.85,
                  dashArray: dashed ? '8, 6' : undefined,
                }}
              >
                <Tooltip sticky>
                  <div style={{ fontFamily: 'Inter, sans-serif', fontSize: 12 }}>
                    <div style={{ fontWeight: 700, color }}>
                      {route.decision.replace(/_/g, ' ')}
                    </div>
                    <div style={{ color: '#94a3b8', marginTop: 2 }}>
                      {route.fromCity} → {route.toCity}
                    </div>
                    <div style={{ color: '#64748b', fontSize: 11 }}>
                      Risk: {route.riskScore} · {route.mode}
                    </div>
                  </div>
                </Tooltip>
              </Polyline>

              {/* FROM marker */}
              <CircleMarker
                center={route.from}
                radius={isCritical ? 6 : 4}
                pathOptions={{ color, fillColor: color, fillOpacity: 0.7, weight: 1.5 }}
              >
                <Tooltip>{route.fromCity}</Tooltip>
              </CircleMarker>

              {/* TO marker */}
              <CircleMarker
                center={route.to}
                radius={isCritical ? 8 : 5}
                pathOptions={{ color, fillColor: color, fillOpacity: 0.9, weight: 2 }}
              >
                <Tooltip>{route.toCity}</Tooltip>
              </CircleMarker>
            </div>
          );
        })}
      </MapContainer>
    </div>
  );
}
