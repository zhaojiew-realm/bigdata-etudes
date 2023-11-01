package org.example.ops;

import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

public class HBaseDDL {
    public static Connection connection = HBaseConnectFactory.connection;

    public static void createNamespace(String namespace) throws IOException {

        Admin admin = connection.getAdmin();

        NamespaceDescriptor.Builder builder = NamespaceDescriptor.create(namespace);

        builder.addConfiguration("user", "tuser");

        try {
            admin.createNamespace(builder.build());
        } catch (IOException e) {
            System.out.println("命令空间已经存在");
            e.printStackTrace();
        }
        admin.close();
    }

    public static boolean isTableExists(String namespace, String tableName) throws IOException {

        Admin admin = connection.getAdmin();

        boolean b = false;
        try {
            b = admin.tableExists(TableName.valueOf(namespace, tableName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        admin.close();

        return b;
    }

    public static void createTable(String namespace, String tableName, String... columnFamilies) throws IOException {
        if (columnFamilies.length == 0) {
            System.out.println("创建表格至少有一个列族");
            return;
        }

        if (isTableExists(namespace, tableName)) {
            System.out.println("表格已经存在");
            return;
        }

        Admin admin = connection.getAdmin();

        TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder.newBuilder(TableName.valueOf(namespace, tableName));

        for (String columnFamily : columnFamilies) {
            ColumnFamilyDescriptorBuilder columnFamilyDescriptorBuilder = ColumnFamilyDescriptorBuilder.newBuilder(Bytes.toBytes(columnFamily));
            columnFamilyDescriptorBuilder.setMaxVersions(5);
            tableDescriptorBuilder.setColumnFamily(columnFamilyDescriptorBuilder.build());
        }

        try {
            admin.createTable(tableDescriptorBuilder.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
// 3. 关闭 admin
        admin.close();
    }

    public static void modifyTable(String namespace, String tableName, String columnFamily, int version) throws IOException {
// 判断表格是否存在
        if (!isTableExists(namespace, tableName)) {
            System.out.println("表格不存在无法修改");
            return;
        }
// 1. 获取 admin
        Admin admin = connection.getAdmin();
        try {
// 2. 调用方法修改表格
// 2.0 获取之前的表格描述
            TableDescriptor descriptor = admin.getDescriptor(TableName.valueOf(namespace, tableName));

            TableDescriptorBuilder tableDescriptorBuilder = TableDescriptorBuilder.newBuilder(descriptor);

            ColumnFamilyDescriptor columnFamily1 = descriptor.getColumnFamily(Bytes.toBytes(columnFamily));

            ColumnFamilyDescriptorBuilder columnFamilyDescriptorBuilder = ColumnFamilyDescriptorBuilder.newBuilder(columnFamily1);

            columnFamilyDescriptorBuilder.setMaxVersions(version);

            tableDescriptorBuilder.modifyColumnFamily(columnFamilyDescriptorBuilder.build());
            admin.modifyTable(tableDescriptorBuilder.build());
        } catch (IOException e) {
            e.printStackTrace();
        }

        admin.close();
    }

    public static boolean deleteTable(String namespace, String tableName) throws IOException {
// 1. 判断表格是否存在
        if (!isTableExists(namespace, tableName)) {
            System.out.println("表格不存在 无法删除");
            return false;
        }
// 2. 获取 admin
        Admin admin = connection.getAdmin();
// 3. 调用相关的方法删除表格
        try {
            TableName tableName1 = TableName.valueOf(namespace, tableName);
            admin.disableTable(tableName1);
            admin.deleteTable(tableName1);
        } catch (IOException e) {
            e.printStackTrace();
        }
// 4. 关闭 admin
        admin.close();
        return true;
    }

    public static void main(String[] args) {

        HBaseDDL hclient = new HBaseDDL();
        try {
            hclient.createNamespace("testns");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
