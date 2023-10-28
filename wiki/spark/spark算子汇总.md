## spark算子汇总

什么是shuffle

- https://spark.apache.org/docs/latest/rdd-programming-guide.html

> In Spark, data is generally not distributed across partitions to be in the necessary place for a specific operation. During computations, a single task will operate on a single partition - thus, to organize all the data for a single reduceByKey reduce task to execute, Spark needs to perform an all-to-all operation. It must read from all partitions to find all the values for all keys, and then bring together values across partitions to compute the final result for each key - this is called the shuffle.

![image-20231028165716677](C:\Users\Administrator\Desktop\wiki\spark\assets\image-20231028165716677.png)

### Transformation：转换算子

并不触发提交作业，完成作业中间过程处理

Transformation操作是延迟计算 — 懒惰机制，RDD 转换生成另一个RDD的转换操作，并不是马上执行，只有遇到Action操作的时候，才会真正的触发运算

**Transformation算子细分：**

单独对v处理：（value）

- 一对一：map ，flatMap
- 多到一：union ，cartesion（笛卡儿积）

> *spark不支持unionall，union=unionall* ——— spark的union不去重

- 多对多：groupby
- 输出是输入的子集合：filter，distinct
- cache类：cache，persist

k-v形式：（key -> value）

- 一对一：mapValues
- 单个聚集（RDD）：reduceByKey，combineByKey，PartitionBy
- 两个聚集（RDD）：cogroup
- 连接：join，lefOutJoin，rightOutJoin

### **Action**：行动算子

触发（SparkContext）提交作业，并将数据输出Spark系统（hdfs，hbase，kafka，console）

**Action算子细分：**

进一步细分：

- 无输出： foreach
- 有输出：saveAsTextFile（存到HDFS）
- 统计类：count，collect，take（取top {$n}）

[![img](https://img2018.cnblogs.com/blog/1713916/201908/1713916-20190819211111235-1576073779.png)](https://img2018.cnblogs.com/blog/1713916/201908/1713916-20190819211111235-1576073779.png)