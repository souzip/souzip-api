#!/bin/bash
DATE=$(date +%Y-%m-%d_%H%M)
BACKUP_DIR="/home/souzip-prod/backups"
BACKUP_FILE="$BACKUP_DIR/souzip-$DATE.dump"
BUCKET="souzip-db-backup"
RETENTION_COUNT=7

if [ -f "/home/souzip-prod/souzip/deploy/prod/.env" ]; then
    export PROD_POSTGRES_USER=$(grep '^PROD_POSTGRES_USER=' /home/souzip-prod/souzip/deploy/prod/.env | cut -d= -f2-)
    export PROD_POSTGRES_PASSWORD=$(grep '^PROD_POSTGRES_PASSWORD=' /home/souzip-prod/souzip/deploy/prod/.env | cut -d= -f2-)
    export PROD_DB_DISCORD_WEBHOOK_URL=$(grep '^PROD_DB_DISCORD_WEBHOOK_URL=' /home/souzip-prod/souzip/deploy/prod/.env | cut -d= -f2-)
    export NCP_ACCESS_KEY=$(grep '^NCP_ACCESS_KEY=' /home/souzip-prod/souzip/deploy/prod/.env | cut -d= -f2-)
    export NCP_SECRET_KEY=$(grep '^NCP_SECRET_KEY=' /home/souzip-prod/souzip/deploy/prod/.env | cut -d= -f2-)
fi

S3CMD_OPTS="--access_key=$NCP_ACCESS_KEY --secret_key=$NCP_SECRET_KEY --host=kr.object.ncloudstorage.com --host-bucket=%(bucket)s.kr.object.ncloudstorage.com"

mkdir -p $BACKUP_DIR

docker exec -e PGPASSWORD=$PROD_POSTGRES_PASSWORD souzip-prod-db \
  pg_dump -U $PROD_POSTGRES_USER -d souzip_prod -F c > $BACKUP_FILE

if [ $? -ne 0 ]; then
  echo "[ERROR] DB 백업 실패 - $DATE"
  curl -s -H "Content-Type: application/json" \
    -X POST \
    -d "{\"username\": \"Souzip Bot\", \"content\": \"@here DB 백업 실패 - $DATE\"}" \
    "$PROD_DB_DISCORD_WEBHOOK_URL" > /dev/null
  exit 1
fi
echo "[INFO] DB 백업 완료 - $BACKUP_FILE"

s3cmd $S3CMD_OPTS put $BACKUP_FILE s3://$BUCKET/souzip-$DATE.dump

if [ $? -ne 0 ]; then
  echo "[ERROR] Object Storage 업로드 실패 - $DATE"
  curl -s -H "Content-Type: application/json" \
    -X POST \
    -d "{\"username\": \"Souzip Bot\", \"content\": \"@here DB 백업 업로드 실패 - $DATE\"}" \
    "$PROD_DB_DISCORD_WEBHOOK_URL" > /dev/null
  exit 1
fi
echo "[INFO] Object Storage 업로드 완료"

# 로컬 백업은 최신순으로 RETENTION_COUNT 개만 보관하고 나머지 삭제
ls -1t $BACKUP_DIR/souzip-*.dump 2>/dev/null | tail -n +$((RETENTION_COUNT + 1)) | while read -r file; do
    rm -f "$file"
    echo "[INFO] 오래된 로컬 백업 삭제 - $file"
done

# Object Storage 백업도 최신순으로 RETENTION_COUNT 개만 보관 (파일명이 날짜순이라 정렬=시간순)
remote_files=$(s3cmd $S3CMD_OPTS ls s3://$BUCKET/ | awk '{print $4}' | grep 'souzip-.*\.dump$' | sort)
remote_total=$(printf '%s\n' "$remote_files" | grep -c .)
remote_delete_count=$((remote_total - RETENTION_COUNT))
if [ "$remote_delete_count" -gt 0 ]; then
    printf '%s\n' "$remote_files" | head -n "$remote_delete_count" | while read -r file; do
        s3cmd $S3CMD_OPTS del "$file"
        echo "[INFO] 오래된 백업 삭제 - $file"
    done
fi

echo "[INFO] DB 백업 프로세스 완료 - $DATE"

curl -s -H "Content-Type: application/json" \
  -X POST \
  -d "{\"username\": \"Souzip Bot\", \"embeds\": [{\"title\": \"DB 백업 완료\", \"color\": 3066993, \"fields\": [{\"name\": \"날짜\", \"value\": \"$DATE\", \"inline\": true}, {\"name\": \"파일\", \"value\": \"souzip-$DATE.dump\", \"inline\": true}]}]}" \
  "$PROD_DB_DISCORD_WEBHOOK_URL" > /dev/null