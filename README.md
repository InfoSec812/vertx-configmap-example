# Vert.x Kubernetes/OpenShift ConfigMap Example

## Overview
The [Vert.x Config](http://vertx.io/docs/vertx-config/java/) module
allows an application to pull it's configuration from a number of 
different source, either in isolation or in combination. One of the 
options as a source for configuration information is a 
Kubernetes/OpenShift ConfigMap. This project demonstrates using
that option and shows a quick-start example using the fabric8-maven
plugin to launch the example on a local Kubernetes/OpenShift cluster.

## Prerequisistes
* Maven >= 3.3.9
* Java 8 >= u131
* One Of:
  * minikube
  * minishift
  * Red Hat Container Development Kit

## Trying It Out

### With Minishift
```bash
minishift start
minishift login -u developer -p developer
mvn clean package vertx:package fabric8:deploy
```