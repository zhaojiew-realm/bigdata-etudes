package org.example.streaming

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.util.Random


object Kafka {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("streaming_wordcount")
    val scc = new StreamingContext(sparkConf,Seconds(3))

    // use docker-compose.yaml launch single node kafka
    val kafkaPara = Map[String,Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "localhost:9092",
      ConsumerConfig.GROUP_ID_CONFIG -> "test",
      "key.deserializer" ->  "org.apache.kafka.common.serialization.StringDeserializer",
      "value.deserializer" ->  "org.apache.kafka.common.serialization.StringDeserializer",
    )

    val kafkaDS: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      scc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String,String](Set("test"), kafkaPara)
    )

    // send message from kafka producer
    // # kafka-console-producer.sh --topic test --broker-list localhost:9092
    kafkaDS.map(_.value()).print()

    // start reciever
    scc.start()
    scc.awaitTermination()

//    scc.stop()

  }

}
