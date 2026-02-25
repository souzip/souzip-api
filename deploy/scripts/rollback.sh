#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

REGISTRY="souzip.kr.ncr.ntruss.com/souzip/souzip-api"
WORK_DIR="/home/ubuntu/souzip"
HEALTH_CHECK_URL="http://localhost:8080/actuator/health"

if [ -f "$WORK_DIR/deploy/.env" ]; then
    export $(grep DISCORD_WEBHOOK_URL "$WORK_DIR/deploy/.env" | xargs)
fi

cd $WORK_DIR || exit 1

echo -e "${YELLOW}[1/5] 롤백용 이미지 확인${NC}"
PREVIOUS_IMAGE=$(docker images ${REGISTRY}:previous -q)

if [ -z "$PREVIOUS_IMAGE" ]; then
    echo -e "${RED}[ERROR] 롤백할 이전 이미지가 없습니다.${NC}"
    echo -e "${YELLOW}[INFO] 수동 복구가 필요합니다.${NC}"
    exit 1
fi

echo -e "${GREEN}[SUCCESS] 롤백 이미지 발견: ${PREVIOUS_IMAGE}${NC}"

echo -e "${YELLOW}[2/5] 현재 컨테이너 중지${NC}"
cd $WORK_DIR/deploy
docker-compose down 2>/dev/null || docker rm -f souzip-api 2>/dev/null || true
echo -e "${GREEN}[SUCCESS] 컨테이너 중지 완료${NC}"

echo -e "${YELLOW}[3/5] 이전 버전으로 태그 변경${NC}"
docker tag ${REGISTRY}:previous ${REGISTRY}:latest
echo -e "${GREEN}[SUCCESS] 태그 변경 완료${NC}"

echo -e "${YELLOW}[4/5] 이전 버전 컨테이너 시작${NC}"
cd $WORK_DIR/deploy

if [ ! -f .env ]; then
    echo -e "${RED}[ERROR] .env 파일이 없습니다${NC}"
    exit 1
fi

docker-compose up -d postgres
if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR] DB 컨테이너 시작 실패${NC}"
    exit 1
fi

docker-compose up -d souzip-api
if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR] API 컨테이너 시작 실패${NC}"
    exit 1
fi

echo -e "${GREEN}[SUCCESS] 이전 버전 컨테이너 시작 완료${NC}"
sleep 10

echo -e "${YELLOW}[5/5] 롤백 후 헬스체크${NC}"
if curl -f -s --max-time 5 $HEALTH_CHECK_URL > /dev/null 2>&1; then
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  롤백 성공${NC}"
    echo -e "${GREEN}========================================${NC}"
    docker ps | grep souzip-api

    if [ ! -z "$DISCORD_WEBHOOK_URL" ] && [ -f "$WORK_DIR/deploy/notification/discord-notify.sh" ]; then
        source "$WORK_DIR/deploy/notification/discord-notify.sh"
        notify_rollback_success
    fi

    exit 0
else
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}  롤백 실패${NC}"
    echo -e "${RED}========================================${NC}"
    docker logs --tail 50 souzip-api

    if [ ! -z "$DISCORD_WEBHOOK_URL" ] && [ -f "$WORK_DIR/deploy/notification/discord-notify.sh" ]; then
        source "$WORK_DIR/deploy/notification/discord-notify.sh"
        notify_rollback_failed
    fi

    exit 1
fi