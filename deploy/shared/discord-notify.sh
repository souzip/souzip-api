#!/bin/bash
set -e

COLOR_RED=15158332
COLOR_GREEN=3066993
COLOR_BLUE=3447003
COLOR_YELLOW=16776960

DISCORD_WEBHOOK_URL="${DISCORD_WEBHOOK_URL:-}"
DEVELOP_DISCORD_WEBHOOK_URL="${DEVELOP_DISCORD_WEBHOOK_URL:-}"
PROD_DISCORD_WEBHOOK_URL="${PROD_DISCORD_WEBHOOK_URL:-}"

DEV_WORK_DIR="/home/souzip-dev/souzip"
PROD_WORK_DIR="/home/souzip-prod/souzip"

get_workdir() {
    local env=$1
    if [ "$env" = "prod" ]; then
        echo "$PROD_WORK_DIR"
    else
        echo "$DEV_WORK_DIR"
    fi
}

notify_deploy_success() {
    local image_id=$1
    local api_docs_url=${2:-""}
    local commit_message=${3:-""}
    local deployer=${4:-""}
    local env=${5:-"dev"}
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')

    local webhook_url
    if [ "$env" = "prod" ]; then
        webhook_url="$PROD_DISCORD_WEBHOOK_URL"
    else
        webhook_url="$DEVELOP_DISCORD_WEBHOOK_URL"
    fi

    [ -z "$webhook_url" ] && return

    PAYLOAD=$(jq -n \
      --arg image_id "$image_id" \
      --arg msg "$commit_message" \
      --arg url "$api_docs_url" \
      --arg ts "$timestamp" \
      --arg deployer "$deployer" \
      --arg env "$env" \
      --argjson color "$COLOR_GREEN" \
      '{
        username: "Souzip Bot",
        embeds: [{
          title: ("배포 완료 [" + $env + "]"),
          description: ("\($msg)\n\n[API 문서 바로가기](\($url))"),
          color: $color,
          fields: [
            {name: "환경", value: $env, inline: true},
            {name: "이미지 ID", value: $image_id, inline: false},
            {name: "시간", value: $ts, inline: true},
            {name: "담당자", value: $deployer, inline: true},
            {name: "상태", value: "헬스체크 통과", inline: false}
          ]
        }]
      }')

    curl -s -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "${webhook_url}" > /dev/null
}

notify_rollback_success() {
    local env=${1:-"dev"}
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$DISCORD_WEBHOOK_URL" ] && return

    PAYLOAD=$(jq -n \
      --arg ts "$timestamp" \
      --arg env "$env" \
      --argjson color "$COLOR_GREEN" \
      '{
        username: "Souzip Bot",
        embeds: [{
          title: ("롤백 성공 [" + $env + "]"),
          description: "이전 버전으로 복구되었습니다.",
          color: $color,
          fields: [
            {name: "환경", value: $env, inline: true},
            {name: "시간", value: $ts, inline: true},
            {name: "상태", value: "헬스체크 통과", inline: false}
          ]
        }]
      }')

    curl -s -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$DISCORD_WEBHOOK_URL" > /dev/null
}

notify_rollback_failed() {
    local env=${1:-"dev"}
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$DISCORD_WEBHOOK_URL" ] && return

    PAYLOAD=$(jq -n \
      --arg ts "$timestamp" \
      --arg env "$env" \
      --argjson color "$COLOR_RED" \
      '{
        username: "Souzip Bot",
        content: "@here 롤백 실패",
        embeds: [{
          title: ("롤백 실패 [" + $env + "]"),
          description: "롤백 후에도 서버가 작동하지 않습니다.",
          color: $color,
          fields: [
            {name: "환경", value: $env, inline: true},
            {name: "시간", value: $ts, inline: true},
            {name: "상태", value: "수동 복구 필요", inline: false}
          ]
        }]
      }')

    curl -s -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$DISCORD_WEBHOOK_URL" > /dev/null
}

