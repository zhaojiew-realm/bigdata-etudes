package org.example.streaming

import org.apache.spark.SparkConf
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.streaming.{StreamingQuery, Trigger}
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession, functions}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Tmp {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("sql")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    val lines: DataFrame = spark.readStream.
      format("socket")
      .option("host", "localhost")
      .option("port", 19999).load()

    val words: DataFrame = lines.select(functions.split(col("value"), "\\s").as("word"))

    val count: DataFrame = words.groupBy("word").count()


    val checkpointDir = "data/store"

    val streamingQuery: StreamingQuery = count.writeStream
      .format("console")
      .outputMode("complete")
      .trigger(Trigger.ProcessingTime("1 second"))
      .option("checkpointLocation", checkpointDir)
      .start()

    streamingQuery.awaitTermination()



    //client run command and send message to reciever
    // nc -lp 19999

  }

  case class Line(word: String,Count:Int)
}
