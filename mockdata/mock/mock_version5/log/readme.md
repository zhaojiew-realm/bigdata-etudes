## 使用方法

共有3种目标

```yaml
#模拟数据发送模式
#mock.type: "log"
mock.type: "http"
#mock.type: "kafka"

#http模式下，发送的地址
mock.url: "http://localhost:8090/applog"

mock:
  kafka-server: "hdp1:9092,hdp2:9092,hdp3:9092"
  kafka-topic: "ODS_BASE_LOG"
```

日志配置如下

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_HOME" value="/tmp/applog" />
    <appender name="console" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%msg%n</pattern>
        </encoder>
    </appender>

    <appender name="rollingFile" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_HOME}/app.%d{yyyy-MM-dd}.log</fileNamePattern>
        </rollingPolicy>
        <encoder>
            <pattern>%msg%n</pattern>
        </encoder>
    </appender>
    <!-- 将某一个包下日志单独打印日志 -->
    <logger name="com.atguigu.gmall2020.mock.log.util.LogUtil"
            level="INFO" additivity="false">
        <appender-ref ref="rollingFile" />
         <appender-ref ref="console" />
    </logger>

    <root level="error"  >
        <appender-ref ref="console" />
        <!-- <appender-ref ref="async-rollingFile" />  -->
    </root>
</configuration>
```

运行mock

```shell
Java -jar gmall2020-mock-log-2021-10-10.jar
```

