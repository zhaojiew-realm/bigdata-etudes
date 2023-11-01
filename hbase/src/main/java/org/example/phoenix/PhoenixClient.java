package org.example.phoenix;

import java.sql.*;
import java.util.Properties;

public class PhoenixClient {
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:phoenix:master:2181";

        Properties properties = new Properties();

        Connection connection = DriverManager.getConnection(url, properties);

        PreparedStatement preparedStatement = connection.prepareStatement("select * from student");

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            System.out.println(resultSet.getString(1) + ":" +
                    resultSet.getString(2) + ":" + resultSet.getString(3));
        }

        connection.close();
        // 由于 Phoenix 框架内部需要获取一个 HBase 连接,所以会延迟关闭
        // 不影响后续的代码执行
        System.out.println("hello");

    }
}
