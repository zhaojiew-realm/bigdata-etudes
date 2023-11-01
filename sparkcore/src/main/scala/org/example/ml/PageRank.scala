package org.example.ml

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object PageRank {

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder.master("local")
      .appName("SparkPageRank")
      .getOrCreate()

    val iters = 2
    val lines = spark.sparkContext.parallelize(Array[String](
      ("2 1"), ("4 1"), ("1 2"), ("6 3"), ("7 3"), ("7 6"), ("6 7"), ("3 7"), ("2 3"),
      ("3 5"), ("5 2"), ("4 7"), ("1 7"), ("5, 7")
    ))
    val links = lines.map { s =>
      val parts = s.split("\\s+")
      (parts(0), parts(1))
    }.groupByKey().cache()
    var ranks: RDD[(String, Double)] = links.mapValues(v => 1.0)

    for (i <- 1 to iters) {
      val contribs = links.join(ranks).values.flatMap { case (urls, rank) =>
        val size = urls.size
        urls.map(url => (url, rank / size))
      }
      ranks = contribs.reduceByKey(_ + _).mapValues(0.15 + 0.85 * _)
    }

    val output: Array[(String, Double)] = ranks.collect()
    output.foreach(tup => println(s"${tup._1} has rank:  ${tup._2} ."))

    System.in.read()

    spark.stop()
  }
}
