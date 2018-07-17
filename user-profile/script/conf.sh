#!/bin/bash

# author: hehuihui@meituan.com
# date: 2015-07-20

this_dir=$(dirname $0)
if [[ $this_dir =~ "." ]]; then
	this_dir=$(pwd)
fi
root_dir=$(dirname $this_dir)
data_dir="$root_dir/data"
log_dir="$root_dir/log"

today=$(date +"%Y%m%d")

HADOOP="/opt/meituan/hadoop-2.4.1-client/bin/hadoop"
HADOOP_STREAM="${HADOOP} jar /opt/meituan/hadoop/share/hadoop/tools/lib/hadoop-streaming-2.2.0.jar streamjob"
HADOOP_QUEUE="root.hadoop-hotel.data"

python='/usr/bin/python'

