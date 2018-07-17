#!/bin/bash

# source kerberos
source /home/sankuai/bin/kerberos_setup.sh \

# set parameters
yesterday=$(date +%Y%m%d --date="1 day ago")

# add external_table partition
/opt/meituan/hive/bin/beeline -u "jdbc:hive2://rz-data-hive-hiveserver01:10000/ns_hotel;principal=hive/rz-data-hive-hiveserver01.rz.sankuai.com@SANKUAI.COM" -e "use ns_hotel;" -e "ALTER TABLE mining_rec_selrec_service add if not exists partition(dt=$yesterday);"

# remove duplicated file
/opt/meituan/hadoop-2.4.1-client/bin/hdfs dfs -rm -r /user/hive/warehouse/ns_hotel.db/rec_hotel_poi_feature/dt=${yesterday} \

# author: jiangweisen@meituan.com
# spark submit
/opt/meituan/spark-1.1/bin/spark-submit \
        --class com.meituan.hbdata.rec.feature.builder.HotelPoiDynamicFeatureBuilder \
        --num-executors 200 \
        --driver-memory 8g \
        --executor-memory 8g \
        --executor-cores 8 \
        --master yarn-cluster \
        --conf spark.yarn.executor.memoryOverhead=4096 \
        --conf spark.default.parallelism=360 \
        --conf spark.serializer=org.apache.spark.serializer.KryoSerializer \
        --conf spark.rdd.compress=true \
        --conf spark.akka.frameSize=100 \
        --queue root.hadoop-hotel.data_cp \
        /opt/meituan/jiangweisen/jar/hbdata-rec-feature-center-1.0-SNAPSHOT-jar-with-dependencies.jar \
        outputPath=/user/hive/warehouse/ns_hotel.db/rec_hotel_poi_feature