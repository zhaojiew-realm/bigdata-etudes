

```yaml
version: '3.0'
services:
  dk-zookeeper:
    image: zookeeper:3.7.0
    ports:
      - 2182:2181
    environment:
      - ALLOW_ANONYMOUS_LOGIN=yes
  dk-kafka-01:
    image: wurstmeister/kafka
    ports:
      - 9092:9092
    environment:
      KAFKA_BROKER_ID: 0
      KAFKA_CFG_LISTENERS: PLAINTEXT://:9092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://127.0.0.1:9092
      # KAFKA_CREATE_TOPICS: "test:1:0" //创建test的topic，具备1分区0副本
      KAFKA_ZOOKEEPER_CONNECT: dk-zookeeper:2181
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
      ALLOW_PLAINTEXT_LISTENER: yes
    depends_on:
      - dk-zookeeper
```