from pyspark import SparkConf, SparkContext
conf = SparkConf().setAppName("WordCount")
sc = SparkContext(conf = conf)
datas = ['hadoop spark', 'spark hive spark spark', 'spark hadoop python hive', ' ']
rdd = sc.parallelize(datas)
word_count_rdd = rdd.filter(lambda line: len(line.strip()) != 0).flatMap(lambda line: line.strip().split(" ")).map(lambda word: (word, 1)).reduceByKey(lambda a, b: a + b)

for word, count in word_count_rdd.collect():
    print(word, ', ', count)