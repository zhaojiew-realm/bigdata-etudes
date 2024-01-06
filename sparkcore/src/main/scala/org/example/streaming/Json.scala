package org.example.streaming

import org.apache.spark.SparkConf
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{DataFrame, SparkSession}

object Json {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("sql")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    // 从HTTP数据源读取JSON数据
    val lines: DataFrame = spark.readStream.
      format("socket")
      .option("host", "52.81.81.244")
      .option("port", 5000).load()

    val query = lines.writeStream
      .format("console")
      .outputMode("append")
      .trigger(Trigger.ProcessingTime("1 second"))
      .start()

    query.awaitTermination()
  }
}
