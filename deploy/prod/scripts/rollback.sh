#!/bin/bash
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

WORK_DIR="/home/kgb581818/souzip"
DEPLOY_DIR="$WORK_DIR/deploy/prod"

BLUE_COMPOSE="docker-compose.blue.yaml"
GREEN_COMPOSE="docker-compose.green.yaml"

NGINX_UPSTREAM_FILE="/etc/nginx/conf.d/upstream-souzip.conf"

BLUE_PORT=8081
GREEN_PORT=8082

MAX_RETRY=6
RETRY_INTERVAL=10

BLUE_CONTAINER_NAME="${BLUE_CONTAINER_NAME:-souzip-api-blue}"
GREEN_CONTAINER_NAME="${GREEN_CONTAINER_NAME:-souzip-api-green}"

health_check() {
  local port="$1"
  curl -f -s --max-time 5 "http://localhost:${port}/actuator/health" > /dev/null 2>&1
}

container_logs_hint() {
  local name="$1"
  if docker ps -a --format '{{.Names}}' | grep -q "^${name}$"; then
    docker logs --tail 50 "$name" || true
  fi
}

cd "$DEPLOY_DIR" || exit 1

echo -e "${YELLOW}[1/7] 현재 active 포트 확인${NC}"

CURRENT_PORT=$(grep -oE 'server[[:space:]]+127\.0\.0\.1:([0-9]+)' "$NGINX_UPSTREAM_FILE" | grep -oE '[0-9]+' | head -n 1 || true)

if [[ "$CURRENT_PORT" == "$BLUE_PORT" ]]; then
  ACTIVE="blue"
  ACTIVE_PORT="$BLUE_PORT"
  ACTIVE_COMPOSE="$BLUE_COMPOSE"

  PREV="green"
  PREV_PORT="$GREEN_PORT"
  PREV_COMPOSE="$GREEN_COMPOSE"
elif [[ "$CURRENT_PORT" == "$GREEN_PORT" ]]; then
  ACTIVE="green"
  ACTIVE_PORT="$GREEN_PORT"
  ACTIVE_COMPOSE="$GREEN_COMPOSE"

  PREV="blue"
  PREV_PORT="$BLUE_PORT"
  PREV_COMPOSE="$BLUE_COMPOSE"
else
  if health_check "$BLUE_PORT"; then
    ACTIVE="blue"; ACTIVE_PORT="$BLUE_PORT"; ACTIVE_COMPOSE="$BLUE_COMPOSE"
    PREV="green"; PREV_PORT="$GREEN_PORT"; PREV_COMPOSE="$GREEN_COMPOSE"
  else
    ACTIVE="green"; ACTIVE_PORT="$GREEN_PORT"; ACTIVE_COMPOSE="$GREEN_COMPOSE"
    PREV="blue"; PREV_PORT="$BLUE_PORT"; PREV_COMPOSE="$BLUE_COMPOSE"
  fi
fi

echo -e "${GREEN}[INFO] active=${ACTIVE} → rollback=${PREV}${NC}"

echo -e "${YELLOW}[2/7] 롤백 대상 컨테이너 실행${NC}"
docker-compose -f "$PREV_COMPOSE" up -d

echo -e "${YELLOW}[3/7] 헬스체크${NC}"

RETRY_COUNT=0
HEALTH_OK=false

while [[ $RETRY_COUNT -lt $MAX_RETRY ]]; do
  if health_check "$PREV_PORT"; then
    HEALTH_OK=true
    break
  else
    RETRY_COUNT=$((RETRY_COUNT + 1))
    sleep $RETRY_INTERVAL
  fi
done

if [[ "$HEALTH_OK" != "true" ]]; then
  echo -e "${RED}[ERROR] 롤백 실패${NC}"
  docker-compose -f "$PREV_COMPOSE" down || true
  exit 1
fi

echo -e "${YELLOW}[4/7] nginx 전환${NC}"

sudo tee "$NGINX_UPSTREAM_FILE" > /dev/null <<EOF
upstream souzip {
    server 127.0.0.1:${PREV_PORT};
}
EOF

sudo nginx -t
sudo nginx -s reload

echo -e "${YELLOW}[5/7] 기존 컨테이너 종료${NC}"
docker-compose -f "$ACTIVE_COMPOSE" down || true

echo -e "${YELLOW}[6/7] 상태 확인${NC}"
docker ps | grep souzip || true

echo -e "${YELLOW}[7/7] 이미지 정리${NC}"
docker image prune -f || true

echo -e "${GREEN}ROLLBACK SUCCESS${NC}"