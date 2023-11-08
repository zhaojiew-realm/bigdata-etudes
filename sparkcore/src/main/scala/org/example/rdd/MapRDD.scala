package org.example.rdd

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object MapRDD {
  def main(args: Array[String]): Unit = {
    val sparkconf = new SparkConf().setAppName("maprdd")
    val sc = new SparkContext(sparkconf)
    val seq = Seq[Int](1, 2, 3, 4)
    val rdd: RDD[Int] = sc.makeRDD(seq, 2)
    // map对每个数据单独操作
    //    rdd.map(_+2)
    //以分区为单位进行数据转换操作，但是会将整个分区的数据加载到内存进行引用
    //如果处理完的数据是不会被释放掉，存在对象的引用。在内存较小，数据量较大的场合下，容易出现内存溢出
    //每个iter都是一个迭代器
    val rdd2: RDD[Int] = rdd.mapPartitions((iter) => {
      iter.map(_ + 2)
    })
    rdd2.collect().foreach(println)

//    val dataRDD = sc.makeRDD(List(
//      List(1, 2), List(3, 4)
//    ), 1)
//    val dataRDD1 = dataRDD.flatMap(
//      list => list
//    )
//    dataRDD1.collect().foreach(println)
    sc.stop()
  }
}
