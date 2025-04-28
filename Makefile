# Makefile for managing DeepTechHub microservices with Docker Compose

ENV_FILE = .env
COMPOSE = docker-compose --env-file $(ENV_FILE)

up:
	$(COMPOSE) up -d

build:
	$(COMPOSE) up -d --build

down:
	$(COMPOSE) down

down-clean:
	$(COMPOSE) down -v

logs:
	$(COMPOSE) logs -f

ps:
	docker ps

restart:
	make down
	make up
