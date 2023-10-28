## 使用方法

配置mysql

```properties
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://hadoop102:3306/gmall?useUni
code=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8
spring.datasource.username=root
spring.datasource.password=123456
```

运行mock

```shell
java -jar ./gmall2020-mock-db-2021-11-14.jar
```

