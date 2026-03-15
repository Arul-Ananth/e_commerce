# Readability and Maintainability Recommendations

Date: 2026-03-15  
Project: `backend-services` (Spring Boot)

## Open Recommendations

### 1. Normalize User Naming Semantics (Medium)
**Current state**:
- Entity field is `username` while accessor naming still uses `getRealUsername` / `setRealUsername` in several call sites.

**Recommendation**:
- Standardize on one naming model (`username` or `displayName`) across entity, DTOs, and service/controller usage.

### 2. Consolidate Authorization Ownership Documentation (Low)
**Current state**:
- Authorization is now split appropriately between route-level config and service-level checks, but ownership conventions are not documented in one place.

**Recommendation**:
- Add a short `docs` note defining where coarse-grained vs object-level authorization logic belongs.

## Resolved Since Previous Report
- Untyped `Map<String, Object>` request bodies replaced with dedicated DTOs and `@Valid`.
- Controller responses moved from entities/ad-hoc maps to explicit DTOs.
- `CartService` transformation/pricing logic split into named helper methods.
- Request/response package structure introduced across modules.
- Product controller mapping normalized under `/api/v1/products`.
- Stale implementation comments in backend hot paths were removed.

## Expected Outcome
Code readability is substantially improved; remaining work is mostly naming consistency and documentation polish.
