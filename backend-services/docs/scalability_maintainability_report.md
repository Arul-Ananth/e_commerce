# Scalability and Maintainability Report

Date: 2026-03-15  
Project: `backend-services` (Spring Boot)

## Scope
- Architecture and module boundaries
- Persistence model and schema quality
- Performance/scalability risks
- Maintainability and software engineering best practices

## Open Findings

### 1. No Versioned Migration Strategy Active (High)
**Locations**:
- `backend-services/src/main/resources/application.properties`
- `backend-services/pom.xml`

**Evidence**:
- No Flyway/Liquibase dependency currently active.
- Schema lifecycle depends on runtime JPA mode.

**Impact**:
- Schema drift risk across environments.
- Harder rollback and reproducibility for DB changes.

**Recommendation**:
- Re-introduce versioned DB migrations when ready.
- Use non-mutating JPA mode outside local development.

## Resolved Since Previous Report
- Monetary model migrated to `BigDecimal/DECIMAL`.
- Product and user listing now support pagination and deterministic sorting.
- Category list now uses DB-level distinct query.
- Cross-module repository dependency from `cart`/`reviews` to `catalog` repository removed.
- Growth-sensitive association fetch strategy shifted away from eager defaults.
- Cart mutating flows are transactional and include optimistic locking (`@Version`).
- Weak API contracts replaced with typed request/response DTOs and validation.
- Checkout no longer uses ad-hoc response map.
- Schema hardening applied in `DatabaseInit.sql` (`NOT NULL`, index on `product.category`, rating check constraint).

## Expected Outcome
Current runtime behavior is improved for correctness and API stability; primary remaining platform risk is lack of versioned migration control.
