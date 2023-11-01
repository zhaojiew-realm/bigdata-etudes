参考资料

- https://github.com/big-data-europe/docker-spark
- https://github.com/big-data-europe/docker-hadoop
- https://tellyouwhat.cn/p/docker-build-spark-wordcount-app/

查看日志

```
docker run --detach --volume=/var/run/docker.sock:/var/run/docker.sock -p 8888:8080 amir20/dozzle
```

## 部署hadoop

- https://blog.csdn.net/Bob______/article/details/123440909

docker-compose文件

```yaml
version: "3"
services:
  namenode:
    image: bde2020/hadoop-namenode:2.0.0-hadoop3.2.1-java8
    container_name: namenode
    restart: always
    ports:
      - 9870:9870
      - 9000:9000   
    volumes:
      - hadoop_namenode:/hadoop/dfs/name
    environment:
      - CLUSTER_NAME=test
    env_file:
      - ./hadoop.env

  datanode:
    image: bde2020/hadoop-datanode:2.0.0-hadoop3.2.1-java8
    container_name: datanode1
    restart: always
    volumes:
      - hadoop_datanode:/hadoop/dfs/data
    ports:
      - 9864:9864
    environment:
      SERVICE_PRECONDITION: "namenode:9870"
    env_file:
      - ./hadoop.env

  datanode2:
    image: bde2020/hadoop-datanode:2.0.0-hadoop3.2.1-java8
    container_name: datanode2
    restart: always
    volumes:
      - hadoop_datanode2:/hadoop/dfs/data
    ports:
      - 9863:9864
    environment:
      SERVICE_PRECONDITION: "namenode:9870"
    env_file:
      - ./hadoop.env

  resourcemanager:
    image: bde2020/hadoop-resourcemanager:2.0.0-hadoop3.2.1-java8
    container_name: resourcemanager
    restart: always
    environment:
      SERVICE_PRECONDITION: "namenode:9000 namenode:9870 datanode:9864 datanode2:9864"
    ports:
      - 8048:8088
    expose:
      - "8032"
      - "8031"
      - "8030"
    env_file:
      - ./hadoop.env

  nodemanager1:
    image: bde2020/hadoop-nodemanager:2.0.0-hadoop3.2.1-java8
    container_name: nodemanager
    restart: always
    environment:
      SERVICE_PRECONDITION: "namenode:9000 namenode:9870 datanode:9864 datanode2:9864 resourcemanager:8088"
    ports:
      - 8042:8042
    env_file:
      - ./hadoop.env
  
  historyserver:
    image: bde2020/hadoop-historyserver:2.0.0-hadoop3.2.1-java8
    container_name: historyserver
    restart: always
    environment:
      SERVICE_PRECONDITION: "namenode:9000 namenode:9870 datanode:9864 datanode2:9864 resourcemanager:8088"
    volumes:
      - hadoop_historyserver:/hadoop/yarn/timeline
    ports:
      - 8188:8188
    env_file:
      - ./hadoop.env

  spark-master:
    image: bde2020/spark-master:3.2.0-hadoop3.2
    container_name: spark-master
    ports:
      - "8088:8080"
      - "7077:7077"
    environment:
      - INIT_DAEMON_STEP=setup_spark
  spark-worker-1:
    image: bde2020/spark-worker:3.2.0-hadoop3.2
    container_name: spark-worker-1
    depends_on:
      - spark-master
    ports:
      - "8081:8081"
    environment:
      - "SPARK_MASTER=spark://spark-master:7077"
  spark-worker-2:
    image: bde2020/spark-worker:3.2.0-hadoop3.2
    container_name: spark-worker-2
    depends_on:
      - spark-master
    ports:
      - "8082:8081"
    environment:
      - "SPARK_MASTER=spark://spark-master:7077"
  spark-history-server:
      image: bde2020/spark-history-server:3.2.0-hadoop3.2
      container_name: spark-history-server
      depends_on:
        - spark-master
      ports:
        - "18081:18081"
      volumes:
        - /tmp/spark-events-local:/tmp/spark-events
 
  zeppelin:
    image: xxxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/zeppelin:0.8.0-hadoop-2.8.0-spark-2.3.1
    ports:
      - 8078:8080
    volumes:
      - ./notebook:/opt/zeppelin/notebook
    environment:
      SPARK_MASTER: "spark://spark-master:7077"
      MASTER: "spark:spark-master:7077"
    env_file:
      - ./hadoop.env

volumes:
  hadoop_namenode:
  hadoop_datanode:
  hadoop_datanode2:
  hadoop_historyserver:

```

