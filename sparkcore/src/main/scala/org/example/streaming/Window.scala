package org.example.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Window {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("NetworkWordCount")
    val ssc = new StreamingContext(conf, Seconds(3))

    ssc.checkpoint("data/state")

    // Create a DStream that will connect to hostname:port, like localhost:19999
    val lines = ssc.socketTextStream("localhost", 19999)

    // Split each line into words
    val words = lines.flatMap(_.split(" "))
    // Count each word in each batch
    val pairs = words.map(word => (word, 1))
    // 3 秒一个批次(collect period)，窗口 12 秒，滑步 6 秒 (default same as collect period 3s)
    val windowDS: DStream[(String, Int)] = pairs.window(Seconds(12), Seconds(6))

    val wordCounts = windowDS.reduceByKey(_ + _)

    // Print the first ten elements of each RDD generated in this DStream to the console
    wordCounts.print()

    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate

  }
}
