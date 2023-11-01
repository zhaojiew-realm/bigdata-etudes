## hadoop使用s3

[Hadoop 集群中使用 S3(对象存储)文件系统](https://blog.csdn.net/cz124560/article/details/125295661)

[Hadoop/Spark on S3](https://xiaoxubeii.github.io/articles/hadoop-spark-on-s3/)

## 挂载juicefs

安装

```bash
curl -sSL https://d.juicefs.com/install | sh -
```

初始化juicefs

```bash
# 指定元数据存储elasticache，数据存储s3
juicefs format --storage s3 \
    --bucket https://zhaojiew-juicefs.s3.cn-north-1.amazonaws.com.cn \
    --access-key AKxxxxxxxxxxxIA \
    --secret-key e6TdxxxxxxxxxxxxxxxxxxxMwO \
    redis://:@mymain.ggqugq.ng.0001.cnn1.cache.amazonaws.com.cn:6379/1 \
    myjfs
```

挂载文件系统

```bash
# linux
juicefs mount redis://:@mymain.ggqugq.ng.0001.cnn1.cache.amazonaws.com.cn:6379/1 ~/jfs
# windows
juicefs mount redis://:@mymain.ggqugq.ng.0001.cnn1.cache.amazonaws.com.cn:6379/1 Z:
```

[在emr中使用juicefs](https://www.bilibili.com/video/BV1nm4y147B5/?spm_id_from=888.80997.embed_other.whitelist&vd_source=a136f72026ee8b3577e31b12a3a6f648)