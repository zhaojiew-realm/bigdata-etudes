package org.example.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object State {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("State")
    val scc = new StreamingContext(sparkConf,Seconds(3))

    // Exception in thread "main" java.lang.IllegalArgumentException: requirement failed: The checkpoint directory has not been set. Please set it by StreamingContext.checkpoint()

    // 无状态数据操作，只对当前的采集周期内的数据进行处理
    // 在某些场合下，需要保留数据统计结果（状态），实现数据的汇息
    // 使用有状态操作时，需要设定检查点路径
    scc.checkpoint("data/state")

    val lines = scc.socketTextStream("localhost", 18090)

    val wordToOne = lines.map((_,1))

    val state: DStream[(String, Int)] = wordToOne.updateStateByKey(
      (seq: Seq[Int], buff: Option[Int]) => {
        val newCount = buff.getOrElse(0) + seq.sum
        Option(newCount)
      }
    )

    state.print()
    // You can see in output still have "hello world" record
    // -------------------------------------------
    //Time: 1697450118000 ms
    //-------------------------------------------
    //(hello world,1)
    //
    //-------------------------------------------
    //Time: 1697450121000 ms
    //-------------------------------------------
    //(hello world,1)
    //(hello python,1)


    // start reciever
    scc.start()
    scc.awaitTermination()

    //client run command and send message to reciever
    // nc -lp 18090

//    scc.stop()

  }
}
