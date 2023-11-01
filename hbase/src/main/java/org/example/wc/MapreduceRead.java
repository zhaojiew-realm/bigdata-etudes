package org.example.wc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

public class MapreduceRead {

    //http://www.hainiubl.com/topics/76168

    public static class HMapper extends TableMapper<Text, IntWritable>{
        Text k = new Text();
        IntWritable v = new IntWritable();
        @Override
        protected void map(ImmutableBytesWritable key, Result value, Mapper<ImmutableBytesWritable, Result, Text, IntWritable>.Context context) throws IOException, InterruptedException {
            byte[] classBytes = value.getValue(Bytes.toBytes("info"), Bytes.toBytes("class"));
            byte[] scoreBytes = value.getValue(Bytes.toBytes("info"), Bytes.toBytes("score"));
            k.set(Bytes.toString(classBytes));
            String score = Bytes.toString(scoreBytes);
            v.set(Integer.valueOf(score));
            System.out.println(k);
            System.out.println(v);
            context.write(k,v);
        }
    }

    public static class HReducer extends Reducer<Text,IntWritable,Text, DoubleWritable> {
        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Reducer<Text, IntWritable, Text, DoubleWritable>.Context context) throws IOException, InterruptedException {
            int sum = 0;
            int count = 0;
            for (IntWritable value : values) {
                sum += value.get();
                count ++;
            }
            double avg = sum * 1.0 / count;
            context.write(key,new DoubleWritable(avg));
        }
    }

    public static void main(String[] args) throws Exception{
        Configuration conf = HBaseConfiguration.create();
//        conf.set("HADOOP_HOME","/hadoop");
//        conf.set("HBASE_HOME","/hbase");
        Job job = Job.getInstance(conf);
        job.setJarByClass(MapreduceRead.class);

        TableMapReduceUtil.initTableMapperJob(
                "score",new Scan(),HMapper.class,Text.class,IntWritable.class,job
        );

        job.setReducerClass(HReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);
        TextOutputFormat.setOutputPath(job,new Path("res"));

        job.waitForCompletion(true);
    }
}
