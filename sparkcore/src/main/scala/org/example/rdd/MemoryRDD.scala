package org.example.rdd

import org.apache.spark.{SparkConf, SparkContext}

object MemoryRDD {
  def main(args: Array[String]): Unit = {
    val sparkconf = new SparkConf().setMaster("local[*]").setAppName("memory")
    val sc = new SparkContext(sparkconf)
    val seq = Seq[Int](1, 2, 3, 4)
    val rdd = sc.parallelize(seq,2)
    rdd.saveAsTextFile("output")
    sc.stop()
  }
}
