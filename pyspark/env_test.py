from pyspark import SparkConf
from pyspark import SparkContext

if __name__ == "__main__":
    conf = SparkConf().setMaster('local').setAppName('envtest')
    sc = SparkContext(conf=conf)
    rdd = sc.parallelize([('a', 7), ('a', 2), ('b', 2)])
    a = rdd.values().collect()
    print(a)