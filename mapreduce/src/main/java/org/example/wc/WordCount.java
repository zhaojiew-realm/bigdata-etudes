package org.example.wc;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount {

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {

        //创建配置获取job对象
        //Configuration类代表作业的配置，该类会加载mapred-site.xml、hdfs-site.xml、core-site.xml等配置文件
        Configuration conf = new Configuration();
//        conf.set("mapreduce.framework.name", "yarn");

        Job job = Job.getInstance(conf,"wordcount");

        //关联Driver程序
        job.setJarByClass(WordCount.class);

        //关联mapper和reducer程序
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReduce.class);

        //设置mapper输出格式
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        //设置reducer输出格式
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        //本地路径
         FileInputFormat.setInputPaths(job, new Path("data/input"));
         FileOutputFormat.setOutputPath(job, new Path("data/output"));
        //打包jar集群路径
        // FileInputFormat.setInputPaths(job, new Path(args[0]));
        // FileOutputFormat.setOutputPath(job, new Path(args[1]));

//        FileInputFormat.setInputPaths(job, new Path("/wcinput/word.txt"));
//        FileOutputFormat.setOutputPath(job, new Path("/wcoutput12/"));

        //提交job
        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);
    }
}
