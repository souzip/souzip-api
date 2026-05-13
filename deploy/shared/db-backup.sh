#!/bin/bash
DATE=$(date +%Y-%m-%d_%H%M)
BACKUP_DIR="/home/souzip-prod/backups"
BACKUP_FILE="$BACKUP_DIR/souzip-$DATE.dump"
BUCKET="souzip-db-backup"
RETENTION_DAYS=30

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

# 로컬 오래된 백업 삭제
find $BACKUP_DIR -name "souzip-*.dump" -mtime +$RETENTION_DAYS -delete

# Object Storage 오래된 백업 삭제
s3cmd $S3CMD_OPTS ls s3://$BUCKET/ | awk '{print $4}' | while read file; do
    file_date=$(echo $file | grep -oP '\d{4}-\d{2}-\d{2}')
    if [ ! -z "$file_date" ]; then
        days_old=$(( ( $(date +%s) - $(date -d "$file_date" +%s) ) / 86400 ))
        if [ $days_old -gt $RETENTION_DAYS ]; then
            s3cmd $S3CMD_OPTS del $file
            echo "[INFO] 오래된 백업 삭제 - $file"
        fi
    fi
done

echo "[INFO] DB 백업 프로세스 완료 - $DATE"

curl -s -H "Content-Type: application/json" \
  -X POST \
  -d "{\"username\": \"Souzip Bot\", \"embeds\": [{\"title\": \"DB 백업 완료\", \"color\": 3066993, \"fields\": [{\"name\": \"날짜\", \"value\": \"$DATE\", \"inline\": true}, {\"name\": \"파일\", \"value\": \"souzip-$DATE.dump\", \"inline\": true}]}]}" \
  "$PROD_DB_DISCORD_WEBHOOK_URL" > /dev/null