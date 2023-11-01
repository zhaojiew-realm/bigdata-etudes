package org.example.core

import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object ComplexRDD {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName("ComplexRDD")

    val sc: SparkContext = new SparkContext(sparkConf)
    //构建一个<K，V>类型的rdd1
    val datal = Array[(Int, Char)]((1, 'a'), (2, 'b'), (3, 'c'), (4, 'd'), (5, 'e'), (3, 'f'), (2, 'g'), (1, 'h'))
    val rddl: RDD[(Int, Char)] = sc.parallelize(datal, 3)

    //使用HashPartitioner对rdd1进行重新划分
    val partitionedRDD = rddl.partitionBy(new HashPartitioner(3))

    //构建一个<K，V>类型的rdd2，并对rdd2中record的Value进行复制
    val data2 = Array[(Int, String)]((1, "A"), (2, "B"), (3, "c"), (4, "D"))
    val rdd2 = sc.parallelize(data2, 2).map(x => (x._1, x._2 + "" + x._2))

    //构建一个<K，V>类型的rdd3
    val data3 = Array[(Int, String)]((3, "x"), (5, "y"), (3, "z"), (4, "y"))
    val rdd3 = sc.parallelize(data3, 2)

    //将rdd2和rdd3进行union）操作
    val unionedRDD = rdd2.union(rdd3)

    //将被重新划分过的rdd1与unionRDD进行join （）操作
    val resultRDD = partitionedRDD.join(unionedRDD)

    //输出join（）操作后的结果，包括每个record及其index
    resultRDD.foreach(println)

    // wait and visit http://127.0.0.1:4040
    Thread.sleep(1000*1800)

  }
}
