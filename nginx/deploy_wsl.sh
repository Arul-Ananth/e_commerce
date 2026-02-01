#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FRONTEND_DIR="${REPO_ROOT}/frontend"
BUILD_DIR="${FRONTEND_DIR}/dist"
DEPLOY_DIR="/var/www/ecommerce/frontend/dist"
NGINX_CONF_SRC="${REPO_ROOT}/nginx/nginx.conf"
NGINX_SITE_SRC="${REPO_ROOT}/nginx/conf.d/ecommerce.conf"
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

echo "==> Building frontend"
cd "${FRONTEND_DIR}"
if [[ ! -d "node_modules" ]]; then
  npm ci
fi
npm run build

echo "==> Deploying build artifacts to ${DEPLOY_DIR}"
sudo mkdir -p "${DEPLOY_DIR}"
sudo rsync -a --delete "${BUILD_DIR}/" "${DEPLOY_DIR}/"

echo "==> Linking Nginx configuration"
sudo mkdir -p /etc/nginx/conf.d /etc/nginx/certs
sudo ln -sf "${NGINX_CONF_SRC}" "${NGINX_CONF_DST}"
sudo ln -sf "${NGINX_SITE_SRC}" "${NGINX_SITE_DST}"
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
