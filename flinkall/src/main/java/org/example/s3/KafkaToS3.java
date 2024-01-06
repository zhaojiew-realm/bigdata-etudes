package org.example.s3;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy;

import java.time.Duration;

public class KafkaToS3 {
    public static void main(String[] args) {
        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();
        String broker = "172.31.3.41:9092";
        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers(broker)
                .setTopics("test")
                .setGroupId("tos3")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();
        String outputPath = "s3://zhaojiew-tmp/flinkfors3/";
        DataStreamSource<String> fd = senv.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");
        FileSink<String> filesink = FileSink.forRowFormat(new Path(outputPath), new SimpleStringEncoder<String>("UTF-8"))
                .withRollingPolicy(DefaultRollingPolicy.builder()
                                .withRolloverInterval(Duration.ofMinutes(3))
                                .withInactivityInterval(Duration.ofMinutes(1))
                                .withMaxPartSize(MemorySize.ofMebiBytes(5))
                                .build())
                .build();
        fd.sinkTo(filesink);
        try {
            senv.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
