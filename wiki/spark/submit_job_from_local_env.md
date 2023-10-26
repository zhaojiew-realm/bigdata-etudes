## Error and solution

(1) jdk version difference

> error: local class incompatible: stream classdesc serialVersionUID = [-2897844985684768944, local class serialVersionUID = 7350468743759137184]()

The jdk version may not be the same.

For example, I use corrote8 in spark server and openjdk8 in client env.

In addition, we need to ensure client packages' version  the same as the dependencies in spark server(spark and scala)

(2) Lambda function not support in spark env
- https://blog.csdn.net/hotdust/article/details/61671448
> error: java.lang.ClassCastException: cannot assign instance of java.lang.invoke.SerializedLambda to field

In addition, we need to repackage ensure use new configuration