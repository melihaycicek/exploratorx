'use client';

import { useEffect, useRef, useCallback, useState } from 'react';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type { DashboardEnvelope, PayloadType } from '@/lib/types';

const ENGINE_URL = process.env.NEXT_PUBLIC_ENGINE_URL || 'http://localhost:8080';

type Handler = (envelope: DashboardEnvelope) => void;

let globalClient: Client | null = null;
const subscribers: Map<string, Set<Handler>> = new Map();

function getOrCreateClient(): Client {
  if (globalClient) return globalClient;

  const client = new Client({
    webSocketFactory: () => new SockJS(`${ENGINE_URL}/ws`),
    reconnectDelay: 3000,
    onConnect: () => {
      console.log('[WS] Connected to ExploratorX engine');
      // Subscribe to all topics
      ['/topic/live-signals', '/topic/live-anomalies', '/topic/stats'].forEach((topic) => {
        client.subscribe(topic, (msg: IMessage) => {
          try {
            const envelope: DashboardEnvelope = JSON.parse(msg.body);
            const handlers = subscribers.get(topic);
            if (handlers) handlers.forEach((h) => h(envelope));
          } catch (e) {
            console.error('[WS] Parse error', e);
          }
        });
      });
    },
    onDisconnect: () => {
      console.log('[WS] Disconnected');
    },
    onStompError: (frame) => {
      console.error('[WS] STOMP error', frame);
    },
  });

  client.activate();
  globalClient = client;
  return client;
}

export type ConnectionStatus = 'connecting' | 'connected' | 'disconnected';

export function useWebSocket(
  topic: '/topic/live-signals' | '/topic/live-anomalies' | '/topic/stats',
  onMessage: Handler
) {
  const [status, setStatus] = useState<ConnectionStatus>('connecting');
  const handlerRef = useRef(onMessage);
  handlerRef.current = onMessage;

  useEffect(() => {
    const stableHandler: Handler = (env) => handlerRef.current(env);
    if (!subscribers.has(topic)) {
      subscribers.set(topic, new Set());
    }
    subscribers.get(topic)!.add(stableHandler);

    const client = getOrCreateClient();

    const checkStatus = setInterval(() => {
      if (client.connected) setStatus('connected');
      else if (client.active) setStatus('connecting');
      else setStatus('disconnected');
    }, 1000);

    return () => {
      subscribers.get(topic)?.delete(stableHandler);
      clearInterval(checkStatus);
    };
  }, [topic]);

  return status;
}

// ─── Demo API calls ───────────────────────────────────────────────────────────

async function callDemo(path: string): Promise<void> {
  await fetch(`${ENGINE_URL}${path}`, { method: 'POST' });
}

export const DemoAPI = {
  // CDR
  cdrNormal:     () => callDemo('/api/demo/cdr/normal'),
  cdrSuspicious: () => callDemo('/api/demo/cdr/suspicious'),
  cdrImpossible: () => callDemo('/api/demo/cdr/impossible'),
  cdrSplit:      () => callDemo('/api/demo/cdr/split'),
  // Payment
  payNormal:    () => callDemo('/api/demo/pay/normal'),
  payImpossible:() => callDemo('/api/demo/pay/impossible'),
  payDuplicate: () => callDemo('/api/demo/pay/duplicate'),
  payVelocity:  () => callDemo('/api/demo/pay/velocity'),
  payChallenge: () => callDemo('/api/demo/pay/challenge'),
  // Backfill
  backfillCdr:  () => callDemo('/api/demo/backfill/cdr'),
  backfillPay:  () => callDemo('/api/demo/backfill/pay'),
  // Reset
  reset:        () => callDemo('/api/demo/reset'),
  // Stats
  stats:        () => fetch(`${ENGINE_URL}/api/stats`).then(r => r.json()),
};
