转载链接

- https://zhuanlan.zhihu.com/p/111696858
- https://blog.51cto.com/u_16213368/8075696

## 为什么要进行刷写和合并

HBase 是 Google BigTable 的开源实现，**底层存储引擎是基于 LSM 树（Log-Structured Merge Tree）数据结构设计的**。写入数据时会先写 WAL 日志，再将数据写到写缓存 MemStore 中，等写缓存达到一定规模或其他触发条件时会 Flush 刷写到磁盘，生成一个 HFile 文件，这样就**将磁盘随机写变成了顺序写**，提高了写性能。

随着时间推移，写入的 HFile 会越来越多，读取数据时就会因为要进行多次io导致性能降低，因此 HBase 会定期执行 Compaction 操作以合并减少 HFile 数量，提升读性能。

<img src="https://pic1.zhimg.com/v2-8b8f2e77f1f047d96a3c7538d9f660d0_r.jpg" alt="img" style="zoom:67%;" />

## Flush 触发条件和参数

理解 Flush 的触发条件非常重要，从中我们也可以看出何时会阻塞写请求，有 7 种情况会触发 Flush：

1、当一个 MemStore 大小达到阈值`hbase.hregion.memstore.flush.size`（默认128M）时，会触发 MemStore 的刷写。这个时候不会阻塞写请求。

2、当一个 Region 中所有 MemStore 总大小达到 `hbase.hregion.memstore.block.multiplier * hbase.hregion.memstore.flush.size`（默认4*128M=512M）时，会触发 MemStore 的刷写，**并阻塞 Region 所有的写请求**，此时写数据会出现 RegionTooBusyException异常。

3、当一个 RegionServer 中所有 MemStore 总大小达到 `hbase.regionserver.global.memstore.size.lower.limit* hbase.regionserver.global.memstore.size * hbase_heapsize`（低水位阈值，默认0.95 * 0.4 * RS 堆大小）时，会触发 RegionServer 中**内存占用大的 MemStore 的刷写**；达到 `hbase.regionserver.global.memstore.size * hbase_heapsize`（高水位阈值，默认0.4 * RS堆大小）时，不仅会触发 Memstore 的刷写，还会**阻塞 RegionServer 所有的写请求**，直到 Memstore 总大小降到低水位阈值以下。

4、当一个 RegionServer 的 HLog 即**WAL文件数量达到上限**（可通过参数`hbase.regionserver.maxlogs` 配置，默认32）时，也会触发 MemStore 的刷写，HBase 会找到最旧的 HLog 文件对应的 Region 进行刷写 。

5、当一个 Region 的更新次数达到 `hbase.regionserver.flush.per.changes`（默认30000000即3千万）时，也会触发 MemStore 的刷写。

6、 定期 `hbase.regionserver.optionalcacheflushinterval`（默认3600000即一个小时）进行 MemStore 的刷写，确保 MemStore 不会长时间没有持久化。为避免所有的 MemStore 在同一时间进行 flush 而导致问题，定期的 flush 操作会有一定时间的随机延时。

7、 手动执行 flush 操作，我们可以通过 hbase shell 或 API 对一张表或一个 Region 进行 flush。

上面是 Flush 的几个触发条件，从中我们拿到 5 个和 Flush 有关的重要参数，并给出调整建议：

- hbase.hregion.memstore.flush.size

默认值 128M，单个 MemStore 大小超过该阈值就会触发 Flush。如果当前集群 Flush 比较频繁，并且内存资源比较充裕，建议适当调整为 256M。调大的副作用可能是造成宕机时需要分裂的 HLog 数量变多，从而延长故障恢复时间。

- hbase.hregion.memstore.block.multiplier

默认值 4，Region 中所有 MemStore 超过单个 MemStore 大小的倍数达到该参数值时，就会阻塞写请求并强制 Flush。一般不建议调整，但对于写入过快且内存充裕的场景，为避免写阻塞，可以适当调整到5~8。

- hbase.regionserver.global.memstore.size

