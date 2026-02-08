#!/bin/bash

WORK_DIR="/home/ubuntu/souzip"
HEALTH_CHECK_URL="http://localhost:8080/actuator/health"
STATUS_FILE="$WORK_DIR/.system-status"

DISK_THRESHOLD=80
MEMORY_THRESHOLD=90
MAX_HEALTH_FAILURES=3

if [ -f "$WORK_DIR/deploy/.env" ]; then
    export $(grep DISCORD_WEBHOOK_URL "$WORK_DIR/deploy/.env" | xargs)
fi

if [ -f "$WORK_DIR/deploy/notification/discord-notify.sh" ]; then
    source "$WORK_DIR/deploy/notification/discord-notify.sh"
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

if curl -f -s --max-time 5 $HEALTH_CHECK_URL > /dev/null 2>&1; then
    if [ "$HEALTH_STATUS" = "down" ]; then
        if [ ! -z "$HEALTH_DOWN_SINCE" ]; then
            DOWN_TIMESTAMP=$(date -d "$HEALTH_DOWN_SINCE" +%s)
            UP_TIMESTAMP=$(date +%s)
            DOWN_DURATION=$(( UP_TIMESTAMP - DOWN_TIMESTAMP ))
            DOWNTIME_MIN=$(( DOWN_DURATION / 60 ))

            if [ ! -z "$DISCORD_WEBHOOK_URL" ]; then
                notify_server_up "${DOWNTIME_MIN}분"
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
            notify_server_down
        fi
    fi
fi

DISK_USAGE=$(df -h / | awk 'NR==2 {print $3 " / " $2}')
DISK_PERCENT=$(df / | awk 'NR==2 {print int($5)}')

if [ $DISK_PERCENT -ge $DISK_THRESHOLD ]; then
    if [ "$DISK_WARNING_SENT" = "false" ]; then
        if [ ! -z "$DISCORD_WEBHOOK_URL" ]; then
            notify_disk_warning "$DISK_USAGE" "$DISK_PERCENT"
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
            notify_memory_warning "$MEMORY_USAGE" "$MEMORY_PERCENT"
        fi
        MEMORY_WARNING_SENT=true
    fi
else
    MEMORY_WARNING_SENT=false
fi

CONTAINER_RUNNING=$(docker ps --filter "name=souzip-api" --filter "status=running" -q)

if [ -z "$CONTAINER_RUNNING" ]; then
    if [ "$CONTAINER_WARNING_SENT" = "false" ]; then
        if [ ! -z "$DISCORD_WEBHOOK_URL" ]; then
            notify_container_stopped
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
