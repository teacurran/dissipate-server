THIS_FILE := $(lastword $(MAKEFILE_LIST))
PROJECT_NAME = $(notdir $(PWD))
CMD_ARGUMENTS ?= $(cmd)
.PHONY: clean build

package:
	cd api && mvn package -Dquarkus.temporal.service.url=temporal:7233

build:
	docker-compose down --remove-orphans
	COMPOSE_HTTP_TIMEOUT=2000 docker-compose build

start:
	docker-compose up -d postgresql temporal-db temporal temporal-admin-tools temporal-ui firebase-emulator
	docker-compose run --service-ports api

stop:
	docker-compose down

test:
	@make test-api

flyway:
	cd api && mvn flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5432/dissipate -Dflyway.user=tea -Dflyway.password=

test:
	docker-compose up --exit-code-from integration-tests postgresql temporal-db temporal firebase-emulator integration-tests

rebuild:
	docker-compose build --no-cache

rebuild-test:
	docker-compose -f docker-compose.yml -f docker-compose.test.yml build api-integration-test

clean:
	@docker-compose down --remove-orphans --rmi all 2>/dev/null \
	&& echo 'Image(s) removed.' \
	|| echo 'Image(s) already removed.'

prune:
	docker system prune -af
