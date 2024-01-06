package org.example.sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object ShowDatabases {
  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf()
      .setAppName("showdatabases")

    val spark = SparkSession.builder()
      .config(sparkConf)
      .enableHiveSupport()
      .getOrCreate()

    spark.sql("select * from userinfo.userinfo").show

    spark.close()
  }
}


