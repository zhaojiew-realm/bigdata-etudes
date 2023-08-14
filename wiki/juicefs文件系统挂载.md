## 挂载juicefs

安装
```bash
curl -sSL https://d.juicefs.com/install | sh -
```

初始化
```bash
# 指定元数据存储elasticache，数据存储s3
juicefs format --storage s3 \
    --bucket https://zhaojiew-juicefs.s3.cn-north-1.amazonaws.com.cn \
    --access-key AKIAQRIBWRJKGEUXMR6R \
    --secret-key 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 \
    redis://:@mymain.ggqugq.ng.0001.cnn1.cache.amazonaws.com.cn:6379/1 \
    myjfs
```

挂载文件系统

```bash
# linux
juicefs mount redis://:@mymain.ggqugq.ng.0001.cnn1.cache.amazonaws.com.cn:6379/1 ~/jfs
# windows
juicefs mount redis://:@mymain.ggqugq.ng.0001.cnn1.cache.amazonaws.com.cn:6379/1 Z://
```