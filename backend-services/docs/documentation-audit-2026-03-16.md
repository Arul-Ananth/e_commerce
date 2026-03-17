# Documentation Audit

Date: 2026-03-16  
Scope: existing docs under `backend-services/docs` compared against current code.

## Summary

- Several docs still describe pre-refactor behavior.
- Most drift is around schema mode, payment architecture, and webhook endpoint path.
- Frontend documentation is mostly missing.

## Outdated or Misleading Docs

### 1. `backend-services/docs/security_vulnerability.md`
- Status: Outdated.
- Issue:
  - Open finding says default `ddl-auto=update`.
  - Current code uses `spring.jpa.hibernate.ddl-auto=validate` in `application.properties`.
- Impact:
  - Readers may think schema mutability risk is still active in default profile.

### 2. `backend-services/docs/backend-remediation-plan.md`
- Status: Partially outdated.
- Issue:
  - Operational note says DB lifecycle uses runtime schema update mode.
  - Current default is `validate`; only dev profile uses `update`.
- Impact:
  - Incorrect operations guidance for default runtime behavior.

### 3. `backend-services/docs/scalability_maintainability_report.md`
- Status: Partially outdated.
- Issue:
  - Mentions Stripe-specific webhook endpoint (`/api/v1/payments/webhook/stripe`).
  - Current endpoint is gateway-agnostic: `/api/v1/payments/webhook/{gateway}`.
  - New multi-gateway architecture (resolver + Stripe/Razorpay services) is not reflected.
- Impact:
  - Payment integration documentation no longer matches runtime API and design.

### 4. `backend-services/docs/runtime-scalability-db-evolution-stripe-plan.md`
- Status: Partially outdated.
- Issue:
  - Document title/content is Stripe-centric.
  - Current implementation includes provider abstraction + Razorpay implementation.
  - Webhook contract changed to gateway path variable.
- Impact:
  - Understates current architecture and can mislead new contributors.

## Docs That Are Still Mostly Valid

### 1. `backend-services/docs/module-boundaries.md`
- Mostly consistent with current package-by-feature architecture and service-boundary principles.

### 2. `backend-services/docs/readability_recommendations.md`
- Mostly valid; remaining recommendations still apply.

### 3. `backend-services/docs/db-change-log.md`
- Mostly valid for checkout/index additions.
- Needs extension for latest payment-gateway abstraction changes and related config behavior.

## Missing Documentation

### 1. No single source of truth for full API contracts
- Missing before this update:
  - complete endpoint list
  - auth requirements per endpoint
  - pagination envelope format
  - error envelope details

### 2. Frontend functional documentation is missing
- Missing:
  - route map
  - role-based UI behavior
  - page-to-endpoint mapping
  - checkout redirect behavior

### 3. Payment gateway operational runbook is incomplete
- Missing:
  - gateway switch behavior (`APP_PAYMENT_GATEWAY`)
  - webhook endpoint patterns per provider
  - required secrets per provider

### 4. Known backend/frontend contract mismatches are undocumented
- Example:
  - frontend `getAllUsers()` requests `size=200`
  - backend validates max `size=100`

### 5. No consolidated environment variable reference
- Missing central list for JWT, DB, media, CORS, Stripe, Razorpay, and payment gateway switch settings.

## Added in This Update

- `backend-services/docs/full-system-feature-api-reference.md`
  - full backend + frontend feature documentation
  - full API inventory and purpose
  - auth/access matrix
  - frontend route map
  - payment configuration summary

