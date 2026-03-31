# Environment Runbook

## Backend Secrets File

Local backend secrets are loaded from `backend-services/secrets.properties` through `spring.config.import=optional:file:./secrets.properties`.

Start from `backend-services/secrets.properties.example` and fill the values you actually need.

## Minimum Local Settings

```properties
APP_JWT_SECRET=replace-with-a-random-secret-at-least-32-chars
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/ecommerce_db
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your-mysql-password
```

## Common Optional Settings

```properties
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,https://localhost
APP_PAYMENT_GATEWAY=stripe
APP_PAYMENT_DEFAULT_CURRENCY=usd
APP_MEDIA_UPLOAD_DIR=C:/Dev/e_commerce/imageResource/
APP_MEDIA_RESOURCE_LOCATION=file:///C:/Dev/e_commerce/imageResource/
APP_MEDIA_PUBLIC_BASE_URL=http://localhost:8080/images/
```

## Stripe Settings

Use these when `APP_PAYMENT_GATEWAY=stripe`:

```properties
APP_STRIPE_SECRET_KEY=sk_test_replace_me
APP_STRIPE_PUBLISHABLE_KEY=pk_test_replace_me
APP_STRIPE_WEBHOOK_SECRET=whsec_replace_me
APP_STRIPE_SUCCESS_URL=http://localhost:5173/checkout/success
APP_STRIPE_CANCEL_URL=http://localhost:5173/checkout/cancel
```

## Razorpay Settings

Use these when `APP_PAYMENT_GATEWAY=razorpay`:

```properties
APP_RAZORPAY_KEY_ID=rzp_test_replace_me
APP_RAZORPAY_KEY_SECRET=replace_me
APP_RAZORPAY_WEBHOOK_SECRET=replace_me
APP_RAZORPAY_CHECKOUT_BASE_URL=http://localhost:5173/checkout/razorpay
```

## Database Notes

- The default runtime uses `spring.jpa.hibernate.ddl-auto=validate`.
- Schema drift causes startup failure.
- `DatabaseInit.sql` is a full reset and reseed.
- `DatabaseUpgrade.sql` is the non-destructive path for evolving an existing schema.

## Common Startup Failures

### Missing JWT secret
- Symptom: `Could not resolve placeholder 'APP_JWT_SECRET'`
- Fix: set `APP_JWT_SECRET` in `backend-services/secrets.properties`

### MySQL authentication failure
- Symptom: access denied for `root` or another configured user
- Fix: set `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` correctly

### Schema validation failure
- Symptom: Hibernate reports missing columns or tables during startup
- Fix: run `DatabaseInit.sql` for a reset or apply the needed changes from `DatabaseUpgrade.sql`

### Java target mismatch
- Symptom: Maven fails with `release version 25 not supported`
- Fix: install/configure Java 25 for native project builds
