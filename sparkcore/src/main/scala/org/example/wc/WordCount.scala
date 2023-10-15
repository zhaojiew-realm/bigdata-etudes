package org.example.wc

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
//      .setMaster("spark://spark-master:7077")
      .setMaster("local[*]")
      .setAppName("WordCount")

    val sc: SparkContext = new SparkContext(sparkConf)
    // 指定hdfs路径使用8020端口（IPC通信端口），其中hadoop的配置文件同样会影响spark任务获取数据的路径
//    val fileRDD: RDD[String] = sc.textFile("hdfs://master:8020/wcinput/word.txt")
     val fileRDD: RDD[String] = sc.textFile("hdfs://namenode:9000/data/input")

    // 下面是简化版写法file://input.tmp
    // val wordsRDD = inputRDD.flatMap(line => line.split("\\s+"))
    // val tuplesRDD: RDD[(String, Int)] = wordsRDD.map(word => (word, 1))
    // val wordCountsRDD: RDD[(String, Int)] = tuplesRDD.reduceByKey((tmp, item) => tmp + item)

    // 将文件中的数据进行分词
    val wordRDD: RDD[String] = fileRDD.flatMap(_.split(" "))
    // 转换数据结构 word => (word, 1)
    val word2OneRDD: RDD[(String, Int)] = wordRDD.map((_, 1))
    // 将转换结构后的数据按照相同的单词进行分组聚合
    val word2CountRDD: RDD[(String, Int)] = word2OneRDD.reduceByKey(_ + _)
    // 将数据聚合结果采集到内存中
    val word2Count: Array[(String, Int)] = word2CountRDD.collect()
    // 打印结果
    word2Count.foreach(println)

    sc.stop()
  }
}

