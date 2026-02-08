#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

REGISTRY="souzip.kr.ncr.ntruss.com/souzip/souzip-api"
WORK_DIR="/home/ubuntu/souzip"
HEALTH_CHECK_URL="http://localhost:8080/actuator/health"
MAX_RETRY=6
RETRY_INTERVAL=10

if [ -f "$WORK_DIR/deploy/.env" ]; then
    export $(grep DISCORD_WEBHOOK_URL "$WORK_DIR/deploy/.env" | xargs)
fi

cd $WORK_DIR || exit 1

echo -e "${YELLOW}[1/9] 현재 이미지를 롤백용으로 보관${NC}"
CURRENT_IMAGE=$(docker images ${REGISTRY}:latest -q)
if [ ! -z "$CURRENT_IMAGE" ]; then
    docker rmi ${REGISTRY}:previous 2>/dev/null || true

    docker tag ${REGISTRY}:latest ${REGISTRY}:previous
    echo -e "${GREEN}[SUCCESS] 롤백용 이미지 준비 완료: ${CURRENT_IMAGE}${NC}"
else
    echo -e "${YELLOW}[INFO] 기존 이미지 없음 (첫 배포)${NC}"
fi

echo -e "${YELLOW}[2/9] 오래된 이미지 정리${NC}"
OLD_IMAGES=$(docker images ${REGISTRY} -q | tail -n +3)
if [ ! -z "$OLD_IMAGES" ]; then
    echo "$OLD_IMAGES" | xargs docker rmi -f 2>/dev/null || true
    echo -e "${GREEN}[SUCCESS] 오래된 이미지 삭제 완료${NC}"
else
    echo -e "${YELLOW}[INFO] 삭제할 오래된 이미지 없음${NC}"
fi

echo -e "${YELLOW}[3/9] 최신 이미지 다운로드${NC}"
docker pull ${REGISTRY}:latest
if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR] 이미지 다운로드 실패${NC}"
    exit 1
fi
NEW_IMAGE=$(docker images ${REGISTRY}:latest -q)
echo -e "${GREEN}[SUCCESS] 새 이미지: ${NEW_IMAGE}${NC}"

echo -e "${YELLOW}[4/9] 기존 컨테이너 중지${NC}"
cd $WORK_DIR/deploy
docker-compose down 2>/dev/null || docker rm -f souzip-api 2>/dev/null || true
echo -e "${GREEN}[SUCCESS] 기존 컨테이너 중지 완료${NC}"

echo -e "${YELLOW}[5/9] 새 컨테이너 시작${NC}"
cd $WORK_DIR/deploy

if [ ! -f .env ]; then
    echo -e "${RED}[ERROR] .env 파일이 없습니다${NC}"
    echo -e "${YELLOW}[INFO] .env.template을 복사하여 .env 파일을 생성하세요${NC}"
    exit 1
fi

docker-compose up -d

if [ $? -ne 0 ]; then
    echo -e "${RED}[ERROR] 컨테이너 시작 실패${NC}"
    echo -e "${YELLOW}[INFO] 롤백을 시작합니다${NC}"
    $WORK_DIR/deploy/scripts/rollback.sh
    exit 1
fi
echo -e "${GREEN}[SUCCESS] 새 컨테이너 시작 완료${NC}"

echo -e "${YELLOW}[6/9] 애플리케이션 시작 대기${NC}"
sleep 10

echo -e "${YELLOW}[7/9] 헬스체크 (최대 ${MAX_RETRY}번 시도)${NC}"
RETRY_COUNT=0
HEALTH_OK=false

while [ $RETRY_COUNT -lt $MAX_RETRY ]; do
    if curl -f -s --max-time 5 $HEALTH_CHECK_URL > /dev/null 2>&1; then
        echo -e "${GREEN}[SUCCESS] 헬스체크 성공${NC}"
        HEALTH_OK=true
        break
    else
        RETRY_COUNT=$((RETRY_COUNT + 1))
        echo -e "${YELLOW}[RETRY] 헬스체크 실패 (${RETRY_COUNT}/${MAX_RETRY}) ${RETRY_INTERVAL}초 후 재시도${NC}"

        if [ $RETRY_COUNT -eq 3 ]; then
            echo -e "${YELLOW}[INFO] 컨테이너 로그 확인:${NC}"
            docker logs --tail 30 souzip-api
        fi

        sleep $RETRY_INTERVAL
    fi
done

if [ "$HEALTH_OK" = false ]; then
    echo -e "${RED}[ERROR] 헬스체크 최종 실패${NC}"
    echo -e "${RED}[ERROR] 배포 실패 - 롤백 시작${NC}"

    echo -e "${YELLOW}=== 실패한 컨테이너 로그 ===${NC}"
    docker logs --tail 100 souzip-api

    $WORK_DIR/deploy/scripts/rollback.sh
    exit 1
fi

echo -e "${YELLOW}[8/9] 최종 확인${NC}"
docker ps | grep souzip-api

echo -e "${YELLOW}[9/9] 정리${NC}"
docker image prune -f

if [ ! -z "$DISCORD_WEBHOOK_URL" ] && [ -f "$WORK_DIR/deploy/notification/discord-notify.sh" ]; then
    source "$WORK_DIR/deploy/notification/discord-notify.sh"
    notify_deploy_success "$NEW_IMAGE"
fi
