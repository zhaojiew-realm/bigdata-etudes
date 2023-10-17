package org.example.streaming.mockdata.datasource

import java.util.Random
import scala.collection.mutable.ListBuffer

case class RanOpt[T](value: T, weight: Int)

object RandomOptions {
  def apply[T](opts: RanOpt[T]*): RandomOptions[T] = {
    val randomOptions = new RandomOptions[T]()
    for (opt <- opts) {
      randomOptions.totalWeight += opt.weight
      for (i <- 1 to opt.weight) {
        randomOptions.optsBuffer += opt.value
      }
    }
    randomOptions
  }
}

class RandomOptions[T](opts: RanOpt[T]*) {
  var totalWeight = 0
  var optsBuffer = new ListBuffer[T]
  def getRandomOpt: T = {
    val randomNum: Int = new Random().nextInt(totalWeight)
    optsBuffer(randomNum)
  }
}