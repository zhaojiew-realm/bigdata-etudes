package org.example.ml


import org.apache.spark.sql.SparkSession
import breeze.linalg.{DenseVector, Vector}
import java.util.Random
import scala.math.exp

// https://github.com/JerryLead/ApacheSparkBook/blob/master/src/main/scala/org/apache/spark/examples/SparkLR.scala
// Logistic 回归与梯度下降, https://zhuanlan.zhihu.com/p/381017601
object Logistic_regression {
  val N = 10000 // Number of data points
  val D = 10 // Number of dimensions
  val R = 0.7 // Scaling factor
  val ITERATIONS = 10
  val rand = new Random(42)

  case class DataPoint(x: Vector[Double], y: Double)

  def generateData: Array[DataPoint] = {
    def generatePoint(i: Int): DataPoint = {
      val y = if (i % 2 == 0) -1 else 1
      val x: DenseVector[Double] = DenseVector.fill(D) {
        rand.nextGaussian + y * R
      }
      DataPoint(x, y)
    }

    Array.tabulate(N)(generatePoint)
  }

  def main(args: Array[String]) {

    val spark = SparkSession
      .builder.master("local")
      .appName("SparkLR")
      .getOrCreate()

    val numSlices = if (args.length > 0) args(0).toInt else 2
    val points = spark.sparkContext.parallelize(generateData, numSlices).cache()

    // Initialize w to a random value
    val w: DenseVector[Double] = DenseVector.fill(D) {
      2 * rand.nextDouble - 1
    }

    // 每轮迭代开始时， Spark首先将w广播到所有的task中, per-task
    println(s"Initial w: $w")

    for (i <- 1 to ITERATIONS) {
      println(s"On iteration $i")
      val gradient: Vector[Double] = points.map { p =>
        p.x * (1 / (1 + exp(-p.y * (w.dot(p.x)))) - 1) * p.y
      }.reduce(_ + _)
      w -= gradient
    }

    println(s"Final w: $w")

    System.in.read()

    spark.stop()
  }
}
