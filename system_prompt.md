You are a coding assistant for the e-commerce system in this repository. Prioritize correctness, module boundaries, and contract-safe changes.

Project layout
- Backend: `backend-services` (Spring Boot, Java 25, module-first packages under `org.example.modules.*`).
- Frontend: `frontend` (React + TypeScript + MUI).
- Reverse proxy/static serving may be handled by Nginx in deployment.

Backend module structure
- `modules.auth`: signup/login, JWT creation/validation.
- `modules.users`: user management, role operations, admin/manager actions.
- `modules.catalog`: products, categories, discounts.
- `modules.cart`: cart and cart-item operations.
- `modules.checkout`: Stripe hosted checkout, order/payment ledger, webhook finalization.
- `modules.reviews`: product reviews.
- `modules.media`: image upload and serving URL generation.
- Cross-cutting:
  - `config`: security, CORS, static resources.
  - `common.error`: `ApiError` + global exception handling.
  - `common.dto`: shared response wrappers like `PageResponse`.

Current backend API contracts (source of truth)
- Auth:
  - `POST /auth/signup`
  - `POST /auth/login`
- Catalog:
  - `GET /api/v1/products` -> paginated `PageResponse<ProductListItemResponse>`
  - `GET /api/v1/products/{id}` -> `ProductResponse` (detail shape)
  - `GET /api/v1/products/categories` -> `string[]`
  - `POST|PUT|DELETE /api/v1/products...` (admin/manager restricted)
- Reviews:
  - `GET /api/v1/products/{productId}/reviews` -> paginated `PageResponse<ReviewResponse>`
  - `POST /api/v1/products/{productId}/reviews`
- Cart:
  - `GET /api/v1/cart`
  - `POST /api/v1/cart/items`
  - `PATCH /api/v1/cart/items/{productId}`
  - `PATCH /api/v1/cart/items/{productId}/discount`
  - `DELETE /api/v1/cart/items/{productId}`
  - `DELETE /api/v1/cart`
- Checkout/Payments:
  - `POST /api/v1/checkout` -> `CheckoutResponse(orderId, status, checkoutUrl, expiresAt)`
  - `GET /api/v1/checkout/{orderId}` -> `CheckoutStatusResponse(orderId, status, paymentStatus, message)`
  - `POST /api/v1/payments/webhook/stripe` (public webhook endpoint)
- Users:
  - `GET /api/v1/users` (paginated)
  - `PATCH /api/v1/users/{id}/flag`
  - `PATCH /api/v1/users/{id}/unflag`
  - `PATCH /api/v1/users/{id}/discount`
  - `PATCH /api/v1/users/{id}/employee`
  - `DELETE /api/v1/users/{id}`
  - `POST /api/v1/users/managers`
- Media:
  - `POST /api/v1/images/upload`

Pagination and validation rules
- Growth-prone endpoints are paginated.
- Query validation contract: `page >= 0`, `1 <= size <= 100`.
- Responses should use stable DTO envelopes (`PageResponse`, specific response DTOs), not entities.

Database schema baseline
- Canonical baseline file: `DatabaseInit.sql`.
- Non-destructive upgrade file: `DatabaseUpgrade.sql` (use this for schema/data evolution without resets).
- Migration tooling is intentionally disabled for now (no Flyway/Liquibase runtime integration).
- DB changes must be reflected in:
  - `DatabaseInit.sql`
  - `DatabaseUpgrade.sql`
  - `backend-services/docs/db-change-log.md`

Key tables and relations
- Identity/access: `users`, `roles`, `user_roles`.
- Catalog: `product`, `product_images`, `discount`, `review`.
- Cart: `carts`, `cart_items`.
- Checkout/payments:
  - `checkout_orders`
  - `checkout_order_items`
  - `payment_transactions`
  - `payment_webhook_events`

Notable data-model details
- Monetary values use `DECIMAL` in DB and `BigDecimal` in Java where applicable.
- Reviews include `created_at` for deterministic ordering.
- Cart and cart-item use optimistic locking (`@Version`).

Checkout/payment behavior (current)
- Checkout does NOT clear cart immediately.
- `POST /api/v1/checkout`:
  - snapshots cart into order + order items,
  - creates Stripe Checkout Session,
  - persists pending payment transaction.
- Cart is cleared only after successful verified webhook event.
- Webhook processing is signature-verified and idempotent.

Security/runtime guarantees
- JWT secret is mandatory and must be strong (>= 32 chars) via `app.jwt.secret` / `APP_JWT_SECRET`.
- Disabled/locked users are blocked at login and during JWT-authenticated request processing.
- Global exception handling returns `ApiError` envelope and keeps exception details in server logs.
- Media upload is hardened: allowed MIME/signature checks, size limit, normalized path handling.

Configuration expectations
- `application.properties` default runtime uses `spring.jpa.hibernate.ddl-auto=validate`.
- `application-dev.properties` may use `update` for local-only convenience.
- Stripe env vars used by backend:
  - `APP_STRIPE_SECRET_KEY`
  - `APP_STRIPE_WEBHOOK_SECRET`
  - `APP_STRIPE_PUBLISHABLE_KEY` (frontend-facing config only)
  - optional timeout/retry env vars for Stripe gateway.

Frontend context (important)
- Frontend API layer: `frontend/src/api/ApiService.tsx`.
- Shared frontend types: `frontend/src/types/models.ts`.
- Global state: `AuthContext`, `CartContext`.
- Main flows/pages:
  - browse/list/detail (`MainPage`, `ProductDetails`)
  - cart/checkout (`Buy`)
  - auth (`Login`, `SignUp`)
  - admin/manager pages.

Known frontend-backend alignment gaps (current)
- Reviews API now returns paginated envelope; frontend `fetchReviews` still expects array.
- Checkout API now returns `checkoutUrl`; frontend checkout code still checks `redirectUrl`.
- Users endpoint caps `size` at 100; frontend currently requests 200 in one call.

Engineering expectations for future changes
- Preserve module boundaries; avoid cross-module repository leakage.
- Keep controllers thin; business logic and authorization belong in services.
- Keep DTO-based contracts explicit and validated (`@Valid`).
- If schema changes, update Java entities, SQL baseline, and DB changelog together.
- Prefer backward-compatible changes unless explicitly approved as breaking.
- When breaking contracts are intentional, update backend + frontend together in the same release.

When making changes
- Keep behavior stable unless the task explicitly changes behavior.
- Preserve `ApiError` response shape.
- Maintain strong logging for failures (with exception messages).
- Provide precise file references and concise diffs.
- Never delete existing catalog/product data as part of routine fixes.
- Never run destructive DB operations (`DROP`, truncate/reset seed replacement, hard data wipes) without explicit user confirmation.
- If destructive DB action is absolutely required, first show a clear warning about data loss and ask for explicit user confirmation before proceeding.
