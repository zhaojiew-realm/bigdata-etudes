## 错误和解决

### jdk version difference

> error: local class incompatible: stream classdesc serialVersionUID = [-2897844985684768944, local class serialVersionUID = 7350468743759137184]()

jdk版本必须一致，例如，我在 park server 中使用aws java8，在客户端 env 中使用 openjdk8

此外，我们需要确保客户端软件包的版本与spark server中的依赖项(spark和 Scala)相同

### Lambda function在spark env中不支持

- https://blog.csdn.net/hotdust/article/details/61671448
> error: java.lang.ClassCastException: cannot assign instance of java.lang.invoke.SerializedLambda to field

此外，我们需要重新打包，以确保使用新的配置