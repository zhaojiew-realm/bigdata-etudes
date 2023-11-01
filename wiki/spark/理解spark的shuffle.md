参考资料

- https://zhuanlan.zhihu.com/p/107087969
- [Spark的Shuffle的四种机制以及参数调优](https://blog.csdn.net/qichangjian/article/details/88039576)

在MapReduce框架，Shuffle是连接Map和Reduce之间的桥梁，Map阶段通过shuffle读取数据并输出到对应的Reduce，而Reduce阶段负责从Map端拉取数据并进行计算。在**整个shuffle过程中，往往伴随着大量的磁盘和网络I/O**。所以shuffle性能的高低也直接决定了整个程序的性能高低，而Spark也会有自己的shuffle实现过程。

在DAG调度的过程中，**Stage** 阶段的划分是根据是否有shuffle过程，也就是存在 **宽依赖** 的时候,需要进行shuffle,这时候会将 **job** 划分成多个Stage，每一个 Stage 内部有很多可以并行运行的 **Task**。

stage与stage之间的过程就是 shuffle 阶段，在 Spark 中，负责 shuffle 过程的执行、计算和处理的组件主要就是 **ShuffleManager** 。ShuffleManager 随着Spark的发展有两种实现的方式，分别为 **HashShuffleManager** 和 **SortShuffleManager** ，因此spark的Shuffle有 `Hash Shuffle` 和 `Sort Shuffle` 两种。

## HashShuffle机制

在 Spark 1.2 以前，默认的shuffle计算引擎是 `HashShuffleManager `。

`HashShuffleManager`有着一个非常严重的弊端，就是会产生大量的中间磁盘文件，进而由大量的磁盘IO操作影响了性能。因此在Spark 1.2以后的版本中，默认的 `ShuffleManager`改成了 **SortShuffleManager** 。

`SortShuffleManager` 相较于 `HashShuffleManager` 来说，有了一定的改进。主要就在于每个Task在进行shuffle操作时，虽然也会产生较多的临时磁盘文件，但是**最后会将所有的临时文件合并(merge)成一个磁盘文件，因此每个 Task 就只有一个磁盘文件**。在下一个 Stage 的shuffle read task拉取自己的数据时，只要**根据索引读取每个磁盘文件中的部分数据即可**。

Hash shuffle是不具有排序的Shuffle。

### 普通机制的Hash shuffle

HashShuffleManager的运行机制主要分成两种：**一种是普通运行机制 ，另一种是合并运行机制** ，而合并机制主要是通过复用buffer来优化Shuffle过程中产生的小文件的数量。

![image-20231028165754986](C:\Users\Administrator\Desktop\wiki\spark\assets\image-20231028165754986.png)

图中有3个ReduceTask，从ShuffleMapTask 开始那边各自把自己进行 Hash 计算(分区器：hash/numReduce取模)，分类出3个不同的类别，每个 ShuffleMapTask 都分成3种类别的数据，想把不同的数据汇聚然后计算出最终的结果，所以ReduceTask 会在属于自己类别的数据收集过来，汇聚成一个同类别的大集合，每1个 ShuffleMapTask 输出3份本地文件，这里有4个 ShuffleMapTask，所以总共输出了4 x 3个分类文件 = 12个本地小文件

**Shuffle Write** 阶段：

主要就是在一个stage结束计算之后，为了下一个stage可以执行shuffle类的算子，而将每个task处理的数据按key进行分区。**所谓 “分区”，就是对相同的key执行hash算法，从而将相同key都写入同一个磁盘文件中，而每一个磁盘文件都只属于reduce端的stage的一个task**。在将数据写入磁盘之前，会先将数据写入内存缓冲中，当内存缓冲填满之后，才会溢写到磁盘文件中去。

那么每个执行 Shuffle Write 的 Task，要为下一个 Stage 创建多少个磁盘文件呢? 很简单，下一个stage的task有多少个，当前stage的每个task就要创建多少份磁盘文件。由此可见，**未经优化的shuffle write操作所产生的磁盘文件的数量是极其惊人的。**

**Shuffle Read** 阶段：

Shuffle Read，通常就是一个stage刚开始时要做的事情。此时该stage的每一个task就需要将上一个stage的计算结果中的所有相同key，从各个节点上通过网络都拉取到自己所在的节点上，然后进行key的聚合或连接等操作。由于shuffle write的过程中，task给Reduce端的stage的每个task都创建了一个磁盘文件，因此shuffle read的过程中，每个task只要从上游stage的所有task所在节点上，拉取属于自己的那一个磁盘文件即可。

Shuffle Read的拉取过程是一边拉取一边进行聚合的。每个shuffle read task都会有一个自己的buffer缓冲，每次都只能拉取与buffer缓冲相同大小的数据，然后通过内存中的一个Map进行聚合等操作。聚合完一批数据后，再拉取下一批数据，并放到buffer缓冲中进行聚合操作。以此类推，直到最后将所有数据到拉取完，并得到最终的结果。

注意：

1. buffer起到的是缓存作用，缓存能够加速写磁盘，提高计算的效率,buffer的默认大小32k。
2. 分区器：根据hash/numRedcue取模决定数据由几个Reduce处理，也决定了写入几个buffer中
3. block file：磁盘小文件，从图中我们可以知道磁盘小文件的个数计算公式：block file=M*R 。 M为map task的数量，R为Reduce的数量，一般Reduce的数量等于buffer的数量，都是由分区器决定的

**Hash shuffle普通机制的问题**：

1. Shuffle阶段在磁盘上会产生海量的小文件，建立通信和拉取数据的次数变多,此时会产生大量耗时低效的 IO 操作 (因为产生过多的小文件)
2. 可能导致 OOM，大量耗时低效的 IO 操作 ，导致写磁盘时的对象过多，读磁盘时候的对象也过多，这些对象存储在堆内存中，会导致堆内存不足，相应会导致频繁的GC，GC会导致OOM。由于内存中需要保存海量文件操作句柄和临时信息，如果数据处理的规模比较庞大的话，内存不可承受，会出现 OOM 等问题

### Consolidate机制的Hash shuffle

- [Shuffle执行原理与Consolidate机制概述](https://blog.51cto.com/u_15015181/2556472)

合并机制就是复用buffer缓冲区，开启合并机制的配置是`spark.shuffle.consolidateFiles`。该参数默认值为false，将其设置为true即可开启优化机制。通常来说，如果我们使用HashShuffleManager，那么都建议开启这个选项。

![image-20231028165804499](C:\Users\Administrator\Desktop\wiki\spark\assets\image-20231028165804499.png)

这里有6个这里有6个shuffleMapTask，数据类别还是分成3种类型，因为Hash算法会根据你的 Key 进行分类，在同一个进程中，无论是有多少过Task，都会把同样的Key放在同一个Buffer里，然后把Buffer中的数据写入以Core数量为单位的本地文件中，(一个Core只有一种类型的Key的数据)，每1个Task所在的进程中，分别写入共同进程中的3份本地文件，这里有6个shuffleMapTasks，所以总共输出是 2个Cores x 3个分类文件 = 6个本地小文件。

此时block file = Core * R ，Core为CPU的核数，R为Reduce的数量，但是如果 Reducer 端的并行任务或者是数据分片过多的话则 Core * Reducer Task 依旧过大，也会产生很多小文件。

## Sort shuffle

SortShuffleManager的运行机制也是主要分成两种，**普通运行机制** 和 **bypass运行机制**

### 普通运行模式

![image-20231028165813527](C:\Users\Administrator\Desktop\wiki\spark\assets\image-20231028165813527.png)

在该模式下，数据会先写入一个数据结构，聚合算子写入Map，一边通过 Map 局部聚合，一遍写入内存。Join 算子写入 ArrayList 直接写入内存中。然后需要**判断是否达到阈值（5M）**，如果达到就会将内存数据结构的数据写入到磁盘，清空内存数据结构。

在溢写磁盘前，先根据 key 进行排序，排序过后的数据，**会分批写入到磁盘文件中。默认批次为10000条**，数据会以每批一万条写入到磁盘文件。写入磁盘文件通过缓冲区溢写的方式，每次溢写都会产生一个磁盘文件，也就是说一个task过程会产生多个临时文件。

最后在每个task中，将所有的临时文件合并，这就是 merge 过程，此过程将所有临时文件读取出来，一次写入到最终文件。意味着一个task的所有数据都在这一个文件中。同时单独写一份索引文件，标识下游各个task的数据在文件中的索引start offset和end offset

这个机制的好处：

1. 小文件明显变少了，一个task只生成一个file文件
2. file文件整体有序，加上索引文件的辅助，查找变快，虽然排序浪费一些性能，但是查找变快很多

### bypass模式的sortShuffle

bypass机制运行条件是**shuffle map task数量小于spark.shuffle.sort.bypassMergeThreshold参数（默认值200）的值，且不是聚合类的shuffle算子（比如reduceByKey）**

![image-20231028165824235](C:\Users\Administrator\Desktop\wiki\spark\assets\image-20231028165824235.png)

在 shuffleMapTask 数量 **小于默认值200** 时，启用bypass模式的 sortShuffle，并没有进行sort，原因是数据量本身比较少，没必要进行sort全排序，因为数据量少本身查询速度就快，正好省了sort的那部分性能开销。

## 相关参数

1.5.1 spark.shuffle.file.buffer

buffer大小默认是32K，为了减少磁盘溢写的次数，可以适当调整这个数值的大小。降低磁盘IO

1.5.2 spark.reducer.MaxSizeFlight

ReduceTask 拉取数据量的大小，默认48M

1.5.3 spark.shuffle.memoryFraction

shuffle聚合内存的比例，占用executor内存比例的大小

1.5.4 spark.shuffle.io.maxRetries

拉取数据重试次数，防止网络抖动带来的影响

1.5.5 spark.shuffle.io.retryWait

调整到重试间隔时间，拉取失败后多久才重新进行拉取

1.5.6 spark.shuffle.consolidateFiles

针对 HashShuffle 合并机制

1.5.7 spark.shuffle.sort.bypassMergeThreshold

SortShuffle bypass机制，默认200次

1.5.8 spark.sql.shuffle.partitions

默认200，shuffle时所使用到的分区数，也就是你生成的 part-00000，part-00001···最多也就只能 part-00199 了