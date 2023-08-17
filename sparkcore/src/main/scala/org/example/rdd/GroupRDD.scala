package org.example.rdd

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object GroupRDD {
  def main(args: Array[String]): Unit = {
    // groupBy会将数据源中的每一个数据进行分组判断，根据返回的分组key进行分组
    //相同的key值的数据会放置在一个组中
    val sparkconf = new SparkConf().setMaster("local[*]").setAppName("memory")
    val sc = new SparkContext(sparkconf)
    val seq = Seq[Int](1, 2, 3, 4)
    val dataRDD: RDD[Int] = sc.makeRDD(seq, 2)
    val groupRDD: RDD[(Int, Iterable[Int])] = dataRDD.groupBy(data => {
      data % 2
    })
    // 一个组的数据在一个分区中，但是并不是说一个分区中只有一个组, 一个分区可以有多个组
    // 分区和分组并没有必然的联系
    groupRDD.collect().foreach(println)


    // Start
    val data2RDD: RDD[String] = sc.makeRDD(List("hadoop", "hive", "scala", "spark"))
    val group2RDD: RDD[(Char, Iterable[String])] = data2RDD.groupBy(_.charAt(0))
    group2RDD.collect().foreach(println)

    sc.stop()
  }
}
