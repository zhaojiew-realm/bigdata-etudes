package org.example.sql

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

object Basic {
  def main(args: Array[String]): Unit = {


    // create spark runtime
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("sql")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    // run logical exec
    // DataFrame
    val df: DataFrame = spark.read.json("data/user.json")
    //  df.show()

    // dataframe => sql
    df.createOrReplaceTempView("user")
    spark.sql("select * from user").show

    // dataframe => dsl
    // related to implicit transfrom
    import spark.implicits._
    df.select("age", "username").show
    df.select('age + 1).show

    // dataset
    val seq = Seq(1, 2, 3, 4)
    val ds: Dataset[Int] = seq.toDS()
    ds.show()

    // rdd <=> dataframe
    val rdd = spark.sparkContext.makeRDD(List((1, "zhangsan", 20), (2, "lisi", 30)))
    val rdddf: DataFrame = rdd.toDF("id", "name", "age")
    val rowRDD: RDD[Row] = rdddf.rdd

    // dataframe <=> dataset
    val ds2: Dataset[User] = rdddf.as[User]
    val df2 = ds2.toDF()

    // rdd <=>dateset
    val ds3 = rdd.map {
      case (id, name, age) => {
        User(id, name, age)
      }
    }.toDS()
    val rdd2 = ds3.rdd

    // close runtime
    spark.close()
  }

  case class User(id: Int, name:String, age:Int)
}


