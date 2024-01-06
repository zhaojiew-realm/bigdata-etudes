package org.example.kafka;


import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.aws.config.AWSConfigConstants;
import org.apache.flink.connector.firehose.sink.KinesisFirehoseSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

    public class KDFSinkDemo {
        public static void main(String[] args) {
            StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();

            // kdf sink
            Properties sinkProperties = new Properties();
            sinkProperties.put(AWSConfigConstants.AWS_REGION, "cn-north-1");

            KinesisFirehoseSink<String> kdfSink = KinesisFirehoseSink.<String>builder()
                    .setFirehoseClientProperties(sinkProperties)      // Required
                    .setSerializationSchema(new SimpleStringSchema()) // Required
                    .setDeliveryStreamName("PUT-S3-S9qP9")        // Required
//                        .setFailOnError(false)                            // Optional
//                        .setMaxBatchSize(500)                             // Optional
//                        .setMaxInFlightRequests(50)                       // Optional
//                        .setMaxBufferedRequests(10_000)                   // Optional
//                        .setMaxBatchSizeInBytes(4 * 1024 * 1024)          // Optional
//                        .setMaxTimeInBufferMS(5000)                       // Optional
//                        .setMaxRecordSizeInBytes(1000 * 1024)             // Optional
                    .build();
            String broker = "172.31.3.41:9092";
            KafkaSource<String> kds = KafkaSource.<String>builder()
                    .setBootstrapServers(broker)
                    .setTopics("test")
                    .setGroupId("windows")
                    .setStartingOffsets(OffsetsInitializer.earliest())
                    .setValueOnlyDeserializer(new SimpleStringSchema())
                    .build();


            DataStreamSource<String> fd = senv.fromSource(kds, WatermarkStrategy.noWatermarks(), "Kafka Source");
            fd.sinkTo(kdfSink);
            try {
                senv.execute();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

