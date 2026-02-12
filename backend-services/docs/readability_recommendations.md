# Readability and Maintainability Recommendations

Date: 2026-02-12  
Project: `backend-services` (Spring Boot)

## Goal
Improve readability, understandability, and long-term maintainability without changing functional behavior.

## Priority Recommendations

### 1. Replace untyped `Map<String, Object>` request bodies with DTOs (High)
**Why it helps**: Removes runtime casting, makes API contract explicit, improves validation and IDE discoverability.

**Current locations**:
- `backend-services/src/main/java/org/example/modules/cart/controller/CartController.java:31`
- `backend-services/src/main/java/org/example/modules/cart/controller/CartController.java:45`
- `backend-services/src/main/java/org/example/modules/cart/controller/CartController.java:54`
- `backend-services/src/main/java/org/example/modules/users/controller/UserController.java:82`
- `backend-services/src/main/java/org/example/modules/users/controller/UserController.java:116`

**Recommendation**:
- Create dedicated request DTOs (e.g., `AddCartItemRequest`, `UpdateQuantityRequest`, `UpdateUserDiscountRequest`, `ToggleEmployeeRoleRequest`).
- Apply `jakarta.validation` annotations and use `@Valid`.

### 2. Stop exposing entities from controllers; use response DTOs (High)
**Why it helps**: Separates persistence model from API model, reduces accidental field leakage, improves response clarity.

**Current locations**:
- `backend-services/src/main/java/org/example/modules/users/controller/UserController.java:48`
- `backend-services/src/main/java/org/example/modules/users/controller/UserController.java:59`
- `backend-services/src/main/java/org/example/modules/users/controller/UserController.java:81`
- `backend-services/src/main/java/org/example/modules/users/controller/UserController.java:115`
- `backend-services/src/main/java/org/example/modules/catalog/controller/ProductController.java:23`
- `backend-services/src/main/java/org/example/modules/catalog/controller/ProductController.java:49`

**Recommendation**:
- Return stable, explicit DTOs from all controller methods.
- Keep mapping logic in dedicated mapper methods/classes.

### 3. Split large service methods into named helpers (High)
**Why it helps**: Reduces cognitive load and makes business rules easier to verify and test.

**Hotspot**:
- `backend-services/src/main/java/org/example/modules/cart/service/CartService.java:127`

**Current issue**:
`toResponse` does transformation + discount policy + pricing math + DTO assembly in one block.

**Recommendation**:
- Extract helpers, e.g.:
  - `calculateTotalDiscountPercentage(CartItem item)`
  - `calculateFinalPrice(BigDecimal basePrice, double totalDiscountPercentage)`
  - `toCartItemDto(CartItem item)`

### 4. Consolidate authorization rules to one primary layer (Medium)
**Why it helps**: Prevents duplicated logic across `SecurityConfig` and `@PreAuthorize`, making behavior easier to reason about.

**Current locations**:
- `backend-services/src/main/java/org/example/config/SecurityConfig.java:42-56`
- `backend-services/src/main/java/org/example/modules/users/controller/UserController.java:37`
- `backend-services/src/main/java/org/example/modules/users/controller/UserController.java:47`

**Recommendation**:
- Keep URL-level coarse rules in `SecurityConfig`.
- Keep fine-grained/object-level rules in method security (service layer preferred).
- Document policy ownership per module.

### 5. Normalize naming and field intent (Medium)
**Why it helps**: Reduces confusion when reading code and avoids accidental misuse.

**Examples**:
- `backend-services/src/main/java/org/example/modules/users/model/User.java:26` uses `username` but API uses `realUsername` getters/setters.
- `backend-services/src/main/java/org/example/modules/catalog/controller/ProductController.java:15` uses generic field name `service`.

**Recommendation**:
- Rename `username` to `displayName` (or consistently use `username` everywhere).
- Prefer explicit injected field names (`productService`, `cartService`, etc.).

### 6. Remove stale or process-oriented comments; keep only intent comments (Medium)
**Why it helps**: Reduces noise and outdated notes that make code harder to trust.

**Examples**:
- `backend-services/src/main/java/org/example/modules/cart/controller/CartController.java:44`
- `backend-services/src/main/java/org/example/modules/cart/controller/CartController.java:65`
- `backend-services/src/main/java/org/example/modules/catalog/model/Product.java:10`

**Recommendation**:
- Remove comments like `// FIX: Added (...)` and migration-style placeholders.
- Keep comments only for non-obvious business rules.

### 7. Introduce request/response package structure per module (Medium)
**Why it helps**: Faster navigation and clearer API boundaries.

**Recommendation**:
- For each module, use:
  - `controller`
  - `service`
  - `repository`
  - `dto/request`
  - `dto/response`
  - `mapper`

### 8. Improve method and endpoint naming consistency (Low)
**Why it helps**: Predictable naming lowers onboarding time.

**Examples**:
- `CartController.addOrUpdate(...)` currently calls `cart.addOrIncrement(...)`.
- `ProductController` is mounted at `/api/v1` then repeats `/products` on each method.

**Recommendation**:
- Align method names with actual behavior (`addItem`, `incrementItem`, etc.).
- Set controller-level mapping to `/api/v1/products` for product endpoints.

## Comment Lines: When to Add Them
Add comments only when code expresses a non-obvious business invariant.

Good candidates in current code:
- `backend-services/src/main/java/org/example/modules/cart/service/CartService.java:137`  
  Explain that multiple discount sources are additive but capped at 100% by policy.
- `backend-services/src/main/java/org/example/modules/cart/service/CartService.java:214`  
  Explain why a missing start date disables user discount (business rule decision).

Avoid comments for obvious operations or historical notes.

## Suggested Refactor Order
1. DTO-ize request bodies in `cart` and `users` controllers.
2. Replace entity responses with response DTOs.
3. Refactor `CartService.toResponse` into named helpers.
4. Clean stale comments and naming inconsistencies.
5. Align endpoint mapping style and package organization.

## Expected Outcome
After these changes, the codebase should be easier to read, safer to evolve, and faster for new contributors to understand.
