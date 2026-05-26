#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
VENV_DIR="$BACKEND_DIR/.venv"
HOST="${BACKEND_HOST:-0.0.0.0}"
PORT="${BACKEND_PORT:-8000}"

detect_lan_ip() {
  local ip

  if command -v ipconfig >/dev/null 2>&1; then
    ip="$(ipconfig getifaddr en0 2>/dev/null || true)"
    if [[ -n "$ip" ]]; then
      echo "$ip"
      return
    fi
  fi

  if command -v ifconfig >/dev/null 2>&1; then
    ip="$(
      ifconfig |
        awk '/inet / && $2 !~ /^127\./ && $2 ~ /^(10\.|172\.(1[6-9]|2[0-9]|3[0-1])\.|192\.168\.)/ { print $2; exit }'
    )"
    if [[ -n "$ip" ]]; then
      echo "$ip"
      return
    fi
  fi
}

cd "$BACKEND_DIR"

if [[ ! -d "$VENV_DIR" ]]; then
  echo "Creating Python virtual environment: $VENV_DIR"
  python3 -m venv "$VENV_DIR"
fi

source "$VENV_DIR/bin/activate"

python -m pip install -r requirements.txt

LAN_IP="$(detect_lan_ip || true)"

echo "Starting backend at http://$HOST:$PORT"
echo "Local:   http://127.0.0.1:$PORT"
if [[ -n "$LAN_IP" ]]; then
  echo "LAN:     http://$LAN_IP:$PORT"
  echo "Health:  http://$LAN_IP:$PORT/health"
else
  echo "LAN:     未检测到局域网 IP，可用 ifconfig 查看当前 Wi-Fi 地址"
fi

exec uvicorn app.main:app --host "$HOST" --port "$PORT"
