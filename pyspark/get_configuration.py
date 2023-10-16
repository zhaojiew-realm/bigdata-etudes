from pyspark.sql import SparkSession
import pprint

if __name__ == "__main__":
    spark = SparkSession.builder.getOrCreate()
    all_conf = spark.sparkContext.getConf().getAll()
    pprint.pprint(all_conf)