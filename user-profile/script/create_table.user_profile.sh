#!/bin/bash

# author: hehuihui@meituan.com
# date: 2015/07/25 13:30:17


db_name="ns_hotel"
table_name="user_profile_hotel"

source ~/bin/kerberos_setup.sh

/opt/meituan/hive/bin/beeline -u "jdbc:hive2://data-etltest01:10000/${db_name};principal=hive/data-etltest01.lf.sankuai.com@SANKUAI.COM" -e "drop table if exists ${db_name}.${table_name}; "

/opt/meituan/hive/bin/beeline \
        -u "jdbc:hive2://data-etltest01:10000/${db_name};principal=hive/data-etltest01.lf.sankuai.com@SANKUAI.COM" \
        -e "create external table if not exists ${db_name}.${table_name} \
        ( \
			userid INT COMMENT '', \
			price STRING COMMENT '价格属性-json', \
			roomtype STRING COMMENT '房型属性-json', \
			hourroom STRING COMMENT '入住类型-json', \
			hoteltype STRING COMMENT '酒店类型-json', \
			last_pay_time STRING COMMENT '最近支付时间', \
			brand STRING COMMENT '品牌偏好' 
		) COMMENT '用户画像-酒店属性' \
		ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' LINES TERMINATED BY '\n' \
		LOCATION '/user/hadoop-hoteldata/hehuihui/user-profile/merge';"

