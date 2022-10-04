THIS_FILE := $(lastword $(MAKEFILE_LIST))
PROJECT_NAME = $(notdir $(PWD))
CMD_ARGUMENTS ?= $(cmd)
.PHONY: clean build

build:
	docker-compose down --remove-orphans
	docker-compose build

start:
	docker-compose up -d postgresql temporaldb temporal temporal-admin-tools temporal-ui temporal-web firebase-emulator
	docker-compose run api

stop:
	docker-compose down

test:
	@make test-api

test-api:
	docker-compose -f docker-compose.yml -f docker-compose.test.yml up --exit-code-from api-integration-test api-integration-test

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