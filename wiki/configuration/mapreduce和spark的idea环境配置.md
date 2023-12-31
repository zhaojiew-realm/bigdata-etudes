## 环境配置

vscode环境配置java+scala，https://blog.nowcoder.net/n/ffdcd93114414d9ea9af9ee7cc4a56a4?from=nowcoder_improve

idea配置

- windows下需要winutils和hadoop.dll文件，配置HADOOP_HOME
- 需要在环境变量中指定HADOOP_USER_NAME=ec2-user
- 安装scala和bigdatatool插件

## 常见问题

### hadoop

对于maven项目，classpath 路径是：java 文件夹和 resources 文件夹下的根位置

[client提交任务的方式](https://blog.csdn.net/liujun122/article/details/106151618)

- 在本地通过hadoop-client，使用local job runner运行任务
- 在ide中将应用打包为uber jar，拷贝到hadoop集群中，使用hadoop命令运行
- 在ide中配置远程hdfs和yarn地址，自动远程提交job

本地模式运行mapreduce任务

- 在windows上需要配置hadoop环境
- 在linux上可以直接运行，hadoop-client本身包含了hadoop-common库

hadoop配置细节

- 配置java环境的地方有2个，hadoop的hadoop-env.sh和shell的环境变量中

jar包构建版本需要和hadoop集群的java版本一致，否则会出现找不到class的问题

```agsl
Exception in thread "main" java.lang.UnsupportedClassVersionError: org/example/wc/WordCount has been compiled by a more recent version of the Java Runtime (class file version 55.0), this version of the Java Runtime only recognizes class file versions up to 52.0
```

出现以下错误

> [Hadoop异常解决：Unable to load native-hadoop library](https://www.cnblogs.com/maxstack/p/13901201.html)

```
Unable to load native-hadoop library for your platform... using builtin-java classes where applicable

export HADOOP_OPTS="-Djava.library.path=${HADOOP_HOME}/lib/native"
```

### spark

spark配置细节

- 在实例上安装scala环境
- 安装scala插件，等待初始化即可
- 直接在原来的java maven项目中编写scala项目即可

spark任务提交

- 需要先注释hadoop-client的依赖，否则会出现冲突和错误，Sink class org.apache.spark.metrics.sink.MetricsServlet cannot be instantiated。https://coding.imooc.com/learn/questiondetail/234527.html

spark客户端配置

- hadoop配置文件同样会影响spark应用获取数据的路径，即使将运行模式调整为local也不影响（运行环境和数据路径没有相关性）

> Spark分区win10下ideaExitCodeException exitCode=-1073741515 异常

原因：操作系统缺少dll文件，需要安装c++的msvc依赖，https://blog.csdn.net/qq_49472679/article/details/119110003