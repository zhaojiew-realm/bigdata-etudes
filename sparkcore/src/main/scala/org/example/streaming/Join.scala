package org.example.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Join {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("jointest")
    val ssc = new StreamingContext(sparkConf,Seconds(3))

    val lineDStream1: ReceiverInputDStream[String] = ssc.socketTextStream("localhost", 19999)
    val lineDStream2: ReceiverInputDStream[String] = ssc.socketTextStream("localhost", 18888)

    val wordToOneDStream: DStream[(String, Int)] = lineDStream1.map((_, 1))
    val wordToADStream: DStream[(String, Int)] = lineDStream2.map((_, 1))

    val joinDStream: DStream[(String, (Int, Int))] = wordToOneDStream.join(wordToADStream)

    joinDStream.print()

    // start reciever
    ssc.start()
    ssc.awaitTermination()

    //client run command and send message to reciever
    // nc -lp 18090

//    scc.stop()

  }
}