配置文件

```
CORE_CONF_fs_defaultFS=hdfs://namenode:9000
CORE_CONF_hadoop_http_staticuser_user=root
CORE_CONF_hadoop_proxyuser_hue_hosts=*
CORE_CONF_hadoop_proxyuser_hue_groups=*
CORE_CONF_io_compression_codecs=org.apache.hadoop.io.compress.SnappyCodec

HDFS_CONF_dfs_webhdfs_enabled=true
HDFS_CONF_dfs_permissions_enabled=false
HDFS_CONF_dfs_namenode_datanode_registration_ip___hostname___check=false

YARN_CONF_yarn_log___aggregation___enable=true
YARN_CONF_yarn_log_server_url=http://historyserver:8188/applicationhistory/logs/
YARN_CONF_yarn_resourcemanager_recovery_enabled=true
YARN_CONF_yarn_resourcemanager_store_class=org.apache.hadoop.yarn.server.resourcemanager.recovery.FileSystemRMStateStore
YARN_CONF_yarn_resourcemanager_scheduler_class=org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler
YARN_CONF_yarn_scheduler_capacity_root_default_maximum___allocation___mb=8192
YARN_CONF_yarn_scheduler_capacity_root_default_maximum___allocation___vcores=4
YARN_CONF_yarn_resourcemanager_fs_state___store_uri=/rmstate
YARN_CONF_yarn_resourcemanager_system___metrics___publisher_enabled=true
YARN_CONF_yarn_resourcemanager_hostname=resourcemanager
YARN_CONF_yarn_resourcemanager_address=resourcemanager:8032
YARN_CONF_yarn_resourcemanager_scheduler_address=resourcemanager:8030
YARN_CONF_yarn_resourcemanager_resource__tracker_address=resourcemanager:8031
YARN_CONF_yarn_timeline___service_enabled=true
YARN_CONF_yarn_timeline___service_generic___application___history_enabled=true
YARN_CONF_yarn_timeline___service_hostname=historyserver
YARN_CONF_mapreduce_map_output_compress=true
YARN_CONF_mapred_map_output_compress_codec=org.apache.hadoop.io.compress.SnappyCodec
YARN_CONF_yarn_nodemanager_resource_memory___mb=16384
YARN_CONF_yarn_nodemanager_resource_cpu___vcores=8
YARN_CONF_yarn_nodemanager_disk___health___checker_max___disk___utilization___per___disk___percentage=98.5
YARN_CONF_yarn_nodemanager_remote___app___log___dir=/app-logs
YARN_CONF_yarn_nodemanager_aux___services=mapreduce_shuffle

MAPRED_CONF_mapreduce_framework_name=yarn
MAPRED_CONF_mapred_child_java_opts=-Xmx4096m
MAPRED_CONF_mapreduce_map_memory_mb=4096
MAPRED_CONF_mapreduce_reduce_memory_mb=8192
MAPRED_CONF_mapreduce_map_java_opts=-Xmx3072m
MAPRED_CONF_mapreduce_reduce_java_opts=-Xmx6144m
MAPRED_CONF_yarn_app_mapreduce_am_env=HADOOP_MAPRED_HOME=/opt/hadoop-3.2.1/
MAPRED_CONF_mapreduce_map_env=HADOOP_MAPRED_HOME=/opt/hadoop-3.2.1/
MAPRED_CONF_mapreduce_reduce_env=HADOOP_MAPRED_HOME=/opt/hadoop-3.2.1/
```

## 部署spark

```yaml
version: '3'
services:
  spark-master:
    image: bde2020/spark-master:3.2.0-hadoop3.2
    container_name: spark-master
    ports:
      - "8080:8080"
      - "7077:7077"
    environment:
      - INIT_DAEMON_STEP=setup_spark
  spark-worker-1:
    image: bde2020/spark-worker:3.2.0-hadoop3.2
    container_name: spark-worker-1
    depends_on:
      - spark-master
    ports:
      - "8081:8081"
    environment:
      - "SPARK_MASTER=spark://spark-master:7077"
  spark-worker-2:
    image: bde2020/spark-worker:3.2.0-hadoop3.2
    container_name: spark-worker-2
    depends_on:
      - spark-master
    ports:
      - "8082:8081"
    environment:
      - "SPARK_MASTER=spark://spark-master:7077"
  spark-history-server:
      image: bde2020/spark-history-server:3.2.0-hadoop3.2
      container_name: spark-history-server
      depends_on:
        - spark-master
      ports:
        - "18081:18081"
      volumes:
        - /tmp/spark-events-local:/tmp/spark-events
```

