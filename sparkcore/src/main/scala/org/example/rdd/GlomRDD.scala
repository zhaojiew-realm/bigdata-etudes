package org.example.rdd

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object GlomRDD {
  def main(args: Array[String]): Unit = {
    val sparkconf = new SparkConf().setMaster("local[*]").setAppName("memory")
    val sc = new SparkContext(sparkconf)
    val dataRDD: RDD[Int] = sc.makeRDD(List(1, 2, 3, 4), 2)
    val glomRDD: RDD[Array[Int]] = dataRDD.glom()
    glomRDD.collect().foreach(data => println(data.mkString(",")))


    //max 1, 2, 3, 4
    // (1,2) (3,4)
    // (2,4) sum
    val maxRDD: RDD[Int] = glomRDD.map(data => {
      data.max
    })
    println(maxRDD.collect().sum)

    sc.stop()
  }
}
