package org.example.streaming.mockdata.datasource

import java.io.InputStreamReader
import java.util.Properties

object PropertiesUtil {

  def load(propertiesName: String): Properties = {
    val prop = new Properties()
    prop.load(new
        InputStreamReader(Thread.currentThread().getContextClassLoader.getResourceAsStream(propertiesName), "UTF-8"))
    prop
  }

}
