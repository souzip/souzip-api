#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

REGISTRY="asia-northeast3-docker.pkg.dev/souzip-488211/souzip-dev-repo/souzip-api"
WORK_DIR="/home/souzip-dev/souzip"
DEPLOY_DIR="$WORK_DIR/deploy/dev"

APP_COMPOSE="docker-compose.app.yaml"

HEALTH_CHECK_URL="http://localhost:8080/actuator/health"

MAX_RETRY=6
RETRY_INTERVAL=10

ENV_FILE="$DEPLOY_DIR/.env"

if [ -f "$ENV_FILE" ]; then
  DISCORD_WEBHOOK_URL="$(grep -E '^DISCORD_WEBHOOK_URL=' "$ENV_FILE" | tail -n 1 | cut -d= -f2- || true)"
  export DISCORD_WEBHOOK_URL
fi

echo -e "${YELLOW}[1/6] 롤백용 이미지 확인${NC}"
PREVIOUS_IMAGE=$(docker images ${REGISTRY}:previous -q || true)

if [ -z "${PREVIOUS_IMAGE:-}" ]; then
  echo -e "${RED}[ERROR] 롤백할 이전 이미지가 없습니다.${NC}"
  echo -e "${YELLOW}[INFO] 수동 복구가 필요합니다.${NC}"
  exit 1
fi
echo -e "${GREEN}[SUCCESS] 롤백 이미지 발견: ${PREVIOUS_IMAGE}${NC}"

echo -e "${YELLOW}[2/6] 현재 APP 컨테이너 중지${NC}"
cd "$DEPLOY_DIR" || exit 1
# FIX: docker-compose(V1) → docker compose(V2)
docker compose -f "$APP_COMPOSE" down 2>/dev/null || docker rm -f souzip-api 2>/dev/null || true
echo -e "${GREEN}[SUCCESS] APP 컨테이너 중지 완료${NC}"

echo -e "${YELLOW}[3/6] 이전 버전으로 태그 변경${NC}"
docker tag ${REGISTRY}:previous ${REGISTRY}:latest
echo -e "${GREEN}[SUCCESS] 태그 변경 완료${NC}"

echo -e "${YELLOW}[4/6] 이전 버전 APP 컨테이너 시작${NC}"
# FIX: docker-compose(V1) → docker compose(V2)
docker compose -f "$APP_COMPOSE" up -d
echo -e "${GREEN}[SUCCESS] 이전 버전 컨테이너 시작 완료${NC}"

echo -e "${YELLOW}[5/6] 애플리케이션 시작 대기${NC}"
sleep 10

echo -e "${YELLOW}[6/6] 롤백 후 헬스체크 (최대 ${MAX_RETRY}번 시도)${NC}"
RETRY_COUNT=0
HEALTH_OK=false

while [ $RETRY_COUNT -lt $MAX_RETRY ]; do
  if curl -f -s --max-time 5 "$HEALTH_CHECK_URL" > /dev/null 2>&1; then
    echo -e "${GREEN}[SUCCESS] 헬스체크 성공${NC}"
    HEALTH_OK=true
    break
  else
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo -e "${YELLOW}[RETRY] 헬스체크 실패 (${RETRY_COUNT}/${MAX_RETRY}) ${RETRY_INTERVAL}초 후 재시도${NC}"

    if [ $RETRY_COUNT -eq 3 ]; then
      echo -e "${YELLOW}[INFO] 컨테이너 로그 확인:${NC}"
      docker logs --tail 30 souzip-api || true
    fi

    sleep $RETRY_INTERVAL
  fi
done

if [ "$HEALTH_OK" = true ]; then
  echo -e "${GREEN}========================================${NC}"
  echo -e "${GREEN}  롤백 성공${NC}"
  echo -e "${GREEN}========================================${NC}"
  docker ps | grep souzip-api || true

  if [ ! -z "${DISCORD_WEBHOOK_URL:-}" ] && [ -f "$WORK_DIR/deploy/shared/discord-notify.sh" ]; then
    source "$WORK_DIR/deploy/shared/discord-notify.sh"
    # FIX: env 인자 추가
    notify_rollback_success "dev"
  fi
  exit 0
else
  echo -e "${RED}========================================${NC}"
  echo -e "${RED}  롤백 실패${NC}"
  echo -e "${RED}========================================${NC}"
  docker logs --tail 50 souzip-api || true

  if [ ! -z "${DISCORD_WEBHOOK_URL:-}" ] && [ -f "$WORK_DIR/deploy/shared/discord-notify.sh" ]; then
    source "$WORK_DIR/deploy/shared/discord-notify.sh"
    # FIX: env 인자 추가 (파일 잘려서 누락됐던 부분)
    notify_rollback_failed "dev"
  fi
  exit 1
fi
