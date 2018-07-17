#!/bin/bash

########################################################
# 准备工作：
# 将当前demo目录，拷贝到data-open-hadoop02机器上
# （data-open-hadoop02部署了SPARK CLIENT）
########################################################

source /home/adhocmobile/bin/kerberos_setup.sh

HADOOP_BIN="/opt/local/hadoop/bin/hadoop"
SPARK_SUBMIT="/opt/meituan/spark-1.1/bin/spark-submit"
MAIN_CLASS="com.meituan.dataapp.gbdt.GBDTTool"

JAR_FILE="gbdt-spark_2.10-1.0.jar"
NAME_NODE="hdfs://hadoop-meituan"

########################################################
# Data Preparation
########################################################

train_file=/user/hadoop-dataapp/tmp/train.data
test_file=/user/hadoop-dataapp/tmp/test.data

$HADOOP_BIN fs -rm $train_file
$HADOOP_BIN fs -rm $test_file

python build_data.py > train.data
python build_data.py > test.data

$HADOOP_BIN fs -put train.data $train_file
$HADOOP_BIN fs -put test.data $test_file

########################################################
# Model Training
########################################################

dim=3                     # 特征维数
depth=4                   # 树深度
iterations=100            # 树棵数
shrinkage=0.15            # 衰减
feature_ratio=0.7         # 特征采样比例
data_ratio=0.4            # 数据采样比例
initial_guess=false       # 是否应用初始值
debug=true                # 是否打印debug信息（如果打印，训练时间会变长）
loss="SquaredError"       # loss function类型（可选：LogLikelihood，SquaredError）
max_bins=30               # 越大，单棵树拟合越好，训练也越慢，一般20~50即可
hasInitValue=false        # 是否训练样本包含初始值

model_path="$NAME_NODE/user/hadoop-dataapp/tmp/demo.model"

$HADOOP_BIN fs -rmr $model_path

cmd="$SPARK_SUBMIT \
        --class $MAIN_CLASS \
        --num-executors 20 \
        --driver-memory 4g \
        --executor-memory 4g \
        --executor-cores 4 \
        --master yarn-cluster \
        --queue root.hadoop-dataapp.mining \
        $JAR_FILE \
        dim=$dim \
        depth=$depth \
        iterations=$iterations \
        shrinkage=$shrinkage \
        fratio=$feature_ratio \
        dratio=$data_ratio \
        init=$initial_guess \
        debug=$debug \
        loss=$loss \
        trainfile=${NAME_NODE}$train_file \
        testfile=${NAME_NODE}$test_file \
        maxBins=$max_bins \
        modelPath=$model_path \
        hasInitValue=$hasInitValue"
echo $cmd
$cmd
$HADOOP_BIN fs -getmerge $model_path ./demo.model
$HADOOP_BIN fs -getmerge ${model_path}.stat ./demo.model.stat


########################################################
# Evaluation
########################################################
MAIN_CLASS="com.meituan.dataapp.gbdt.GBDTEvaluate"
predict_output="$NAME_NODE/user/hadoop-dataapp/tmp/predict"

$HADOOP_BIN fs -rmr $predict_output

cmd="$SPARK_SUBMIT \
        --class $MAIN_CLASS \
        --num-executors 20 \
        --driver-memory 4g \
        --executor-memory 4g \
        --executor-cores 4 \
        --master yarn-cluster \
        --queue root.hadoop-dataapp.mining \
        $JAR_FILE \
        numberOfFeature=$dim \
        loss=$loss \
        testPath=${NAME_NODE}$test_file \
        modelPath=$model_path \
        outputPath=$predict_output \
        enableInitialGuess=$initial_guess \
        hasInitValue=$hasInitValue"
echo $cmd
$cmd

$HADOOP_BIN fs -getmerge $predict_output ./predict
