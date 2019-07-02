.PHONY: build
build:
	docker build -f src/main/docker/Dockerfile.native -t quarkus-jms/hello-quarkus-jms-receiver .

.PHONY: run
run: build
	docker run --rm -p 8080:8080 quarkus-jms/hello-quarkus-jms-receiver

.PHONY: push
push:
