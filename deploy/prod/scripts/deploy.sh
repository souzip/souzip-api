#!/bin/bash
set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

REGISTRY="asia-northeast3-docker.pkg.dev/souzip-488211/souzip-prod-repo/souzip-api"

WORK_DIR="/home/souzip-prod/souzip"
DEPLOY_DIR="$WORK_DIR/deploy/prod"

BLUE_COMPOSE="docker-compose.blue.yaml"
GREEN_COMPOSE="docker-compose.green.yaml"
MONITORING_COMPOSE="docker-compose.monitoring.yaml"
LOGGING_COMPOSE="docker-compose.logging.yaml"

BLUE_PROJECT="souzip-blue"
GREEN_PROJECT="souzip-green"

NGINX_UPSTREAM_FILE="/etc/nginx/conf.d/upstream-souzip.conf"

BLUE_PORT=8081
GREEN_PORT=8082

MAX_RETRY=6
RETRY_INTERVAL=10

ENV_FILE="$DEPLOY_DIR/.env"
if [ -f "$ENV_FILE" ]; then
  while IFS='=' read -r key value; do
    case "$key" in
      DISCORD_WEBHOOK_URL|PROD_DISCORD_WEBHOOK_URL|PROD_API_DOCS_URL|COMMIT_MESSAGE|DEPLOYER)
        export "$key=$value"
        ;;
    esac
  done < "$ENV_FILE"
fi

cd "$WORK_DIR" || exit 1

echo -e "${YELLOW}[1/8] 최신 이미지 다운로드${NC}"

docker pull ${REGISTRY}:latest

NEW_IMAGE=$(docker images ${REGISTRY}:latest -q)

if [ -z "${NEW_IMAGE:-}" ]; then
  echo -e "${RED}[ERROR] 이미지 다운로드 실패${NC}"
  exit 1
fi

echo -e "${GREEN}[SUCCESS] 새 이미지: ${NEW_IMAGE}${NC}"

cd "$DEPLOY_DIR" || exit 1

[ -f "$NGINX_UPSTREAM_FILE" ] || {
  echo -e "${RED}[ERROR] upstream 파일 없음${NC}"
  exit 1
}

echo -e "${YELLOW}[2/8] 현재 active 포트 확인${NC}"

CURRENT_PORT=$(grep -oE '127\.0\.0\.1:[0-9]+' "$NGINX_UPSTREAM_FILE" | cut -d: -f2 | head -n 1 || true)

if [ -z "$CURRENT_PORT" ]; then
  echo -e "${YELLOW}[WARN] upstream 없음 → 최초 배포 (blue=8081)${NC}"
  CURRENT_PORT="none"
fi

if [ "$CURRENT_PORT" == "$BLUE_PORT" ]; then
  TARGET="green"
  TARGET_PORT=$GREEN_PORT
  TARGET_PROJECT=$GREEN_PROJECT
  TARGET_COMPOSE=$GREEN_COMPOSE

  STOP_PROJECT=$BLUE_PROJECT
  STOP_COMPOSE=$BLUE_COMPOSE
else
  TARGET="blue"
  TARGET_PORT=$BLUE_PORT
  TARGET_PROJECT=$BLUE_PROJECT
  TARGET_COMPOSE=$BLUE_COMPOSE

  STOP_PROJECT=$GREEN_PROJECT
  STOP_COMPOSE=$GREEN_COMPOSE
fi

echo -e "${GREEN}[INFO] 현재:$CURRENT_PORT → 배포:$TARGET($TARGET_PORT)${NC}"

echo -e "${YELLOW}[3/8] 모니터링 스택 확인${NC}"
PROMETHEUS_RUNNING=$(docker ps --format '{{.Names}}' | grep -q '^souzip-prometheus-prod$' && echo "true" || echo "false")
GRAFANA_RUNNING=$(docker ps --format '{{.Names}}' | grep -q '^souzip-grafana-prod$' && echo "true" || echo "false")
NODE_EXPORTER_RUNNING=$(docker ps --format '{{.Names}}' | grep -q '^souzip-node-exporter$' && echo "true" || echo "false")
CADVISOR_RUNNING=$(docker ps --format '{{.Names}}' | grep -q '^souzip-cadvisor$' && echo "true" || echo "false")

