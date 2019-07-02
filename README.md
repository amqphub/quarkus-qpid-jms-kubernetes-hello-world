# Getting started with JMS on Kubernetes usiong Quarkus 

This guide shows you how to send and receive messages using [Apache
Qpid JMS](http://qpid.apache.org/components/jms/index.html) and
[ActiveMQ Artemis](https://activemq.apache.org/artemis/index.html) on
[Kubernetes](https://kubernetes.io/) using a Quarkus Qpid JMS extension. 
It uses the [AMQP 1.0](http://www.amqp.org/) message protocol to send 
and receive messages.

## Overview

The example application has three parts:

* An AMQP 1.0 message broker, ActiveMQ Artemis

* A sender service exposing an HTTP endpoint that converts HTTP
  requests into AMQP 1.0 messages.  It sends the messages to a queue
  called `example/strings` on the broker.

* A receiver service that consumes AMQP messages from
  `example/strings`.  It exposes another HTTP endpoint that returns
  the messages as HTTP responses.

The sender and the receiver use the JMS API to perform messaging
operations. 

## Prerequisites 

* For native builds, [GraalVM](https://www.graalvm.org/) version [19.0.2](https://github.com/oracle/graal/releases/tag/vm-19.0.2)+ [installed](https://www.graalvm.org/docs/getting-started), with `GRAALVM_HOME` set and [native-image extension](https://www.graalvm.org/docs/reference-manual/aot-compilation/).

## Steps

1. [Install and run Minikube in your
   environment](https://kubernetes.io/docs/setup/minikube/)

1. Configure your shell to use the Minikube Docker instance:

   ```bash
   $ eval $(minikube docker-env)
   $ echo $DOCKER_HOST
   tcp://192.168.39.67:2376
   ```

1. Create a broker deployment and expose it as a service:

   ```bash
   $ kubectl run broker --image docker.io/ssorj/activemq-artemis
   deployment.apps/broker created
   $ kubectl expose deployment/broker --port 5672
   service/broker exposed
   ```

1. Change directory to the sender application, build it, create a
   deployment, and expose it as a service:

   ```bash
   $ cd sender/
   $ mvn package -Pnative
   [Maven output]
   $ docker build -f src/main/docker/Dockerfile.native -t quarkus-jms/hello-quarkus-jms-sender .
   [Docker output]
   $ kubectl run sender --image quarkus-jms/hello-quarkus-jms-sender --image-pull-policy Never --env MESSAGING_SERVICE_HOST=broker
   deployment.apps/sender created
   $ kubectl expose deployment/sender --port 8080 --type NodePort
   service/sender exposed
   ```

To build and run in native build mode:

1. Change directory to the receiver application, build it, create a
   deployment, and expose it as a service:

   ```bash
   $ cd ../receiver/
   $ mvn package -P native
   [Maven output]
   $ docker build -f src/main/docker/Dockerfile.native -t quarkus-jms/hello-quarkus-jms-receiver .
   [Docker output]
   $ kubectl run receiver --image quarkus-jms/hello-quarkus-jms-receiver --image-pull-policy Never --env MESSAGING_SERVICE_HOST=broker
   deployment.apps/receiver created
   $ kubectl expose deployment/receiver --port 8080 --type NodePort
   service/receiver exposed
   ```

1. Check that the deployments and pods are present.  You should see
   deployments and services for `broker`, `sender`, and `receiver`.

   ```bash
   $ kubectl get deployment
   $ kubectl get service
   ```

1. Save the `NodePort` URLs in local variables:

   ```bash
   $ sender_url=$(minikube service sender --url)
   $ receiver_url=$(minikube service receiver --url)
   ```

1. Use `curl` to test the readiness of the send and receive services:

   ```bash
   $ curl $sender_url/api/ready
   OK
   $ curl $receiver_url/api/ready
   OK
   ```

1. Use `curl` to send strings to the sender service:

   ```bash
   $ curl -X POST -H "content-type: text/plain" -d hello1 $sender_url/api/send
   OK
   $ curl -X POST -H "content-type: text/plain" -d hello2 $sender_url/api/send
   OK
   $ curl -X POST -H "content-type: text/plain" -d hello3 $sender_url/api/send
   OK
   ```

1. Use `curl` to receive the sent strings back from the receiver
   service:

   ```bash
   $ curl -X POST $receiver_url/api/receive
   hello1
   $ curl -X POST $receiver_url/api/receive
   hello2
   $ curl -X POST $receiver_url/api/receive
   hello3
   ```

## More resources

* [Quarkus Getting Started](https://quarkus.io/get-started/)
* [Getting started with Minikube](https://kubernetes.io/docs/tutorials/hello-minikube/)
* [Apache ActiveMQ Artemis](https://activemq.apache.org/artemis/)
* [Artemis container image](https://cloud.docker.com/u/ssorj/repository/docker/ssorj/activemq-artemis)
* [Apache Qpid JMS](http://qpid.apache.org/components/jms/index.html)
* [What's New in JMS 2.0 by Nigel Deakin](https://www.oracle.com/technetwork/articles/java/jms20-1947669.html)
* [AMQP](https://www.amqp.org/)
