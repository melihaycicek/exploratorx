"""
ExploratorX — Phase 7: Security Layer

Architecture:
  Browser → Nginx (demo.audfix.com/exploratorx)
              ├── auth_request → /audfix/auth/verify (Audfix kendi servisi)
              ├── 200 OK       → dashboard/API erişim izni
              └── 401          → login sayfasına yönlendir

This matches the exact same pattern used in Audroad at demo.audfix.com/audroad.
The Nginx config references the Audfix auth endpoint which queries its own DB
to check guest/admin status.

No separate auth-service is needed — Audfix handles all authentication.
The Next.js dashboard only needs middleware to handle the redirect on the
client side if the cookie is missing/expired.
"""