if [ "$PROMETHEUS_RUNNING" = "false" ] || [ "$GRAFANA_RUNNING" = "false" ] || [ "$NODE_EXPORTER_RUNNING" = "false" ] || [ "$CADVISOR_RUNNING" = "false" ]; then
  if [ -f "$MONITORING_COMPOSE" ]; then
    echo -e "${YELLOW}[INFO] 모니터링 스택 시작 중...${NC}"
    if docker compose -f "$MONITORING_COMPOSE" up -d; then
      echo -e "${GREEN}[SUCCESS] 모니터링 스택 시작 완료${NC}"
    else
      echo -e "${RED}[WARNING] 모니터링 스택 시작 실패 (계속 진행)${NC}"
    fi
  else
    echo -e "${YELLOW}[INFO] 모니터링 설정 파일 없음 ($MONITORING_COMPOSE)${NC}"
  fi
else
  echo -e "${GREEN}[SUCCESS] 모니터링 스택 이미 실행 중${NC}"
fi

echo -e "${YELLOW}[3.5/8] 로그 수집 스택 확인${NC}"
LOKI_RUNNING=$(docker ps --format '{{.Names}}' | grep -q '^souzip-loki-prod$' && echo "true" || echo "false")
PROMTAIL_RUNNING=$(docker ps --format '{{.Names}}' | grep -q '^souzip-promtail-prod$' && echo "true" || echo "false")

if [ "$LOKI_RUNNING" = "false" ] || [ "$PROMTAIL_RUNNING" = "false" ]; then
  if [ -f "$LOGGING_COMPOSE" ]; then
    echo -e "${YELLOW}[INFO] 로그 수집 스택 시작 중...${NC}"
    if docker compose -f "$LOGGING_COMPOSE" up -d; then
      echo -e "${GREEN}[SUCCESS] 로그 수집 스택 시작 완료${NC}"
    else
      echo -e "${RED}[WARNING] 로그 수집 스택 시작 실패 (계속 진행)${NC}"
    fi
  else
    echo -e "${YELLOW}[INFO] 로그 수집 설정 파일 없음 ($LOGGING_COMPOSE)${NC}"
  fi
else
  echo -e "${GREEN}[SUCCESS] 로그 수집 스택 이미 실행 중${NC}"
fi

echo -e "${YELLOW}[4/8] $TARGET 컨테이너 실행${NC}"

docker compose -p "$TARGET_PROJECT" -f "$TARGET_COMPOSE" pull
docker compose -p "$TARGET_PROJECT" -f "$TARGET_COMPOSE" up -d

echo -e "${YELLOW}[5/8] 헬스체크 시작${NC}"

RETRY_COUNT=0
HEALTH_OK=false

while [ $RETRY_COUNT -lt $MAX_RETRY ]; do

  if curl -f -s --max-time 5 "http://localhost:${TARGET_PORT}/actuator/health" > /dev/null; then
    echo -e "${GREEN}[SUCCESS] 헬스체크 성공${NC}"
    HEALTH_OK=true
    break
  else
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo -e "${YELLOW}[RETRY] ${RETRY_COUNT}/${MAX_RETRY}${NC}"
    sleep $RETRY_INTERVAL
  fi

done

if [ "$HEALTH_OK" = false ]; then
  echo -e "${RED}[ERROR] 헬스체크 실패 - 자동 롤백 완료 (nginx 전환 없음)${NC}"
  docker compose -p "$TARGET_PROJECT" -f "$TARGET_COMPOSE" down

  if [ ! -z "${PROD_DISCORD_WEBHOOK_URL:-}" ] && [ -f "$WORK_DIR/deploy/shared/discord-notify.sh" ]; then
    source "$WORK_DIR/deploy/shared/discord-notify.sh"
    notify_rollback_success "prod"
  fi
  exit 1
fi

echo -e "${YELLOW}[6/8] nginx upstream 전환${NC}"

sudo tee "$NGINX_UPSTREAM_FILE" > /dev/null <<EOF
upstream souzip {
    server 127.0.0.1:${TARGET_PORT};
}
EOF

sudo nginx -t
sudo nginx -s reload

echo -e "${GREEN}[SUCCESS] nginx 전환 완료${NC}"

echo -e "${YELLOW}[7/8] 이전 컨테이너 종료${NC}"

docker compose -p "$STOP_PROJECT" -f "$STOP_COMPOSE" down || true

echo -e "${YELLOW}[8/8] 이미지 정리${NC}"

docker image prune -f || true

echo -e "${GREEN}[DEPLOY SUCCESS] 완료${NC}"

if [ ! -z "${PROD_DISCORD_WEBHOOK_URL:-}" ] && [ -f "$WORK_DIR/deploy/shared/discord-notify.sh" ]; then
  source "$WORK_DIR/deploy/shared/discord-notify.sh"
  notify_deploy_success "$NEW_IMAGE" "${PROD_API_DOCS_URL:-}" "${COMMIT_MESSAGE:-}" "${DEPLOYER:-}" "prod"
fi
