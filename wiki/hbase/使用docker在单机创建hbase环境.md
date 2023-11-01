`docker-compose.yaml`配置

```yaml
version: "3"

services:
  namenode:
    image: xxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hadoop-namenode:2.0.0-hadoop3.2.1-java8
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
    image: xxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hadoop-datanode:2.0.0-hadoop3.2.1-java8
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
    image: xxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hadoop-resourcemanager:2.0.0-hadoop3.2.1-java8
    container_name: resourcemanager
    environment:
      SERVICE_PRECONDITION: "namenode:9870 datanode:9864"
    env_file:
      - ./hadoop.env
    ports:
      - 8088:8088

  nodemanager1:
    image: xxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hadoop-nodemanager:2.0.0-hadoop3.2.1-java8
    container_name: nodemanager
    environment:
      SERVICE_PRECONDITION: "namenode:9870 datanode:9864 resourcemanager:8088"
    env_file:
      - ./hadoop.env
    ports:
      - 8042:8042

  historyserver:
    image: xxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hadoop-historyserver:2.0.0-hadoop3.2.1-java8
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
    image: xxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/zookeeper:3.4.10
    container_name: zoo
    environment:
      ZOO_MY_ID: 1
      ZOO_SERVERS: server.1=0.0.0.0:2888:3888
    ports:
      - 2181:2181

  hbase-master:
    image: xxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hbase-master:1.0.0-hbase1.2.6
    container_name: hbase-master
    hostname: hbase-master
    env_file:
      - ./hbase-distributed-local.env
    environment:
      SERVICE_PRECONDITION: "namenode:9870 datanode:9864 zoo:2181"
    ports:
      - 16000:16000
      - 16010:16010

  hbase-region1:
    image: xxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hbase-regionserver:1.0.0-hbase1.2.6
    container_name: hbase-regionserver1
    hostname: hbase-regionserve1
    env_file:
      - ./hbase-distributed-local.env
    environment:
      HBASE_CONF_hbase_regionserver_hostname: hbase-region1
      SERVICE_PRECONDITION: "namenode:9870 datanode:9864 zoo:2181 hbase-master:16010"
    ports:
      - 16030:16030

  hbase-region2:
    image: xxxxxxxx.dkr.ecr.cn-north-1.amazonaws.com.cn/hbase-regionserver:1.0.0-hbase1.2.6
    container_name: hbase-regionserver2
    hostname: hbase-regionserver2
    env_file:
      - ./hbase-distributed-local.env
    environment:
      HBASE_CONF_hbase_regionserver_hostname: hbase-region2
      SERVICE_PRECONDITION: "namenode:9870 datanode:9864 zoo:2181 hbase-master:16010"
    ports:
      - 16031:16030

volumes:
  hadoop_namenode:
  hadoop_datanode:
  hadoop_historyserver:
```

环境变量配置

```properties
$ cat hadoop.env
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
```

hbase配置文件

```properties
$ cat hbase-distributed-local.env
HBASE_CONF_hbase_rootdir=hdfs://namenode:9000/hbase
HBASE_CONF_hbase_cluster_distributed=true
HBASE_CONF_hbase_zookeeper_quorum=zoo

HBASE_CONF_hbase_master=hbase-master:16000
HBASE_CONF_hbase_master_hostname=hbase-master
HBASE_CONF_hbase_master_port=16000
HBASE_CONF_hbase_master_info_port=16010
HBASE_CONF_hbase_regionserver_port=16020
HBASE_CONF_hbase_regionserver_info_port=16030

HBASE_MANAGES_ZK=false
```

