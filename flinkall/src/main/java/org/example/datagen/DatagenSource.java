package org.example.datagen;

import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.datagen.DataGeneratorSource;

// https://blog.csdn.net/ifenggege/article/details/115104560
public class DatagenSource {
    public static void main(String[] args) {

        StreamExecutionEnvironment senv = StreamExecutionEnvironment.getExecutionEnvironment();
        // 创建DataGeneratorSource。传入上面自定义的数据生成器
        DataGeneratorSource<TrafficData> trafficDataDataGeneratorSource = new DataGeneratorSource<>(new TrafficData.TrafficDataGenerator());

        senv.addSource(trafficDataDataGeneratorSource)
                .returns(new TypeHint<TrafficData>() {})
                .print();
        try {
            senv.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
