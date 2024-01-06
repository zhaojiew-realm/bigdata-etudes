参考资料

- https://docs.aws.amazon.com/emr/latest/ReleaseGuide/emr-spark-docker.html
- https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/DockerContainers.html

集群配置

- LCE 要求容器执行器二进制文件归 root：hadoop 所有，并具有 6050 个权限
- docker daemon和docker client必须在所有 NodeManager 上运行