默认值 0.4，RegionServer 中所有 MemStore 大小总和最多占 RegionServer 堆内存的 40%。这是写缓存的总比例，可以根据实际场景适当调整，且要与 HBase 读缓存参数 hfile.block.cache.size（默认也是0.4）配合调整。旧版本参数名称为 hbase.regionserver.global.memstore.upperLimit。

- hbase.regionserver.global.memstore.size.lower.limit

默认值 0.95，表示 RegionServer 中所有 MemStore 大小的低水位是 hbase.regionserver.global.memstore.size 的 95%，超过该比例就会强制 Flush。一般不建议调整。旧版本参数名称为 hbase.regionserver.global.memstore.lowerLimit。

- hbase.regionserver.optionalcacheflushinterval

默认值 3600000（即 1 小时），HBase 定期 Flush 所有 MemStore 的时间间隔。一般建议调大，比如 10 小时，因为很多场景下 1 小时 Flush 一次会产生很多小文件，一方面导致 Flush 比较频繁，另一方面导致小文件很多，影响随机读性能，因此建议设置较大值。

上面就是 Flush 的触发条件及核心参数，理解并适当调整参数有利于维护 HBase 集群的稳定性。

## Compaction 类型、触发时机和参数

从上面分析我们知道，HBase 会定期执行 Compaction 合并 HFile，**提升读性能**，其实就是以短时间内的io消耗，换取相对稳定的读取性能。

### Compaction类型

Compaction 分为两种：`Minor Compaction` 与 `Major Compaction`，可以称为小合并、大合并

