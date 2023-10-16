package org.example.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

object WordCout {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("streaming_wordcount")
    val scc = new StreamingContext(sparkConf,Seconds(3))

    val lines = scc.socketTextStream("localhost", 18090)

    val words = lines.flatMap((line)=>{
      line.split(" ")
    })
    val word_one = words.map((_,1))
    val word_key = word_one.reduceByKey(_ + _)

    word_key.print()

    // start reciever
    scc.start()
    scc.awaitTermination()

    //client run command and send message to reciever
    // nc -lp 18090

//    scc.stop()

  }
}
