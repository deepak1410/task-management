#!/bin/sh

# Load secrets from mounted files (if any)
export DB_USER=$(cat /run/secrets/db_user 2>/dev/null || echo $DB_USER)
export DB_PASSWORD=$(cat /run/secrets/db_password 2>/dev/null || echo $DB_PASSWORD)
export DTH_JWT_SECRET=$(cat /run/secrets/jwt_secret 2>/dev/null || echo DTH_JWT_SECRET)
export DTH_GMAIL_PWD=$(cat /run/secrets/gmail_pass 2>/dev/null || echo DTH_JWT_SECRET)

# Execute the passed command (Spring Boot JAR)
exec java -jar app.jar
