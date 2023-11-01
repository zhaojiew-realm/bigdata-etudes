package org.example.ops;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.CompareOperator;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.ColumnValueFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;

import java.io.IOException;

public class HBaseDML {

    public static Connection connection = HBaseConnectFactory.connection;

    public static void putCell(String namespace, String tableName, String rowKey, String columnFamily, String columnName, String value) throws IOException {

        Table table = connection.getTable(TableName.valueOf(namespace, tableName));

        Put put = new Put(Bytes.toBytes(rowKey));

        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), Bytes.toBytes(value));

        try {
            table.put(put);
        } catch (IOException e) {
            e.printStackTrace();
        }
        table.close();
    }

    public static void getCells(String namespace, String tableName, String rowKey, String columnFamily, String columnName) throws IOException {
// 1. 获取 table
        Table table = connection.getTable(TableName.valueOf(namespace, tableName));
// 2. 创建 get 对象
        Get get = new Get(Bytes.toBytes(rowKey));
// 如果直接调用 get 方法读取数据 此时读一整行数据
// 如果想读取某一列的数据 需要添加对应的参数
        get.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName));
// 设置读取数据的版本
        get.readAllVersions();
        try {
// 读取数据 得到 result 对象
            Result result = table.get(get);
// 处理数据
            Cell[] cells = result.rawCells();
// 测试方法: 直接把读取的数据打印到控制台
// 如果是实际开发 需要再额外写方法 对应处理数据
            for (Cell cell : cells) {
// cell 存储数据比较底层
                String value = new String(CellUtil.cloneValue(cell));
                System.out.println(value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
// 关闭 table
        table.close();
    }

    public static void scanRows(String namespace, String tableName,
                                String startRow, String stopRow) throws IOException {
        // 1. 获取 table
        Table table = connection.getTable(TableName.valueOf(namespace, tableName));
// 2. 创建 scan 对象
        Scan scan = new Scan();
// 如果此时直接调用 会直接扫描整张表
// 添加参数 来控制扫描的数据
// 默认包含
        scan.withStartRow(Bytes.toBytes(startRow));
// 默认不包含
        scan.withStopRow(Bytes.toBytes(stopRow));
        try {
// 读取多行数据 获得 scanner
            ResultScanner scanner = table.getScanner(scan);
// result 来记录一行数据 cell 数组
// ResultScanner 来记录多行数据 result 的数组
            for (Result result : scanner) {
                Cell[] cells = result.rawCells();
                for (Cell cell : cells) {
                    System.out.print(new
                            String(CellUtil.cloneRow(cell)) + "-" + new
                            String(CellUtil.cloneFamily(cell)) + "-" + new
                            String(CellUtil.cloneQualifier(cell)) + "-" + new
                            String(CellUtil.cloneValue(cell)) + "\t");
                }
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
// 3. 关闭 table
        table.close();
    }

    public static void filterScan(String namespace, String tableName,
                                  String startRow, String stopRow, String columnFamily, String
                                          columnName, String value) throws IOException {
// 1. 获取 table
        Table table =
                connection.getTable(TableName.valueOf(namespace, tableName));
// 2. 创建 scan 对象
        Scan scan = new Scan();
// 如果此时直接调用 会直接扫描整张表
// 添加参数 来控制扫描的数据
// 默认包含
        scan.withStartRow(Bytes.toBytes(startRow));
// 默认不包含
        scan.withStopRow(Bytes.toBytes(stopRow));
// 可以添加多个过滤
        FilterList filterList = new FilterList();
// 创建过滤器
// (1) 结果只保留当前列的数据
        ColumnValueFilter columnValueFilter = new ColumnValueFilter(
// 列族名称
                Bytes.toBytes(columnFamily),
// 列名
                Bytes.toBytes(columnName),
// 比较关系
                CompareOperator.EQUAL,
// 值
                Bytes.toBytes(value)
        );
// (2) 结果保留整行数据
// 结果同时会保留没有当前列的数据
        SingleColumnValueFilter singleColumnValueFilter = new SingleColumnValueFilter(
// 列族名称
                Bytes.toBytes(columnFamily),
// 列名
                Bytes.toBytes(columnName),
// 比较关系
                CompareOperator.EQUAL,
// 值
                Bytes.toBytes(value)
        );
// 本身可以添加多个过滤器
        filterList.addFilter(singleColumnValueFilter);
        scan.setFilter(filterList);
        try {
// 读取多行数据 获得 scanner
            ResultScanner scanner = table.getScanner(scan);
// result 来记录一行数据 cell 数组
// ResultScanner 来记录多行数据 result 的数组
            for (Result result : scanner) {
                Cell[] cells = result.rawCells();
                for (Cell cell : cells) {
                    System.out.print(new
                            String(CellUtil.cloneRow(cell)) + "-" + new
                            String(CellUtil.cloneFamily(cell)) + "-" + new
                            String(CellUtil.cloneQualifier(cell)) + "-" + new
                            String(CellUtil.cloneValue(cell)) + "\t");
                }
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
// 3. 关闭 table
        table.close();
    }

    public static void deleteColumn(String nameSpace, String tableName,
                                    String rowKey, String family, String column) throws IOException {
// 1.获取 table
        Table table = connection.getTable(TableName.valueOf(nameSpace,
                tableName));
// 2.创建 Delete 对象
        Delete delete = new Delete(Bytes.toBytes(rowKey));
// 3.添加删除信息
// 3.1 删除单个版本
//
        delete.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));

        // 3.2 删除所有版本
        delete.addColumns(Bytes.toBytes(family),
                Bytes.toBytes(column));
// 3.3 删除列族
// delete.addFamily(Bytes.toBytes(family));
// 3.删除数据
        table.delete(delete);
// 5.关闭资源
        table.close();
    }

    public static void main(String[] args) {

    }
}