![img](https://pic2.zhimg.com/v2-c64a1b6a8dfae910ec4db64abd955c61_r.jpg)

- Minor Compaction 是指选取一些小的、相邻的 HFile 将他们合并成一个更大的 HFile。默认情况下，Minor Compaction 会删除选取 HFile 中的 TTL 过期数据。

- Major Compaction 是指将一个 Store 中所有的 HFile 合并成一个 HFile，这个过程会清理三类没有意义的数据：被删除的数据（打了 Delete 标记的数据）、TTL 过期数据、版本号超过设定版本号的数据。另外，一般情况下，Major Compaction 时间会持续比较长，整个过程会消耗大量系统资源，对上层业务有比较大的影响。因此，生产环境下通常关闭自动触发 Major Compaction 功能，改为手动在业务低峰期触发。

### Compaction触发时机

概括的说，HBase 会在三种情况下检查是否要触发 Compaction，分别是 **MemStore Flush、后台线程周期性检查、手动触发**。

- MemStore Flush：可以说 Compaction 的根源就在于Flush，MemStore 达到一定阈值或触发条件就会执行 Flush 操作，在磁盘上生成 HFile 文件，正是因为 HFile 文件越来越多才需要 Compact。HBase 每次Flush 之后，都会判断是否要进行 Compaction，一旦满足 Minor Compaction 或 Major Compaction 的条件便会触发执行。

- 后台线程周期性检查： 后台线程 CompactionChecker 会定期检查是否需要执行 Compaction，检查周期为 `hbase.server.thread.wakefrequency * hbase.server.compactchecker.interval.multiplier`，这里主要考虑的是一段时间内没有写入仍然需要做 Compact 检查。其中参数 hbase.server.thread.wakefrequency 默认值 10000 即 10s，是 HBase 服务端线程唤醒时间间隔，用于 LogRoller、MemStoreFlusher 等的周期性检查；参数 `hbase.server.compactchecker.interval.multiplier `默认值1000，是 Compaction 操作周期性检查乘数因子，10 * 1000 s 时间上约等于2hrs, 46mins, 40sec。

- 手动触发：通过 HBase Shell、Master UI 界面或 HBase API 等任一种方式执行 compact、major_compact等命令，会立即触发 Compaction。

### Compaction 原理

HBase 自动合并的原理是通过周期性的合并小的 Region 来减少 Region 数量，从而达到负载均衡和优化查询性能的目的。自动合并主要包括两个阶段：合并选择和合并执行。

（1）合并选择

在合并选择阶段，HBase 会根据一定的策略选择需要合并的 Region。常用的策略有以下几种：

1. BySize：按照 Region 的大小选择。即选择相邻的两个大小较小的 Region 进行合并，使得合并后的 Region 大小接近设定的目标大小。
2. ByStoreFileCount：按照 Region 中 StoreFile 的数量选择。即选择相邻的两个 StoreFile 数量较少的 Region 进行合并，减少 Region 中 StoreFile 的个数。
3. ByRegionServer：按照 RegionServer 上的 Region 数量进行选择。即选择一个 RegionServer 上的两个相邻 Region 进行合并，使得 RegionServer 上的 Region 数量保持一定的平衡。

（2）合并执行

在合并执行阶段，HBase 会将选择的两个 Region 进行合并。合并过程包括以下步骤：

1. 将两个 Region 中的数据合并到一个新的 StoreFile 中。
2. 创建一个新的 HFile，保存合并后的数据。
3. 使用 Compaction 来合并 StoreFile，将合并后的数据写入 HDFS。
4. 更新 HBase 的元数据信息，包括删除旧的 Region、更新新的 Region。

HBase 提供了一些参数来配置自动合并的行为。下面是一些常用的参数：

- hbase.hstore.compaction.min：最小合并文件数，即一个 Region 中至少有多少个 StoreFile 才能触发合并，默认为 3。
- hbase.hstore.compaction.max：最大合并文件数，即一个 Region 中最多能有多少个 StoreFile，默认为 10。
- hbase.hstore.compaction.ratio：合并率，**即合并后的文件大小占原文件大小的比例，默认为 1.2**。
- hbase.hstore.compaction.throughput.lower.bound：合并下限，即合并速率的下限，默认为 8MB/s。
- hbase.hstore.compaction.throughput.higher.bound：合并上限，即合并速率的上限，默认为 32MB/s。

可以根据实际的需求来调整这些参数，以达到最佳的自动合并效果。

### Compaction 核心参数

和上面类似，这里总结了几个和 Compaction 有关的重要参数，并给出调整建议：

1、hbase.hstore.compaction.min

默认值 3，一个 Store 中 HFile 文件数量超过该阈值就会触发一次 Compaction（Minor Compaction），这里称该参数为 minFilesToCompact。一般不建议调小，重写场景下可以调大该参数，比如 5~10 之间，注意相应调整下一个参数。老版本参数名称为 hbase.hstore.compactionthreshold。

2、hbase.hstore.compaction.max

默认值 10，一次 Minor Compaction 最多合并的 HFile 文件数量，这里称该参数为 maxFilesToCompact。这个参数也控制着一次压缩的耗时。一般不建议调整，但如果上一个参数调整了，该参数也应该相应调整，一般设为 minFilesToCompact 的 2~3 倍。

3、hbase.regionserver.thread.compaction.throttle

HBase RegionServer 内部设计了两个线程池 large compactions 与 small compactions，用来分离处理 Compaction 操作，该参数就是控制一个 Compaction 交由哪一个线程池处理，默认值是 2 *maxFilesToCompact* hbase.hregion.memstore.flush.size（默认2*10*128M=2560M即2.5G），建议不调整或稍微调大。

4、hbase.regionserver.thread.compaction.large/small

默认值 1，表示 large compactions 与 small compactions 线程池的大小。一般建议调整到 2~5，不建议再调太大比如10，否则可能会消费过多的服务端资源造成不良影响。

5、hbase.hstore.blockingStoreFiles

默认值 10，表示一个 Store 中 HFile 文件数量达到该值就会阻塞写入，等待 Compaction 的完成。一般建议调大点，比如设置为 100，避免出现阻塞更新的情况，阻塞日志如下：

```text
too many store files; delaying flush up to 90000ms
```

> 生产环境建议认真根据实际业务量做好集群规模评估，如果小集群遇到了持续写入过快的场景，合理扩展集群也非常重要。

6、hbase.hregion.majorcompaction

默认值604800000ms 即7天，这是 Major Compaction 周期性触发执行的时间间隔。通常 Major Compaction 持续时间较长、资源消耗较大，一般设为 0，表示关闭自动触发，建议在业务低峰期时手动执行。