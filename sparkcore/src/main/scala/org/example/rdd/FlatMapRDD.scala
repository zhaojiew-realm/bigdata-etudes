package org.example.rdd

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object FlatMapRDD {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "hadoop")
    val sparkconf = new SparkConf()
      .setMaster("yarn")
      .setAppName("file2")
    val sc = new SparkContext(sparkconf)
    val dataRDD = sc.makeRDD(List(List(1, 2), 3, List(4, 5)))
    val flatRDD: RDD[Any] = dataRDD.flatMap(
      data => {
        data match {
          case list: List[_] => list
          case item => List(item)
        }
      })
    flatRDD.collect().foreach(println)
  }

}
