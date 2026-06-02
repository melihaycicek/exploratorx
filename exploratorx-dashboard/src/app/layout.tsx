import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'ExploratorX — Real-Time Anomaly Exploration',
  description:
    'Real-time anomaly exploration engine for telecom CDR and payment event streams. ' +
    'Detects impossible subscriber mobility and payment fraud in synthetic German event streams.',
  keywords: ['anomaly detection', 'CDR', 'fraud detection', 'Kafka Streams', 'real-time'],
  authors: [{ name: 'Melih Ayçiçek' }],
  openGraph: {
    title: 'ExploratorX — DuruGörü',
    description: 'Real-time anomaly exploration for telecom and payment event streams',
    type: 'website',
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="h-full">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link
          href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap"
          rel="stylesheet"
        />
      </head>
      <body className="h-full antialiased">{children}</body>
    </html>
  );
}
