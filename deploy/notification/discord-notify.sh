#!/bin/bash

DISCORD_WEBHOOK_URL="${DISCORD_WEBHOOK_URL:-}"

COLOR_RED=15158332
COLOR_GREEN=3066993
COLOR_BLUE=3447003
COLOR_YELLOW=16776960

notify_deploy_success() {
    local image_id=$1
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    if [ -z "$DISCORD_WEBHOOK_URL" ]; then
        return
    fi

    curl -s -H "Content-Type: application/json" \
         -X POST \
         -d "{
           \"username\": \"Souzip Bot\",
           \"embeds\": [{
             \"title\": \"배포 완료\",
             \"description\": \"성공적으로 배포되었습니다.\",
             \"color\": ${COLOR_GREEN},
             \"fields\": [
               {\"name\": \"시간\", \"value\": \"${timestamp}\", \"inline\": true},
               {\"name\": \"상태\", \"value\": \"헬스체크 통과\", \"inline\": false}
             ]
           }]
         }" \
         "${DISCORD_WEBHOOK_URL}" > /dev/null
}

notify_rollback_success() {
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    if [ -z "$DISCORD_WEBHOOK_URL" ]; then
        return
    fi

    curl -s -H "Content-Type: application/json" \
         -X POST \
         -d "{
           \"username\": \"Souzip Bot\",
           \"embeds\": [{
             \"title\": \"롤백 성공\",
             \"description\": \"이전 버전으로 복구되었습니다.\",
             \"color\": ${COLOR_GREEN},
             \"fields\": [
               {\"name\": \"시간\", \"value\": \"${timestamp}\", \"inline\": true},
               {\"name\": \"상태\", \"value\": \"헬스체크 통과\", \"inline\": false}
             ]
           }]
         }" \
         "${DISCORD_WEBHOOK_URL}" > /dev/null
}

notify_rollback_failed() {
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    if [ -z "$DISCORD_WEBHOOK_URL" ]; then
        return
    fi

    curl -s -H "Content-Type: application/json" \
         -X POST \
         -d "{
           \"username\": \"Souzip Bot\",
           \"content\": \"@here 롤백 실패\",
           \"embeds\": [{
             \"title\": \"롤백 실패\",
             \"description\": \"롤백 후에도 서버가 작동하지 않습니다.\",
             \"color\": ${COLOR_RED},
             \"fields\": [
               {\"name\": \"시간\", \"value\": \"${timestamp}\", \"inline\": true},
               {\"name\": \"상태\", \"value\": \"수동 복구 필요\", \"inline\": false}
             ]
           }]
         }" \
         "${DISCORD_WEBHOOK_URL}" > /dev/null
}

notify_server_down() {
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    if [ -z "$DISCORD_WEBHOOK_URL" ]; then
        return
    fi

    curl -s -H "Content-Type: application/json" \
         -X POST \
         -d "{
           \"username\": \"Souzip Bot\",
           \"content\": \"@here 서버 다운\",
           \"embeds\": [{
             \"title\": \"서버 다운\",
             \"description\": \"서버가 응답하지 않습니다.\",
             \"color\": ${COLOR_RED},
             \"fields\": [
               {\"name\": \"시간\", \"value\": \"${timestamp}\", \"inline\": true},
               {\"name\": \"상태\", \"value\": \"헬스체크 실패\", \"inline\": false}
             ]
           }]
         }" \
         "${DISCORD_WEBHOOK_URL}" > /dev/null
}

notify_server_up() {
    local downtime=$1
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    if [ -z "$DISCORD_WEBHOOK_URL" ]; then
        return
    fi

    curl -s -H "Content-Type: application/json" \
         -X POST \
         -d "{
           \"username\": \"Souzip Bot\",
           \"embeds\": [{
             \"title\": \"서버 복구\",
             \"description\": \"서버가 복구되었습니다.\",
             \"color\": ${COLOR_GREEN},
             \"fields\": [
               {\"name\": \"복구 시간\", \"value\": \"${timestamp}\", \"inline\": true},
               {\"name\": \"다운타임\", \"value\": \"${downtime}\", \"inline\": false}
             ]
           }]
         }" \
         "${DISCORD_WEBHOOK_URL}" > /dev/null
}

notify_disk_warning() {
    local disk_usage=$1
    local disk_percent=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    if [ -z "$DISCORD_WEBHOOK_URL" ]; then
        return
    fi

    curl -s -H "Content-Type: application/json" \
         -X POST \
         -d "{
           \"username\": \"Souzip Bot\",
           \"content\": \"@here 디스크 공간 부족\",
           \"embeds\": [{
             \"title\": \"디스크 사용량 경고\",
             \"description\": \"디스크 사용량이 임계값을 초과했습니다.\",
             \"color\": ${COLOR_YELLOW},
             \"fields\": [
               {\"name\": \"시간\", \"value\": \"${timestamp}\", \"inline\": true},
               {\"name\": \"디스크 사용량\", \"value\": \"${disk_usage} (${disk_percent}%)\", \"inline\": false}
             ]
           }]
         }" \
         "${DISCORD_WEBHOOK_URL}" > /dev/null
}

notify_memory_warning() {
    local memory_usage=$1
    local memory_percent=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    if [ -z "$DISCORD_WEBHOOK_URL" ]; then
        return
    fi

    curl -s -H "Content-Type: application/json" \
         -X POST \
         -d "{
           \"username\": \"Souzip Bot\",
           \"content\": \"@here 메모리 부족\",
           \"embeds\": [{
             \"title\": \"메모리 사용량 경고\",
             \"description\": \"메모리 사용량이 임계값을 초과했습니다.\",
             \"color\": ${COLOR_RED},
             \"fields\": [
               {\"name\": \"시간\", \"value\": \"${timestamp}\", \"inline\": true},
               {\"name\": \"메모리 사용량\", \"value\": \"${memory_usage} (${memory_percent}%)\", \"inline\": false}
             ]
           }]
         }" \
         "${DISCORD_WEBHOOK_URL}" > /dev/null
}

notify_container_stopped() {
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    if [ -z "$DISCORD_WEBHOOK_URL" ]; then
        return
    fi

    curl -s -H "Content-Type: application/json" \
         -X POST \
         -d "{
           \"username\": \"Souzip Bot\",
           \"content\": \"@here 컨테이너 중지\",
           \"embeds\": [{
             \"title\": \"컨테이너 중지\",
             \"description\": \"컨테이너가 중지되었습니다.\",
             \"color\": ${COLOR_RED},
             \"fields\": [
               {\"name\": \"시간\", \"value\": \"${timestamp}\", \"inline\": true}
             ]
           }]
         }" \
         "${DISCORD_WEBHOOK_URL}" > /dev/null
}
