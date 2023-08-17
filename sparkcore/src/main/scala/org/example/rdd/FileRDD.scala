package org.example.rdd

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object FileRDD {
  def main(args: Array[String]): Unit = {
    val sparkconf = new SparkConf().setMaster("local[*]").setAppName("file")
    val sc = new SparkContext(sparkconf)
    // 从项目的根目录查找文件，可以填写相对或绝对路径，获取目录下所有文件
    val rdd: RDD[String] = sc.textFile("data/*.txt")
    //        val rdd = sc.wholeTextFiles("data/*.txt")
    rdd.collect().foreach(println)
    sc.stop()
  }
}
