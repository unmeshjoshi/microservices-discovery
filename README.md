# Service Discovery Samples

Prototype code for implementing service discovery with Zookeeper, etcd and Consul
 
### Prerequisites

Install docker and docker-compose.

### Running Zookeeper

There is a docker compose setup with 3 node zookeeper cluster

* Run
```
docker-compose -f locationservice/src/test/resources/docker-compose-zookeeper.yml up
```
* Execute 
  microservices-discovery/locationservice/src/test/scala/com/servicediscovery/impl/zookeeper/ZookeeperLocationServiceTest.scala

### Running Etcd

There is a docker compose setup with 3 node etcd cluster

* Run
```
docker-compose -f locationservice/src/test/resources/docker-compose-etcd.yml up
```
* Execute 
 /microservices-discovery/locationservice/src/test/scala/com/servicediscovery/impl/etcd/EtcdLocationServiceTest.scala

### Running Consul

There is a docker compose setup with 3 node consul cluster

* Run
```
docker-compose -f locationservice/src/test/resources/docker-compose-consul.yml up
```
* Execute 
 microservices-discovery/locationservice/src/main/scala/com/servicediscovery/impl/consul/ConsulRegistration.scala