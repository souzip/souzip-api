#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

REGISTRY="souzip.kr.ncr.ntruss.com/souzip/souzip-api"
WORK_DIR="/home/ubuntu/souzip"
DEPLOY_DIR="$WORK_DIR/deploy/dev"
APP_COMPOSE="docker-compose.app.yaml"

HEALTH_CHECK_URL="http://localhost:8080/actuator/health"
ENV_FILE="$DEPLOY_DIR/.env"

if [ -f "$ENV_FILE" ]; then
  DISCORD_WEBHOOK_URL="$(grep -E '^DISCORD_WEBHOOK_URL=' "$ENV_FILE" | tail -n 1 | cut -d= -f2- || true)"
  export DISCORD_WEBHOOK_URL
fi

echo -e "${YELLOW}[1/5] 롤백용 이미지 확인${NC}"
PREVIOUS_IMAGE=$(docker images ${REGISTRY}:previous -q || true)

if [ -z "${PREVIOUS_IMAGE:-}" ]; then
  echo -e "${RED}[ERROR] 롤백할 이전 이미지가 없습니다.${NC}"
  echo -e "${YELLOW}[INFO] 수동 복구가 필요합니다.${NC}"
  exit 1
fi
echo -e "${GREEN}[SUCCESS] 롤백 이미지 발견: ${PREVIOUS_IMAGE}${NC}"

echo -e "${YELLOW}[2/5] 현재 APP 컨테이너 중지${NC}"
cd "$DEPLOY_DIR" || exit 1
docker compose -f "$APP_COMPOSE" down 2>/dev/null || docker rm -f souzip-api 2>/dev/null || true
echo -e "${GREEN}[SUCCESS] APP 컨테이너 중지 완료${NC}"

echo -e "${YELLOW}[3/5] 이전 버전으로 태그 변경${NC}"
docker tag ${REGISTRY}:previous ${REGISTRY}:latest
echo -e "${GREEN}[SUCCESS] 태그 변경 완료${NC}"

echo -e "${YELLOW}[4/5] 이전 버전 APP 컨테이너 시작${NC}"
docker compose -f "$APP_COMPOSE" up -d
echo -e "${GREEN}[SUCCESS] 이전 버전 컨테이너 시작 완료${NC}"
sleep 10

echo -e "${YELLOW}[5/5] 롤백 후 헬스체크${NC}"
if curl -f -s --max-time 5 "$HEALTH_CHECK_URL" > /dev/null 2>&1; then
  echo -e "${GREEN}========================================${NC}"
  echo -e "${GREEN}  롤백 성공${NC}"
  echo -e "${GREEN}========================================${NC}"
  docker ps | grep souzip-api || true

  if [ ! -z "${DISCORD_WEBHOOK_URL:-}" ] && [ -f "$WORK_DIR/deploy/shared/discord-notify.sh" ]; then
    source "$WORK_DIR/deploy/shared/discord-notify.sh"
    notify_rollback_success
  fi
  exit 0
else
  echo -e "${RED}========================================${NC}"
  echo -e "${RED}  롤백 실패${NC}"
  echo -e "${RED}========================================${NC}"
  docker logs --tail 50 souzip-api || true

  if [ ! -z "${DISCORD_WEBHOOK_URL:-}" ] && [ -f "$WORK_DIR/deploy/shared/discord-notify.sh" ]; then
    source "$WORK_DIR/deploy/shared/discord-notify.sh"
    notify_rollback_failed
  fi
  exit 1
fi
