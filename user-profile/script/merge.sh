#!/bin/bash
# author: hehuihui@meituan.com
# date: 2015-07-21

source ./conf.sh

jobname="hehuihui_user_profile_$0"
map_script='merge.map.py'
reduce_script='merge.reduce.py'
f_deal_room=$data_dir/deal_room

input_dir="/user/hive/warehouse/ba_hotel.db/fact_orderpayment_all/*/*"
output_dir="/user/hadoop-hoteldata/hehuihui/user-profile/merge"

${HADOOP} fs -rmr $output_dir
${HADOOP_STREAM} \
	-jobconf mapred.job.name=$jobname \
	-jobconf mapred.job.queue.name=${HADOOP_QUEUE} \
	-jobconf mapred.job.priority="VERY_HIGH" \
	-jobconf stream.memory.limit=2048 \
	-jobconf mapred.reduce.tasks=20 \
	-input $input_dir \
	-output $output_dir \
	-mapper "$python $(basename $map_script)" \
	-reducer "$python $(basename $reduce_script) $(basename $f_deal_room)" \
	-file $map_script \
	-file $reduce_script \
	-file $f_deal_room

if [ $? -ne 0 ]
then
	echo "[Haodop Error]: job $jobname failed!"
	exit -1
fi
