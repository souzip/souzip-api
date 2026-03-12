#!/bin/bash
set -e

ENV=${1:-dev}

if [ "$ENV" = "prod" ]; then
    WORK_DIR="/home/souzip-prod/souzip"
else
    WORK_DIR="/home/souzip-dev/souzip"
fi
DEPLOY_DIR="$WORK_DIR/deploy/$ENV"
STATUS_FILE="$DEPLOY_DIR/.system-status"
DISK_THRESHOLD=80
MEMORY_THRESHOLD=90
MAX_HEALTH_FAILURES=3

if [ -f "$DEPLOY_DIR/.env" ]; then
    export DISCORD_WEBHOOK_URL=$(grep '^DISCORD_WEBHOOK_URL=' "$DEPLOY_DIR/.env" | cut -d= -f2-)
fi

if [ -f "$WORK_DIR/deploy/shared/discord-notify.sh" ]; then
    source "$WORK_DIR/deploy/shared/discord-notify.sh"
fi

if [ -f "$STATUS_FILE" ]; then
    source "$STATUS_FILE"
else
    HEALTH_STATUS="up"
    HEALTH_FAILURE_COUNT=0
    HEALTH_DOWN_SINCE=""
    DISK_WARNING_SENT=false
    MEMORY_WARNING_SENT=false
    CONTAINER_WARNING_SENT=false
fi

if [ "$ENV" = "dev" ]; then
    HEALTH_CHECK_URL="http://localhost:8080/actuator/health"
    CONTAINER_NAME="souzip-api"
else
    CONTAINER_NAME="souzip-api-${CONTAINER_COLOR:-blue}"
    case "$CONTAINER_COLOR" in
        blue)
            HEALTH_CHECK_URL="http://localhost:8081/actuator/health"
            ;;
        green)
            HEALTH_CHECK_URL="http://localhost:8082/actuator/health"
            ;;
        *)
            HEALTH_CHECK_URL="http://localhost:8081/actuator/health"
            ;;
    esac
fi

if curl -f -s --max-time 5 $HEALTH_CHECK_URL > /dev/null 2>&1; then
    if [ "$HEALTH_STATUS" = "down" ]; then
        if [ ! -z "$HEALTH_DOWN_SINCE" ]; then
            DOWN_TIMESTAMP=$(date -d "$HEALTH_DOWN_SINCE" +%s)
            UP_TIMESTAMP=$(date +%s)
            DOWN_DURATION=$(( UP_TIMESTAMP - DOWN_TIMESTAMP ))
            DOWNTIME_MIN=$(( DOWN_DURATION / 60 ))
            if [ ! -z "$DISCORD_WEBHOOK_URL" ]; then
                notify_server_up "${DOWNTIME_MIN}분" "$ENV"
            fi
        fi
        HEALTH_STATUS="up"
        HEALTH_FAILURE_COUNT=0
        HEALTH_DOWN_SINCE=""
    else
        HEALTH_FAILURE_COUNT=0
    fi
else
    HEALTH_FAILURE_COUNT=$((HEALTH_FAILURE_COUNT + 1))
    if [ $HEALTH_FAILURE_COUNT -ge $MAX_HEALTH_FAILURES ] && [ "$HEALTH_STATUS" = "up" ]; then
        HEALTH_STATUS="down"
        HEALTH_DOWN_SINCE=$(date '+%Y-%m-%d %H:%M:%S')
        if [ ! -z "$DISCORD_WEBHOOK_URL" ]; then
            notify_server_down "$ENV"
        fi
    fi
fi

DISK_USAGE=$(df -h / | awk 'NR==2 {print $3 " / " $2}')
DISK_PERCENT=$(df / | awk 'NR==2 {print int($5)}')
if [ $DISK_PERCENT -ge $DISK_THRESHOLD ]; then
    if [ "$DISK_WARNING_SENT" = "false" ]; then
        if [ ! -z "$DISCORD_WEBHOOK_URL" ]; then
            notify_disk_warning "$DISK_USAGE" "$DISK_PERCENT" "$ENV"
        fi
        DISK_WARNING_SENT=true
    fi
else
    DISK_WARNING_SENT=false
fi

MEMORY_USAGE=$(free -h | awk 'NR==2 {print $3 " / " $2}')
MEMORY_PERCENT=$(free | awk 'NR==2 {print int($3/$2 * 100)}')
if [ $MEMORY_PERCENT -ge $MEMORY_THRESHOLD ]; then
    if [ "$MEMORY_WARNING_SENT" = "false" ]; then
        if [ ! -z "$DISCORD_WEBHOOK_URL" ]; then
            notify_memory_warning "$MEMORY_USAGE" "$MEMORY_PERCENT" "$ENV"
        fi
        MEMORY_WARNING_SENT=true
    fi
else
    MEMORY_WARNING_SENT=false
fi

CONTAINER_RUNNING=$(docker ps --filter "name=$CONTAINER_NAME" --filter "status=running" -q)
if [ -z "$CONTAINER_RUNNING" ]; then
    if [ "$CONTAINER_WARNING_SENT" = "false" ]; then
        if [ ! -z "$DISCORD_WEBHOOK_URL" ]; then
            notify_container_stopped "$ENV"
        fi
        CONTAINER_WARNING_SENT=true
    fi
else
    CONTAINER_WARNING_SENT=false
fi

cat > "$STATUS_FILE" << EOF
HEALTH_STATUS="$HEALTH_STATUS"
HEALTH_FAILURE_COUNT=$HEALTH_FAILURE_COUNT
HEALTH_DOWN_SINCE="$HEALTH_DOWN_SINCE"
DISK_WARNING_SENT=$DISK_WARNING_SENT
MEMORY_WARNING_SENT=$MEMORY_WARNING_SENT
CONTAINER_WARNING_SENT=$CONTAINER_WARNING_SENT
EOF