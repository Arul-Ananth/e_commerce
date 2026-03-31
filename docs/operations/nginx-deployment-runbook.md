# Nginx Deployment Runbook

## Files

- Site template: `nginx/conf.d/commerce-platform.conf`
- Main config: `nginx/nginx.conf`
- Deploy script: `nginx/deploy_wsl.sh`
- Backend upstream config: `nginx/backend-upstream.conf`

## Template Rendering

The site template contains `__BACKEND_UPSTREAM__` placeholders in both:

- `/api/`
- `/auth/`

The deploy script reads `BACKEND_UPSTREAM` from `nginx/backend-upstream.conf`, validates the value, and renders the final site file from the template.

Expected format:

```properties
BACKEND_UPSTREAM=host:port
```

## Important Paths

- Rendered site path: `/etc/nginx/conf.d/commerce-platform.conf`
- Frontend deploy path: `/var/www/commerce-platform/frontend/dist`
- TLS certificate path: `/etc/nginx/certs/commerce-platform.crt`
- TLS key path: `/etc/nginx/certs/commerce-platform.key`

## Deploy Script Behavior

`nginx/deploy_wsl.sh` currently:

- ensures Nginx, rsync, curl, and Node are available
- builds the frontend in WSL
- copies frontend assets to the deploy directory
- renders the Nginx template using `BACKEND_UPSTREAM`
- removes the default Nginx site if present
- optionally validates backend reachability through `/api/v1/products/categories`
- reloads Nginx when TLS cert files exist

## Troubleshooting

### Placeholder validation fails
- Cause: the template is missing `__BACKEND_UPSTREAM__` in `/api` or `/auth`
- Fix: verify both proxy blocks use the placeholder before deployment

### Backend probe fails
- Cause: backend host/port is wrong or backend is not reachable from WSL/Nginx
- Fix: update `nginx/backend-upstream.conf` with the correct `host:port`

### TLS reload is skipped
- Cause: certificate or key file is missing
- Fix: place the certificate files at the configured `commerce-platform` paths
