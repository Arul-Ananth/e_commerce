# Commerce Platform

Full-stack e-commerce application with a React frontend, a Spring Boot backend, MySQL persistence, Redis cache-aside caching, and Nginx-based deployment support.

## Current Capabilities

- JWT-based signup and login
- Role-aware administration for users, managers, products, and discounts
- Product catalog with categories, images, discounts, and reviews
- Cart and checkout flows with payment gateway abstraction
- Stripe and Razorpay payment integrations
- Redis-backed cache-aside reads for catalog, reviews, images, categories, and cart responses
- Secure image upload and public image serving
- Nginx reverse proxy support for `/api` and `/auth`

## Stack

- Frontend: React, TypeScript, Vite, MUI
- Backend: Spring Boot 3.5, Java 25
- Database: MySQL 8
- Cache: Redis
- Reverse proxy: Nginx

## Repository Layout

```text
.
|-- backend-services/   Spring Boot backend
|-- frontend/           React frontend
|-- nginx/              Nginx config and deploy script
|-- docs/               Architecture, frontend, operations, reference, and reports
|-- DatabaseInit.sql    Full schema reset + seed data
|-- DatabaseUpgrade.sql Non-destructive schema evolution script
```

## Local Development

### Prerequisites

- Java 25 for the backend target
- Maven
- Node.js 18+
- MySQL 8
- Redis 7-compatible server

### 1. Initialize the database

Create the `ecommerce_db` database in MySQL, then run:

```sql
SOURCE DatabaseInit.sql;
```

`DatabaseInit.sql` recreates the schema and seeds sample data.

Use `DatabaseUpgrade.sql` only when you need to evolve an existing database without resetting it.

### 2. Configure backend secrets

Copy `backend-services/secrets.properties.example` to `backend-services/secrets.properties` and fill in the values you need.

The backend loads that file automatically through `spring.config.import=optional:file:./secrets.properties`.

Minimum local values:

```properties
APP_JWT_SECRET=replace-with-a-random-secret-at-least-32-chars
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/ecommerce_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your-mysql-password
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
```

Optional but commonly needed:

```properties
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,https://localhost
APP_PAYMENT_GATEWAY=stripe
APP_STRIPE_SECRET_KEY=sk_test_replace_me
APP_STRIPE_PUBLISHABLE_KEY=pk_test_replace_me
APP_STRIPE_WEBHOOK_SECRET=whsec_replace_me
```

### 3. Start the backend

```bash
cd backend-services
mvn spring-boot:run
```

Backend default URL:

```text
http://localhost:8080
```

The backend uses Redis only as a performance cache. MySQL remains the source of truth. If cached list data was created by an older local build, clear Redis once with `redis-cli flushall` or delete the relevant cache keys.

### 4. Start the frontend

```bash
cd frontend
npm install
npm start
```

Frontend default URL:

```text
http://localhost:5173
```

## Deployment Notes

The Nginx deployment assets live under `nginx/`.

- Site template: `nginx/conf.d/commerce-platform.conf`
- Main Nginx config: `nginx/nginx.conf`
- Deploy script: `nginx/deploy_wsl.sh`
- Backend upstream config: `nginx/backend-upstream.conf`

The Nginx site template proxies both `/api` and `/auth` using the `__BACKEND_UPSTREAM__` placeholder, which the deploy script renders from `nginx/backend-upstream.conf`.

The deploy script also:

- builds the frontend
- deploys static assets under `/var/www/commerce-platform/frontend/dist`
- renders the Nginx site file
- validates the upstream placeholder usage

## Seeded Accounts

`DatabaseInit.sql` seeds these accounts:

- `admin@ecommerce.com`
- `user@ecommerce.com`
- `manager@ecommerce.com`

Passwords are bcrypt-hashed in the SQL seed file. If you need the plain-text test password, check the project's seed/auth documentation before sharing it outside local development.

## Documentation Map

- `docs/README.md`
- `docs/reference/api-reference.md`
- `docs/architecture/module-boundaries.md`
- `docs/architecture/runtime-cache-portability.md`
- `docs/frontend/route-map.md`
- `docs/frontend/state-and-auth.md`
- `docs/frontend/api-integration.md`
- `docs/frontend/page-flow.md`
- `docs/operations/environment-runbook.md`
- `docs/operations/nginx-deployment-runbook.md`
- `docs/operations/db-change-log.md`
- `docs/reports/scalability-maintainability-report.md`
- `docs/reports/documentation-audit.md`

## Known Setup Pitfalls

- The backend default runtime uses `spring.jpa.hibernate.ddl-auto=validate`, so schema drift will fail startup.
- Java 21 can compile the code for local validation with overrides, but the project target remains Java 25.
- For WSL/Linux runs, use Linux paths such as `/mnt/c/Dev/e_commerce`; do not use PowerShell syntax like `$env:...`.
- The default media path is relative to `backend-services`, so run the backend from that directory or set `APP_MEDIA_RESOURCE_LOCATION` explicitly.
- If the backend fails on startup, first check:
  - JWT secret presence
  - MySQL credentials
  - Redis reachability for cache-backed reads
  - schema state versus `DatabaseInit.sql` or `DatabaseUpgrade.sql`

## API Surface

Primary public/authenticated route groups:

- `/auth`
- `/api/v1/products`
- `/api/v1/cart`
- `/api/v1/checkout`
- `/api/v1/payments/webhook/{gateway}`
- `/api/v1/users`
- `/api/v1/images/upload`

For the full endpoint inventory, use `docs/reference/api-reference.md`.
