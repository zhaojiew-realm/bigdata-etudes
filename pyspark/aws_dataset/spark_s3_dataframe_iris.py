# Loads RDD
lines = sc.textFile("s3://jakechenaws/tutorials/sample_data/iris/iris.csv")
# Split lines into columns; change split() argument depending on deliminiter e.g. '\t'
parts = lines.map(lambda l: l.split(','))
# Convert RDD into DataFrame
df = spark.createDataFrame(parts, ['sepal_length','sepal_width','petal_length','petal_width','class'])