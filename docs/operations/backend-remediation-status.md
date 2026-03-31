# Backend Remediation Status

Date: 2026-03-31

## Implemented
1. Security
- JWT fallback secret removed; runtime now requires `APP_JWT_SECRET`.
- Login and JWT request auth reject disabled and flagged users.
- User mutation logic is centralized with manager guardrails.
- Upload handling is hardened with MIME, signature, size, and path validation.
- User responses avoid leaking sensitive persistence data.

2. API and Module Boundaries
- DTO-based contracts are used in controllers.
- Product routes are normalized under `/api/v1/products`.
- Cross-module service access is preferred over direct repository leakage.
- Namespace is aligned under `com.ecommerce.platform`.

3. Data Model and Concurrency
- Monetary and percentage values use `BigDecimal` and `DECIMAL`.
- Pagination is used for growth-prone product, review, and user endpoints.
- Optimistic locking exists on cart aggregates.
- Checkout and payment persistence tables are present in the current schema baseline.

4. Payment Architecture
- Checkout uses provider-neutral payment contracts under `payment.core`.
- Stripe and Razorpay integrations are split into provider-specific packages.
- Webhook handling is gateway-aware through `/api/v1/payments/webhook/{gateway}`.

## Operational Notes
- Versioned runtime migration tooling is intentionally disabled for now.
- Default runtime schema handling uses `spring.jpa.hibernate.ddl-auto=validate`.
- Schema changes must be reflected in `DatabaseInit.sql`, `DatabaseUpgrade.sql`, and the DB change log.

## Validation Snapshot
- Backend compile and tests have been validated locally using Java 21 compiler overrides because the installed JDK is 21 while the project target remains Java 25.
- Frontend route, auth, and API usage behavior are now documented under `docs/frontend/`.
