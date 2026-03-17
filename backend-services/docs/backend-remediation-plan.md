# Backend Services Remediation Status (Current)

Date: 2026-03-15

## Implemented
1. Security
- JWT fallback secret removed; runtime now requires `APP_JWT_SECRET`.
- Login and JWT request auth now reject disabled/flagged users.
- User mutation logic centralized in `UserService` with manager guardrails.
- Upload endpoint hardened with MIME/signature/size/path validation.
- Password serialization prevented (`@JsonIgnore`), and user endpoints return DTOs.

2. API and Module Boundaries
- Request maps replaced with typed DTOs + `@Valid`.
- Entity/map responses replaced with typed response DTOs.
- Product routes normalized under `/api/v1/products`.
- `cart` and `reviews` now use `ProductService` instead of direct `ProductRepository` access.

3. Data Model and Concurrency
- Money/percentage types migrated to `BigDecimal` in Java and `DECIMAL` in SQL init script.
- Pagination added for product/user list APIs.
- Distinct categories now queried at DB level.
- Optimistic locking (`@Version`) added for cart aggregates.

4. Frontend Alignment
- Frontend API calls aligned to paginated product/user contracts and category endpoint.
- Product loading now shows explicit loading/error/empty states.

## Operational Notes
- Versioned migration tooling is intentionally disabled for now per current project direction.
- Current DB lifecycle uses JPA runtime schema update mode.

## Validation
- Backend compile: passed (`mvn -q -DskipTests compile`)
- Frontend build: passed (`npm run build`)
- Runtime API check: `/api/v1/products` returns paged product data successfully.
