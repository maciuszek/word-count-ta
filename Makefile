REGISTRY = com.maciuszek
APP_NAME = wordcount
TAG = 0.0.2-SNAPSHOT

default: help
.PHONY: jar image help

jar: ## Build the jar
	@echo "Building Jar"
	./mvnw -U clean package

image: jar ## Build the image
	@echo "Building Docker image..."
	docker build \
		-t ${REGISTRY}/${APP_NAME}:${TAG} \
		.

help: ## Display available make targets
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)