notify_server_down() {
    local env=${1:-"dev"}
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$DISCORD_WEBHOOK_URL" ] && return

    PAYLOAD=$(jq -n \
      --arg ts "$timestamp" \
      --arg env "$env" \
      --argjson color "$COLOR_RED" \
      '{
        username: "Souzip Bot",
        content: "@here 서버 다운",
        embeds: [{
          title: ("서버 다운 [" + $env + "]"),
          description: "서버가 응답하지 않습니다.",
          color: $color,
          fields: [
            {name: "환경", value: $env, inline: true},
            {name: "시간", value: $ts, inline: true},
            {name: "상태", value: "헬스체크 실패", inline: false}
          ]
        }]
      }')

    curl -s -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$DISCORD_WEBHOOK_URL" > /dev/null
}

notify_server_up() {
    local env=${1:-"dev"}
    local downtime=$2
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$DISCORD_WEBHOOK_URL" ] && return

    PAYLOAD=$(jq -n \
      --arg ts "$timestamp" \
      --arg dt "$downtime" \
      --arg env "$env" \
      --argjson color "$COLOR_GREEN" \
      '{
        username: "Souzip Bot",
        embeds: [{
          title: ("서버 복구 [" + $env + "]"),
          description: "서버가 복구되었습니다.",
          color: $color,
          fields: [
            {name: "환경", value: $env, inline: true},
            {name: "복구 시간", value: $ts, inline: true},
            {name: "다운타임", value: $dt, inline: false}
          ]
        }]
      }')

    curl -s -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$DISCORD_WEBHOOK_URL" > /dev/null
}

notify_disk_warning() {
    local env=${1:-"dev"}
    local disk_usage=$2
    local disk_percent=$3
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$DISCORD_WEBHOOK_URL" ] && return

    PAYLOAD=$(jq -n \
      --arg env "$env" \
      --arg du "$disk_usage" \
      --arg dp "$disk_percent" \
      --arg ts "$timestamp" \
      --argjson color "$COLOR_YELLOW" \
      '{
        username: "Souzip Bot",
        content: "@here 디스크 공간 부족",
        embeds: [{
          title: ("디스크 사용량 경고 [" + $env + "]"),
          description: "디스크 사용량이 임계값을 초과했습니다.",
          color: $color,
          fields: [
            {name: "환경", value: $env, inline: true},
            {name: "시간", value: $ts, inline: true},
            {name: "디스크 사용량", value: "\($du) (\($dp)%)", inline: false}
          ]
        }]
      }')

    curl -s -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$DISCORD_WEBHOOK_URL" > /dev/null
}

notify_memory_warning() {
    local env=${1:-"dev"}
    local memory_usage=$2
    local memory_percent=$3
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$DISCORD_WEBHOOK_URL" ] && return

    PAYLOAD=$(jq -n \
      --arg env "$env" \
      --arg mu "$memory_usage" \
      --arg mp "$memory_percent" \
      --arg ts "$timestamp" \
      --argjson color "$COLOR_RED" \
      '{
        username: "Souzip Bot",
        content: "@here 메모리 부족",
        embeds: [{
          title: ("메모리 사용량 경고 [" + $env + "]"),
          description: "메모리 사용량이 임계값을 초과했습니다.",
          color: $color,
          fields: [
            {name: "환경", value: $env, inline: true},
            {name: "시간", value: $ts, inline: true},
            {name: "메모리 사용량", value: "\($mu) (\($mp)%)", inline: false}
          ]
        }]
      }')

    curl -s -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$DISCORD_WEBHOOK_URL" > /dev/null
}

notify_container_stopped() {
    local env=${1:-"dev"}
    local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    [ -z "$DISCORD_WEBHOOK_URL" ] && return

    PAYLOAD=$(jq -n \
      --arg env "$env" \
      --arg ts "$timestamp" \
      --argjson color "$COLOR_RED" \
      '{
        username: "Souzip Bot",
        content: "@here 컨테이너 중지",
        embeds: [{
          title: ("컨테이너 중지 [" + $env + "]"),
          description: "컨테이너가 중지되었습니다.",
          color: $color,
          fields: [
            {name: "환경", value: $env, inline: true},
            {name: "시간", value: $ts, inline: true}
          ]
        }]
      }')

    curl -s -H "Content-Type: application/json" -X POST -d "$PAYLOAD" "$DISCORD_WEBHOOK_URL" > /dev/null
}