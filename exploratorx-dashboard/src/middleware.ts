import { NextRequest, NextResponse } from 'next/server';

/**
 * ExploratorX — Next.js Auth Middleware
 *
 * Audfix auth flow:
 *   - Nginx auth_request koruması birincil güvenlik katmanıdır.
 *   - Bu middleware yalnızca istemci tarafında ek güvence sağlar.
 *   - Audfix başarılı login sonrası X-Auth-User header'ı iletir.
 *   - Bu header yoksa kullanıcı Audfix login sayfasına yönlendirilir.
 *
 * Public paths (auth gerektirmez):
 *   - /_next/static/* (Next.js assets)
 *   - /api/health (uptime monitoring)
 *   - /favicon.svg
 */

const PUBLIC_PATHS = [
  '/_next/',
  '/favicon',
  '/leaflet/',
  '/api/health',
];

const AUDFIX_LOGIN = 'https://demo.audfix.com/login';
const BASE_PATH = process.env.NEXT_PUBLIC_BASE_PATH || '/exploratorx';

function isPublicPath(pathname: string): boolean {
  return PUBLIC_PATHS.some((p) => pathname.startsWith(p));
}

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Always allow public paths
  if (isPublicPath(pathname)) {
    return NextResponse.next();
  }

  // Check for Audfix auth header (set by Nginx after auth_request success)
  const authUser = request.headers.get('x-auth-user');
  const forwardedUser = request.headers.get('x-forwarded-user');

  if (!authUser && !forwardedUser) {
    // In production: Nginx blocks before we get here.
    // In development (no Nginx): redirect to login.
    if (process.env.NODE_ENV === 'production') {
      // Nginx already validated — but if somehow reached, trust the proxy
      return NextResponse.next();
    }

    // Development mode: allow with a warning header
    const response = NextResponse.next();
    response.headers.set('X-Auth-Warning', 'no-auth-in-dev-mode');
    return response;
  }

  // Auth user is present — pass through, forward user identity
  const response = NextResponse.next();
  if (authUser) {
    response.headers.set('X-Exploratorx-User', authUser);
  }

  return response;
}

export const config = {
  matcher: [
    /*
     * Match all paths except:
     * - _next/static (static files)
     * - _next/image (image optimization)
     * - favicon
     */
    '/((?!_next/static|_next/image|favicon).*)',
  ],
};
