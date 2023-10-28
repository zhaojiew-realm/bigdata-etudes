# Example uses GDELT dataset found here: https://aws.amazon.com/public-datasets/gdelt/
# Column headers found here: http://gdeltproject.org/data/lookups/CSV.header.dailyupdates.txt

# Load RDD
lines = sc.textFile("s3://gdelt-open-data/events/2016*") # Loads 73,385,698 records from 2016
# Split lines into columns; change split() argument depending on deliminiter e.g. '\t'
parts = lines.map(lambda l: l.split('\t'))
# Convert RDD into DataFrame
from urllib import urlopen
html = urlopen("http://gdeltproject.org/data/lookups/CSV.header.dailyupdates.txt").read().rstrip()
columns = html.split('\t')
df = spark.createDataFrame(parts, columns)