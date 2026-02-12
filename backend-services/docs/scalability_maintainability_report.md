# Scalability and Maintainability Report

Date: 2026-02-12  
Project: `backend-services` (Spring Boot)

## Scope
- Architecture and module boundaries
- Persistence model and schema quality
- Performance/scalability risks
- Maintainability and software engineering best practices

## Findings (Ordered by Severity)

### 1. No migration strategy; high schema drift risk (High)
**Locations**:
- `backend-services/src/main/resources/application.properties:6`
- `backend-services/pom.xml:28`

**Evidence**:
- `spring.jpa.hibernate.ddl-auto=update` in runtime config.
- No Flyway/Liquibase dependency declared.

**Impact**:
- Environment drift across dev/test/prod.
- Risky deployments and difficult rollbacks.
- Harder team collaboration on database evolution.

**Recommendation**:
- Adopt Flyway or Liquibase for versioned migrations.
- Use `ddl-auto=validate` (or `none`) outside local development.

---

### 2. Monetary values use floating-point types (High)
**Locations**:
- `backend-services/src/main/java/org/example/modules/catalog/model/Product.java:17`
- `DatabaseInit.sql:62`
- `DatabaseInit.sql:104`

**Evidence**:
- Java model uses `double price`.
- Schema uses `DOUBLE` for product price and discount percentage.

**Impact**:
- Precision errors in totals and discount calculations.
- Financial inconsistencies at scale.

**Recommendation**:
- Use `BigDecimal` in Java for money and percentage math.
- Use `DECIMAL(p,s)` in DB schema (e.g., `DECIMAL(12,2)` for price).

---

### 3. Unbounded reads and in-memory aggregation (High)
**Locations**:
- `backend-services/src/main/java/org/example/modules/catalog/service/ProductService.java:21`
- `backend-services/src/main/java/org/example/modules/catalog/service/ProductService.java:30`
- `backend-services/src/main/java/org/example/modules/users/controller/UserController.java:39`

**Evidence**:
- `findAll()` used for product listing and user listing.
- Categories derived by streaming all products in memory.

**Impact**:
- Slow endpoints and memory pressure as data grows.
- Poor tail latency under load.

**Recommendation**:
- Introduce pagination (`Pageable`) and deterministic sorting.
- Use DB-level distinct category query (projection/repository method).

---

### 4. Module-boundary violations against project policy (Medium)
**Policy reference**:
- `backend-services/docs/module-boundaries.md:22`

**Violations**:
- `backend-services/src/main/java/org/example/modules/cart/service/CartService.java:31`
- `backend-services/src/main/java/org/example/modules/reviews/service/ReviewService.java:17`

**Evidence**:
- `cart` and `reviews` modules directly depend on `catalog` repository.

**Impact**:
- Tight coupling across modules.
- Harder future extraction into services.

**Recommendation**:
- Route cross-module access through owning module service/API facade.
- Exchange IDs/DTOs rather than direct repository dependence.

---

### 5. EAGER fetching on growth-sensitive relationships (Medium)
**Locations**:
- `backend-services/src/main/java/org/example/modules/users/model/User.java:45`
- `backend-services/src/main/java/org/example/modules/catalog/model/Product.java:24`

**Impact**:
- Larger-than-expected queries.
- Increased memory consumption and serialization risk.

**Recommendation**:
- Default to `LAZY` for collections/associations.
- Use targeted fetch joins/entity graphs only where needed.

---

### 6. Transaction consistency and concurrent update risks (Medium)
**Locations**:
- `backend-services/src/main/java/org/example/modules/cart/service/CartService.java:114`
- `backend-services/src/main/java/org/example/modules/cart/service/CartService.java:52`
- `backend-services/src/main/java/org/example/modules/cart/service/CartService.java:78`

**Evidence**:
- `updateItemDiscount(...)` mutates state but is not annotated `@Transactional`.
- Read-modify-write cart operations have no optimistic locking (`@Version` absent).

**Impact**:
- Lost updates and inconsistent cart state under concurrent requests.

**Recommendation**:
- Mark all mutating service methods as transactional.
- Add optimistic locking (`@Version`) to mutable aggregate roots/entities.

---

### 7. Weak API contracts and validation boundaries (Medium)
**Locations**:
- `backend-services/src/main/java/org/example/modules/cart/controller/CartController.java:31`
- `backend-services/src/main/java/org/example/modules/reviews/controller/ReviewController.java:30`
- `backend-services/src/main/java/org/example/modules/catalog/controller/ProductController.java:49`

**Evidence**:
- Untyped `Map<String, Object>` request bodies.
- Domain entities used directly as request payloads.

**Impact**:
- Runtime casting errors and implicit contracts.
- Harder evolution/versioning of APIs.

**Recommendation**:
- Introduce dedicated request/response DTOs with `@Valid` constraints.
- Keep entities internal to persistence layer.

---

### 8. Schema constraints/indexing are insufficient for scale (Medium)
**Locations**:
- `DatabaseInit.sql:61`
- `DatabaseInit.sql:77`
- `DatabaseInit.sql:57`

**Evidence**:
- `product.category` is queried but not explicitly indexed.
- `review.rating` has no range constraint.
- Core product columns are nullable (`name`, `category`, `price`).

**Impact**:
- Slower filtered queries.
- Weak data quality guarantees.

**Recommendation**:
- Add index on `product(category)`.
- Add check constraint for rating range (e.g., 1..5).
- Enforce `NOT NULL` where business-required.

---

### 9. Inconsistent response modeling with ad-hoc maps (Low)
**Location**:
- `backend-services/src/main/java/org/example/modules/checkout/controller/CheckoutController.java:28`

**Evidence**:
- Returns `Map<String, Object>` instead of structured response DTO.

**Impact**:
- Weaker API contracts and harder client maintenance.

**Recommendation**:
- Use typed response DTOs for stable API schemas.

## Additional Observations
- `spring.jpa.show-sql=true` in `backend-services/src/main/resources/application.properties:7` should be profile-specific to avoid noisy logs and potential sensitive data exposure in production logs.
- Schema/data initialization script (`DatabaseInit.sql`) includes mixed credential quality and should be separated into deterministic migrations + environment-specific seed policies.

## Suggested Remediation Roadmap
1. Introduce migration tooling (Flyway/Liquibase) and production-safe JPA settings.
2. Migrate monetary fields from `double/DOUBLE` to `BigDecimal/DECIMAL`.
3. Add pagination and DB-level category queries.
4. Enforce module boundary contracts via service facades.
5. Convert request/response surfaces to DTOs with validation.
6. Add indexing and check/not-null constraints for critical tables.
7. Apply transaction/locking hardening for cart flows.

## Expected Outcome
Applying these changes will significantly improve long-term scalability, reduce operational risk, and make the codebase easier to evolve in a multi-developer production environment.
