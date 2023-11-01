参考资料

- https://docs.fedoraproject.org/en-US/quick-docs/postgresql/
- https://wiki.archlinux.org/title/PostgreSQL
- https://www.runoob.com/postgresql/linux-install-postgresql.html

## 安装和配置

安装pgsql

```
yum install postgresql-server
```

设置密码

```
sudo passwd postgres
```

初始化数据库

> https://www.postgresql.org/docs/current/creating-cluster.html

- 可见默认的数据库创建在`/var/lib/pgsql`
- 创建了配置文件`postgresql.conf`和`pg_hba.conf`

```shell
postgresql-setup --initdb
ERROR: The /var/lib/pgsql directory has wrong permissions.
       Please make sure the directory is writable by ec2-user.
       
// 初始化之后       
# ls
backups  data  initdb_postgresql.log
```

启动数据库

```
sudo systemctl start postgresql
```

切换用户

```
su - postgres
```

连接

```
psql
```

初始的3个表

```
postgres=# \l
                                  List of databases
   Name    |  Owner   | Encoding |   Collate   |    Ctype    |   Access privileges
-----------+----------+----------+-------------+-------------+-----------------------
 postgres  | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 |
 template0 | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 | =c/postgres          +
           |          |          |             |             | postgres=CTc/postgres
 template1 | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 | =c/postgres          +
           |          |          |             |             | postgres=CTc/postgres
(3 rows)
```

创建database

```
createdb mydb
```

配置远程连接

- 修改`pg_hba.conf`

```
# IPv4 local connections:
host    all             all             127.0.0.1/32            ident
host    all             all             0.0.0.0/0            trust
```

- 编辑`postgresql.conf`

  ```
  # - Connection Settings -
  
  listen_addresses = '*'          # what IP address(es) to listen on;
  ```

- 开启数据库时host为服务器ip地址，port默认为5432

## 基础概念

> https://blog.csdn.net/qq_35462323/article/details/112623388

集群，数据库和表

![图片](https://img-blog.csdnimg.cn/img_convert/34afa89460d2ccace813aa8d1ebca7db.png)

- 使用`initdb`的命令去初始化一个新的数据库集群。-D参数，通过它来**指定应该存储数据库集群的目录**

- 表、数据库这些数据库对象，还有比如索引、视图、函数、序列等对象，pg对这些对象**统一采用对象标识符（OIDs）来管理**，oid是无符号的4字节整数

  ```
  select datname,oid from pg_database where datname in ('postgres','template0','template1','testextenddb');
  ```

- 集群中的`database`对应`$PGDATA/base`目录下的一个文件夹

  ![图片](https://img-blog.csdnimg.cn/img_convert/ef5d05891ef5440c3a2363bb8f3c7ed5.png)