#!/bin/bash

ENV_FILE=".env"
COMPOSE="docker-compose --env-file $ENV_FILE"

case "$1" in
  up)
    echo "🚀 Starting services..."
    $COMPOSE up -d
    ;;
  build)
    echo "🔧 Building and starting services..."
    $COMPOSE up -d --build
    ;;
  down)
    echo "🛑 Stopping services..."
    $COMPOSE down
    ;;
  down-clean)
    echo "🧹 Stopping and removing containers and volumes..."
    $COMPOSE down -v
    ;;
  logs)
    echo "📜 Showing logs..."
    $COMPOSE logs -f
    ;;
  ps)
    echo "📦 Showing running containers..."
    docker ps
    ;;
  restart)
    echo "♻️ Restarting services..."
    $0 down
    $0 up
    ;;
  *)
    echo "Usage: $0 {up|build|down|down-clean|logs|ps|restart}"
    exit 1
    ;;
esac