安装pyspark

```
sudo pip install pyspark -i https://pypi.tuna.tsinghua.edu.cn/simple
```

使用`pyspark`测试

```python
from pyspark import SparkConf, SparkContext
conf = SparkConf().setAppName("My App")
sc = SparkContext(conf = conf)
datas = ['hadoop spark', 'spark hive spark spark', 'spark hadoop python hive', ' ']
rdd = sc.parallelize(datas)
word_count_rdd = rdd.filter(lambda line: len(line.strip()) != 0).flatMap(lambda line: line.strip().split(" ")).map(lambda word: (word, 1)).reduceByKey(lambda a, b: a + b)

for word, count in word_count_rdd.collect():
  print(word, ', ', count)
```

提交任务

```shell
spark-submit --master spark://172.21.0.2:7077 main.py

spark-submit main.py
```


## 部署hive 

```yaml
version: "3"

services:
  namenode:
    image: xxxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hadoop-namenode:2.0.0-hadoop3.2.1-java8
    container_name: namenode
    volumes:
      - hadoop_namenode:/hadoop/dfs/name
    environment:
      - CLUSTER_NAME=test
    env_file:
      - ./hadoop.env
    ports:
      - 9870:9870
      - 9000:9000 

  datanode:
    image: xxxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hadoop-datanode:2.0.0-hadoop3.2.1-java8
    container_name: datanode
    volumes:
      - hadoop_datanode:/hadoop/dfs/data
    environment:
      SERVICE_PRECONDITION: "namenode:9870"
    env_file:
      - ./hadoop.env
    ports:
      - 9864:9864

  resourcemanager:
    image: xxxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hadoop-resourcemanager:2.0.0-hadoop3.2.1-java8
    container_name: resourcemanager
    environment:
      SERVICE_PRECONDITION: "namenode:9870 datanode:9864"
    env_file:
      - ./hadoop.env
    ports:
      - 8088:8088

  nodemanager1:
    image: xxxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hadoop-nodemanager:2.0.0-hadoop3.2.1-java8
    container_name: nodemanager
    environment:
      SERVICE_PRECONDITION: "namenode:9870 datanode:9864 resourcemanager:8088"
    env_file:
      - ./hadoop.env
    ports:
      - 8042:8042

  historyserver:
    image: xxxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hadoop-historyserver:2.0.0-hadoop3.2.1-java8
    container_name: historyserver
    volumes:
      - hadoop_historyserver:/hadoop/yarn/timeline
    environment:
      SERVICE_PRECONDITION: "namenode:9870 datanode:9864 resourcemanager:8088"
    env_file:
      - ./hadoop.env
    ports:
      - 8188:8188

  zoo:
    image: xxxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/zookeeper:3.4.10
    container_name: zoo
    environment:
      ZOO_MY_ID: 1
      ZOO_SERVERS: server.1=0.0.0.0:2888:3888
    ports:
      - 2181:2181

  hbase-master:
    image: xxxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hbase-master:1.0.0-hbase1.2.6
    container_name: hbase-master
    hostname: hbase-master
    env_file:
      - ./hbase-distributed-local.env
    environment:
      SERVICE_PRECONDITION: "namenode:9870 datanode:9864 zoo:2181"
    ports:
      - 16010:16010

  hbase-region:
    image: xxxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hbase-regionserver:1.0.0-hbase1.2.6
    container_name: hbase-regionserver
    hostname: hbase-regionserver
    env_file:
      - ./hbase-distributed-local.env
    environment:
      HBASE_CONF_hbase_regionserver_hostname: hbase-region
      SERVICE_PRECONDITION: "namenode:9870 datanode:9864 zoo:2181 hbase-master:16010"
    ports:
      - 16030:16030

volumes:
  hadoop_namenode:
  hadoop_datanode:
  hadoop_historyserver:
```