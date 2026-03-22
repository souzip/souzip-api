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
DB_COMPOSE="docker-compose.db.yaml"
MONITORING_COMPOSE="docker-compose.monitoring.yaml"

HEALTH_CHECK_URL="http://localhost:8080/actuator/health"
MAX_RETRY=6
RETRY_INTERVAL=10

ENV_FILE="$DEPLOY_DIR/.env"

if [ -f "$ENV_FILE" ]; then
  while IFS='=' read -r key value; do
    case "$key" in
      DISCORD_WEBHOOK_URL|DEVELOP_DISCORD_WEBHOOK_URL|DEV_API_DOCS_URL|COMMIT_MESSAGE|DEPLOYER)
        export "$key=$value"
        ;;
    esac
  done < "$ENV_FILE"
fi

cd "$WORK_DIR" || exit 1

echo -e "${YELLOW}[1/10] 현재 이미지를 롤백용으로 보관${NC}"
CURRENT_IMAGE=$(docker images ${REGISTRY}:latest -q || true)
if [ ! -z "${CURRENT_IMAGE:-}" ]; then
  docker rmi ${REGISTRY}:previous 2>/dev/null || true
  docker tag ${REGISTRY}:latest ${REGISTRY}:previous
  echo -e "${GREEN}[SUCCESS] 롤백용 이미지 준비 완료: ${CURRENT_IMAGE}${NC}"
else
  echo -e "${YELLOW}[INFO] 기존 이미지 없음 (첫 배포)${NC}"
fi

echo -e "${YELLOW}[2/10] 오래된 이미지 정리${NC}"
OLD_IMAGES=$(docker images ${REGISTRY} -q | tail -n +3 || true)
if [ ! -z "${OLD_IMAGES:-}" ]; then
  echo "$OLD_IMAGES" | xargs docker rmi -f 2>/dev/null || true
  echo -e "${GREEN}[SUCCESS] 오래된 이미지 삭제 완료${NC}"
else
  echo -e "${YELLOW}[INFO] 삭제할 오래된 이미지 없음${NC}"
fi

echo -e "${YELLOW}[3/10] 최신 이미지 다운로드${NC}"
docker pull ${REGISTRY}:latest
NEW_IMAGE=$(docker images ${REGISTRY}:latest -q || true)
if [ -z "${NEW_IMAGE:-}" ]; then
  echo -e "${RED}[ERROR] 이미지 다운로드 실패${NC}"
  exit 1
fi
echo -e "${GREEN}[SUCCESS] 새 이미지: ${NEW_IMAGE}${NC}"

cd "$DEPLOY_DIR" || exit 1

if [ ! -f ".env" ]; then
  echo -e "${RED}[ERROR] .env 파일이 없습니다: $DEPLOY_DIR/.env${NC}"
  exit 1
fi

echo -e "${YELLOW}[4/10] DB 컨테이너 확인${NC}"
if ! docker ps --format '{{.Names}}' | grep -q '^souzip-dev-db$'; then
  docker compose -f "$DB_COMPOSE" up -d
  if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR] DB 컨테이너 시작 실패${NC}"
    exit 1
  fi
  echo -e "${GREEN}[SUCCESS] DB 컨테이너 시작 완료${NC}"
else
  echo -e "${GREEN}[SUCCESS] DB 컨테이너 이미 실행 중${NC}"
fi

echo -e "${YELLOW}[5/10] 모니터링 스택 확인${NC}"
PROMETHEUS_RUNNING=$(docker ps --format '{{.Names}}' | grep -q '^souzip-prometheus-dev$' && echo "true" || echo "false")
GRAFANA_RUNNING=$(docker ps --format '{{.Names}}' | grep -q '^souzip-grafana-dev$' && echo "true" || echo "false")

if [ "$PROMETHEUS_RUNNING" = "false" ] || [ "$GRAFANA_RUNNING" = "false" ]; then
  if [ -f "$MONITORING_COMPOSE" ]; then
    echo -e "${YELLOW}[INFO] 모니터링 스택 시작 중...${NC}"
    docker compose -f "$MONITORING_COMPOSE" up -d
    if [ $? -ne 0 ]; then
      echo -e "${RED}[WARNING] 모니터링 스택 시작 실패 (계속 진행)${NC}"
    else
      echo -e "${GREEN}[SUCCESS] 모니터링 스택 시작 완료${NC}"
    fi
  else
    echo -e "${YELLOW}[INFO] 모니터링 설정 파일 없음 ($MONITORING_COMPOSE)${NC}"
  fi
else
  echo -e "${GREEN}[SUCCESS] 모니터링 스택 이미 실행 중${NC}"
fi

echo -e "${YELLOW}[6/10] 기존 APP 컨테이너 중지${NC}"
docker compose -f "$APP_COMPOSE" down 2>/dev/null || docker rm -f souzip-api 2>/dev/null || true
echo -e "${GREEN}[SUCCESS] 기존 APP 컨테이너 중지 완료${NC}"

echo -e "${YELLOW}[7/10] 새 APP 컨테이너 시작${NC}"
docker compose -f "$APP_COMPOSE" up -d
if [ $? -ne 0 ]; then
  echo -e "${RED}[ERROR] APP 컨테이너 시작 실패${NC}"
  echo -e "${YELLOW}[INFO] 롤백을 시작합니다${NC}"
  "$DEPLOY_DIR/scripts/rollback.sh"
  exit 1
fi
echo -e "${GREEN}[SUCCESS] 새 컨테이너 시작 완료${NC}"

echo -e "${YELLOW}[8/10] 애플리케이션 시작 대기${NC}"
sleep 10

echo -e "${YELLOW}[9/10] 헬스체크 (최대 ${MAX_RETRY}번 시도)${NC}"
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

if [ "$HEALTH_OK" = false ]; then
  echo -e "${RED}[ERROR] 헬스체크 최종 실패${NC}"
  echo -e "${RED}[ERROR] 배포 실패 - 롤백 시작${NC}"
  docker logs --tail 100 souzip-api || true
  "$DEPLOY_DIR/scripts/rollback.sh"
  exit 1
fi

echo -e "${YELLOW}[10/10] 정리${NC}"
docker image prune -f || true

if [ ! -z "${DEVELOP_DISCORD_WEBHOOK_URL:-}" ] && [ -f "$WORK_DIR/deploy/shared/discord-notify.sh" ]; then
  source "$WORK_DIR/deploy/shared/discord-notify.sh"
  notify_deploy_success "$NEW_IMAGE" "$DEV_API_DOCS_URL" "$COMMIT_MESSAGE" "$DEPLOYER" "dev"
fi
