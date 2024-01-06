package org.example.kafka;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class KafkaStreamDemo {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();
        String broker = "172.31.3.41:9092";
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(broker)
                .setTopics("test")
                .setGroupId("ubuntu")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStreamSource<String> fd = senv.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");
        fd.print();
        senv.execute();
    }
}

