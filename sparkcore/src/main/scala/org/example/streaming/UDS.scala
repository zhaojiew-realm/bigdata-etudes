package org.example.streaming


import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.util.Random




object UDS {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("streaming_wordcount")
    val scc = new StreamingContext(sparkConf,Seconds(3))

    val messageDS = scc.receiverStream(new MyReciever)
    messageDS.print()

    // start reciever
    scc.start()
    scc.awaitTermination()

//    scc.stop()

  }

  class MyReciever extends Receiver[String](StorageLevel.MEMORY_ONLY){
    private var flag = true
    override def onStart(): Unit = {
      new Thread(()=> {
        while (flag) {
          val message = "collect message: " + new Random().nextInt(10).toString()
          store(message)
          Thread.sleep(500)
        }
      }).start() // start thread
    }

    override def onStop(): Unit = {
      flag = false
    }
  }
}
