#!/usr/bin/env bash
# Levanta la MIKORI API en local para pruebas.
# Uso: ./serve.sh            (por defecto en 0.0.0.0:8000)
#      PORT=9000 ./serve.sh  (puerto personalizado)
set -e

cd "$(dirname "$0")"

if [ ! -f .env ]; then
  cp .env.example .env
  echo "→ .env creado desde .env.example (SQLite)."
fi

echo "→ Aplicando migraciones…"
php database/migrate.php

PORT="${PORT:-8000}"
echo "→ MIKORI API en http://0.0.0.0:${PORT}  (health: /v1/health)"
echo "  (Ctrl+C para detener)"
php -S "0.0.0.0:${PORT}" -t public
