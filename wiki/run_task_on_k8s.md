## hadoop

```
hadoop jar wc.jar com.zhaojie.wordcount.WordCountDriver /pythonzen.txt /output
```

## spark

[在kubernetes上运行spark任务](https://spark.apache.org/docs/latest/running-on-kubernetes.html)

```shell
bin/spark-submit \
--class org.apache.spark.examples.SparkPi \
--master spark://hadoop26:7077 \
./examples/jars/spark-examples_2.12-3.0.0.jar \
10
```

