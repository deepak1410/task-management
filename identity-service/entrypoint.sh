#!/bin/sh

# Read secrets from mounted files
export DB_PASSWORD=$(cat /run/secrets/db_password)
export DTH_JWT_SECRET=$(cat /run/secrets/jwt_secret)
export DTH_GMAIL_PWD=$(cat /run/secrets/gmail_pass)

# Start the application
exec java -jar app.jar
