#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="${REPO_ROOT}/frontend"
BUILD_DIR="${FRONTEND_DIR}/dist"
DEPLOY_DIR="/var/www/ecommerce/frontend/dist"
NGINX_CONF_SRC="${REPO_ROOT}/nginx/nginx.conf"
NGINX_SITE_SRC="${REPO_ROOT}/nginx/conf.d/ecommerce.conf"
BACKEND_CFG="${REPO_ROOT}/nginx/backend-upstream.conf"
NGINX_CONF_DST="/etc/nginx/nginx.conf"
NGINX_SITE_DST="/etc/nginx/conf.d/ecommerce.conf"
CERT_PATH="/etc/nginx/certs/ecommerce.crt"
KEY_PATH="/etc/nginx/certs/ecommerce.key"

echo "==> Installing Nginx if missing"
if ! command -v nginx >/dev/null 2>&1; then
  sudo apt-get update
  sudo apt-get install -y nginx
fi

if ! command -v rsync >/dev/null 2>&1; then
  sudo apt-get install -y rsync
fi

if ! command -v npm >/dev/null 2>&1; then
  echo "npm not found. Please install Node.js/npm in WSL before running this script."
  exit 1
fi

NODE_BIN=""
if command -v node >/dev/null 2>&1; then
  NODE_BIN="node"
elif command -v nodejs >/dev/null 2>&1; then
  NODE_BIN="nodejs"
else
  echo "Node.js runtime not found. Install Node.js 18+ in WSL and retry."
  exit 1
fi

NODE_MAJOR="$(${NODE_BIN} -p 'process.versions.node.split(".")[0]' 2>/dev/null || echo 0)"
if [[ "${NODE_MAJOR}" -lt 18 ]]; then
  echo "Node.js 18+ is required. Current version: $(${NODE_BIN} -v 2>/dev/null || echo unknown)"
  echo "Install a newer Node.js (recommended: Node 20 LTS) and retry."
  exit 1
fi

echo "==> Building frontend"
cd "${FRONTEND_DIR}"
if [[ ! -d "node_modules" || ! -x "node_modules/.bin/vite" ]]; then
  echo "==> Installing frontend dependencies"
  if [[ -f "package-lock.json" ]]; then
    npm ci || npm install
  else
    npm install
  fi
fi
npm run build

echo "==> Deploying build artifacts to ${DEPLOY_DIR}"
sudo mkdir -p "${DEPLOY_DIR}"
sudo rsync -a --delete "${BUILD_DIR}/" "${DEPLOY_DIR}/"

echo "==> Linking Nginx configuration"
sudo mkdir -p /etc/nginx/conf.d /etc/nginx/certs
sudo ln -sf "${NGINX_CONF_SRC}" "${NGINX_CONF_DST}"

if [[ ! -f "${BACKEND_CFG}" ]]; then
  echo "Missing ${BACKEND_CFG}"
  echo "Create nginx/backend-upstream.conf from .example and set BACKEND_UPSTREAM."
  exit 1
fi

raw_upstream_line="$(grep -E '^[[:space:]]*BACKEND_UPSTREAM=' "${BACKEND_CFG}" | tail -n1 || true)"
if [[ -z "${raw_upstream_line}" ]]; then
  echo "BACKEND_UPSTREAM not found in ${BACKEND_CFG}"
  echo "Create nginx/backend-upstream.conf from .example and set BACKEND_UPSTREAM."
  exit 1
fi

BACKEND_UPSTREAM="${raw_upstream_line#*=}"
BACKEND_UPSTREAM="$(printf '%s' "${BACKEND_UPSTREAM}" | sed -E 's/^[[:space:]]+//; s/[[:space:]]+$//')"

if [[ -z "${BACKEND_UPSTREAM}" ]]; then
  echo "BACKEND_UPSTREAM is empty in ${BACKEND_CFG}"
  exit 1
fi

if [[ ! "${BACKEND_UPSTREAM}" =~ ^[A-Za-z0-9._-]+:[0-9]{1,5}$ ]]; then
  echo "Invalid BACKEND_UPSTREAM='${BACKEND_UPSTREAM}' in ${BACKEND_CFG}"
  echo "Expected format: host:port (no scheme/path)."
  exit 1
fi

placeholder_count="$( (grep -o '__BACKEND_UPSTREAM__' "${NGINX_SITE_SRC}" || true) | wc -l | tr -d ' ' )"
if [[ "${placeholder_count}" -lt 2 ]]; then
  echo "Template ${NGINX_SITE_SRC} must contain __BACKEND_UPSTREAM__ placeholder in /api and /auth."
  exit 1
fi

echo "==> Using BACKEND_UPSTREAM=${BACKEND_UPSTREAM}"
tmp_site="$(mktemp)"
sed "s|__BACKEND_UPSTREAM__|${BACKEND_UPSTREAM}|g" "${NGINX_SITE_SRC}" > "${tmp_site}"
sudo cp "${tmp_site}" "${NGINX_SITE_DST}"
rm -f "${tmp_site}"
echo "==> Rendered ${NGINX_SITE_DST} from template"

if [[ -f /etc/nginx/sites-enabled/default ]]; then
  sudo rm -f /etc/nginx/sites-enabled/default
fi

if [[ -f "${CERT_PATH}" && -f "${KEY_PATH}" ]]; then
  echo "==> Testing and reloading Nginx"
  sudo nginx -t
  if command -v systemctl >/dev/null 2>&1; then
    sudo systemctl reload nginx || sudo systemctl restart nginx
  else
    sudo service nginx reload || sudo nginx -s reload
  fi
else
  echo "==> Skipping Nginx reload because TLS certs are missing:"
  echo "    ${CERT_PATH}"
  echo "    ${KEY_PATH}"
fi
