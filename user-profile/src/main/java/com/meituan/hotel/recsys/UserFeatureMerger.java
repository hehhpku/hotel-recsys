package com.meituan.hotel.recsys;

/**
 * UserFeatureMerger.java
 *
 * @author hehuihui@meituan.com
 * @date 2015-09-01
 * @brief
 */


import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.concurrent.Exchanger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import org.apache.hive.hcatalog.mapreduce.HCatInputFormat;
import org.apache.hive.hcatalog.mapreduce.HCatMultipleInputs;
import com.meituan.hadoop.SecurityUtils;

import org.apache.hive.hcatalog.data.HCatRecord;
import org.apache.hive.hcatalog.data.schema.HCatSchema;
import org.apache.hive.hcatalog.mapreduce.HCatSplit;

public class UserFeatureMerger extends Configured implements Tool {

    enum LOCATION {
        LOCATION_TYPE_HOME,
        LOCATION_TYPE_WORK
    }

    private static final String LOCATION_HOME = "home_location";
    private static final String LOCATION_WORK = "work_location";

    public static class user_hotel_profile_mapper extends Mapper<WritableComparable, HCatRecord, Text, Text> {
        @Override
        public void map(WritableComparable key, HCatRecord value, Context context)
                throws IOException, InterruptedException
        {
            try {
                HCatSplit split = (HCatSplit) context.getInputSplit();
                HCatSchema recordSchema = split.getTableSchema();

                Integer userid = value.getInteger("userid", recordSchema);
                String price  = value.getString("price", recordSchema);
                String brand = value.getString("brand", recordSchema);
                String hourroom = value.getString("hourroom", recordSchema);
                String roomtype = value.getString("roomtype", recordSchema);
                String hoteltype = value.getString("hoteltype", recordSchema);
                String last_pay_time = value.getString("last_pay_time", recordSchema);

                StringBuffer sb = new StringBuffer();
                sb.append("price_json=" + price + "\t");
                sb.append("brand_json=" + brand + "\t");
                sb.append("hourroom_json=" + hourroom + "\t");
                sb.append("roomtype_json=" + roomtype + "\t");
                sb.append("hoteltype_json=" + hoteltype + "\t");
                sb.append("last_pay_time=" + last_pay_time);

                context.write(new Text(userid.toString()), new Text(sb.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class user_location_mapper extends Mapper<WritableComparable, HCatRecord, Text, Text> {
        @Override
        public void map(WritableComparable key, HCatRecord value, Context context)
                throws IOException, InterruptedException
        {
            try {
                HCatSplit split = (HCatSplit) context.getInputSplit();
                HCatSchema recordSchema = split.getTableSchema();

                Integer userid = value.getInteger("userid", recordSchema);
                Integer type  = value.getInteger("type", recordSchema);
                Double lat = value.getDouble("lat", recordSchema);
                Double lon = value.getDouble("lng", recordSchema);
                Integer freq = value.getInteger("frequence", recordSchema);

                //String name = type.equals(LOCATION.LOCATION_TYPE_HOME) ? LOCATION_HOME : LOCATION_WORK;
                //String location = lat + "," + lon + "," + freq;

                StringBuffer sb = new StringBuffer();
                sb.append("lat=" + lat + "\t");
                sb.append("lon=" + lon + "\t");
                sb.append("freq=" + freq + "\t");
                sb.append("type=" + type);

                context.write(new Text(userid.toString()), new Text(sb.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class userid2uuid_mapper extends
            Mapper<WritableComparable, HCatRecord, Text, Text> {

        @Override
        public void map(WritableComparable key, HCatRecord value,
                        Context context) throws IOException, InterruptedException {
            try {
                HCatSplit split = (HCatSplit) context.getInputSplit();
                HCatSchema recordSchema = split.getTableSchema();
                String userid = value.getString("userid", recordSchema);
                String uuid = value.getString("uuid", recordSchema);
                String output = "UUID#"+uuid;
                context.write(new Text(userid), new Text(output));
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    public static  class user_basic_profile_mapper extends Mapper<WritableComparable, HCatRecord, Text, Text> {
        @Override
        public void map(WritableComparable key, HCatRecord value,
                        Context context) throws  IOException, InterruptedException {
            StringBuffer sb = new StringBuffer();
            try {
                HCatSplit split = (HCatSplit) context.getInputSplit();
                HCatSchema recordSchema = split.getTableSchema();
                Integer userid = value.getInteger("userid", recordSchema);

                Integer age = value.getInteger("age", recordSchema);
                sb.append("age=" + age + "\t");

                Integer agegroup = value.getInteger("agegroup", recordSchema);
                sb.append("agegroup=" + agegroup + "\t");

                Double agegroup_score = value.getDouble("agegroup_score", recordSchema);
                sb.append("agegroup_score=" + agegroup_score + "\t");

                Integer gender = value.getInteger("gender", recordSchema);
                sb.append("gender=" + gender + "\t");

                Double gender_score = value.getDouble("gender_score", recordSchema);
                sb.append("gender_score=" + gender_score + "\t");

                Integer identity = value.getInteger("identity", recordSchema);
                sb.append("identity=" + identity + "\t");

                Double identity_score = value.getDouble("identity_score", recordSchema);
                sb.append("identity_score=" + identity_score + "\t");

                Integer marriage = value.getInteger("marriage", recordSchema);
                sb.append("marriage=" + marriage + "\t");

                Double marriage_score = value.getDouble("marriage_score", recordSchema);
                sb.append("marriage_score=" + marriage_score + "\t");

                Integer highest_edu_level = value.getInteger("highest_edu_level", recordSchema);
                sb.append("highest_edu_level=" + highest_edu_level + "\t");

                Double highest_edu_level_score = value.getDouble("highest_edu_level_score", recordSchema);
                sb.append("highest_edu_level_score=" + highest_edu_level_score);
                context.write(new Text(userid.toString()), new Text(sb.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static  class user_consumption_level_mapper extends Mapper<WritableComparable, HCatRecord, Text, Text> {
        @Override
        public void map(WritableComparable key, HCatRecord value,
                        Context context) throws  IOException, InterruptedException {
            try {
                HCatSplit split = (HCatSplit) context.getInputSplit();
                HCatSchema recordSchema = split.getTableSchema();
                //低消费 < 0.3 ,  高消费 > 0.9
                Double consumption_level = value.getDouble("consumption_level", recordSchema);
                Integer userid = value.getInteger("userid", recordSchema);

                String output = "consumption_level=" + consumption_level;
                context.write(new Text(userid.toString()), new Text(output.toString()));
            } catch (Exception e) {

            }
        }
    }

    public static  class user_feedback_attr_mapper extends Mapper<WritableComparable, HCatRecord, Text, Text> {
        @Override
        public void map(WritableComparable key, HCatRecord value,
                        Context context) throws  IOException, InterruptedException {
            StringBuffer sb = new StringBuffer();
            try {
                HCatSplit split = (HCatSplit) context.getInputSplit();
                HCatSchema recordSchema = split.getTableSchema();
                Double feedback_avgscore = value.getDouble("feedback_avgscore", recordSchema);
                sb.append("feedback_avgscore=" + feedback_avgscore + "\t");

                Integer feedback_count = value.getInteger("feedback_count", recordSchema);
                sb.append("feedback_count=" + feedback_count + "\t");

                Integer feedback_totalscore = value.getInteger("feedback_totalscore", recordSchema);
                sb.append("feedback_totalscore=" + feedback_totalscore);

                Integer userid = value.getInteger("userid", recordSchema);
                context.write(new Text(userid.toString()), new Text(sb.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static  class user_lifetime_mapper extends Mapper<WritableComparable, HCatRecord, Text, Text> {
        @Override
        public void map(WritableComparable key, HCatRecord value,
                        Context context) throws  IOException, InterruptedException {
            try {
                HCatSplit split = (HCatSplit) context.getInputSplit();
                HCatSchema recordSchema = split.getTableSchema();
                Integer userid = value.getInteger("userid", recordSchema);
                Integer stage = value.getInteger("stage", recordSchema);
                String stage_f = "STAGE:"+stage;
                String output = "FEATURE#"+stage_f;
                context.write(new Text(userid.toString()), new Text(output));
            } catch (Exception e) {

            }
        }
    }


    public static  class user_population_property_mapper extends Mapper<WritableComparable, HCatRecord, Text, Text> {
        @Override
        public void map(WritableComparable key, HCatRecord value,
                        Context context) throws  IOException, InterruptedException {
            try {
                HCatSplit split = (HCatSplit) context.getInputSplit();
                HCatSchema recordSchema = split.getTableSchema();
                Integer userid = value.getInteger("userid", recordSchema);
                Integer age = value.getInteger("age", recordSchema);
                Integer gender = value.getInteger("gender", recordSchema);
                String output = "real_age=" + age + "\treal_gender=" + gender;
                context.write(new Text(userid.toString()), new Text(output));
            } catch (Exception e) {

            }
        }
    }


    public static  class user_attr_mapper extends Mapper<WritableComparable, HCatRecord, Text, Text> {
        @Override
        public void map(WritableComparable key, HCatRecord value,
                        Context context) throws  IOException, InterruptedException {
            StringBuffer sb = new StringBuffer();
            try {
                HCatSplit split = (HCatSplit) context.getInputSplit();
                HCatSchema recordSchema = split.getTableSchema();
                Double feedback_avgscore = value.getDouble("feedback_avgscore", recordSchema);
                sb.append("FEATURE#");
                sb.append("FEEDBACK_AVGSCORE2:"+feedback_avgscore+",");

                Integer first_mobbuy_classid = value.getInteger("first_mobbuy_classid", recordSchema);
                sb.append("FIRST_MOBBUY_CLASSID:"+first_mobbuy_classid+",");

                Integer first_mobbuy_typeid = value.getInteger("first_mobbuy_typeid", recordSchema);
                sb.append("FIRST_MOBBUY_TYPEID:"+first_mobbuy_typeid+",");

                Integer first_pcbuy_classid = value.getInteger("first_pcbuy_classid", recordSchema);
                sb.append("FIRST_PCBUY_CLASSID:"+first_pcbuy_classid+",");

                Integer first_pcbuy_typeid = value.getInteger("first_pcbuy_typeid", recordSchema);
                sb.append("FIRST_PCBUY_TYPEID:"+first_pcbuy_typeid+",");

                Integer last_mobbuy_classid = value.getInteger("last_mobbuy_classid", recordSchema);
                sb.append("LAST_MOBBUY_CLASSID:"+last_mobbuy_classid+",");

                Integer last_mobbuy_typeid = value.getInteger("last_mobbuy_typeid", recordSchema);
                sb.append("LAST_MOBBUY_TYPEID:"+last_mobbuy_typeid+",");

                Integer last_pcbuy_classid = value.getInteger("last_pcbuy_classid", recordSchema);
                sb.append("LAST_PCBUY_CLASSID:"+last_pcbuy_classid+",");

                Integer last_pcbuy_typeid = value.getInteger("last_pcbuy_typeid", recordSchema);
                sb.append("LAST_PCBUY_TYPEID:"+ last_pcbuy_typeid);

                Integer userid = value.getInteger("userid", recordSchema);
                context.write(new Text(userid+""), new Text(sb.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class mergeReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException
        {
            Iterator<Text> iter = values.iterator();
            StringBuffer sb = new StringBuffer();

            while (iter.hasNext()) {
                String val = iter.next().toString();
                sb.append(val + "\t");
            }
            context.write(new Text(key), new Text(sb.toString().trim()));
        }
    }

    //@Override
    public int run(String[] args) throws Exception {
        // TODO Auto-generated method stub
        String outputPath = args[0];
        Configuration conf = getConf();
        conf.addResource("hive-site.xml");

        Job job = new Job(conf, "fetcher_ups_log_job");
        job.getConfiguration().set("mapred.job.queue.name",
                "root.hadoop-hotel.data");
        job.setInputFormatClass(HCatInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        HCatMultipleInputs.init(job);
        String filter = "";
        //http://wiki.sankuai.com/pages/viewpage.action?pageId=186230203
        HCatMultipleInputs.addInput("user_profile_hotel","ns_hotel", filter, user_hotel_profile_mapper.class);
        HCatMultipleInputs.addInput("user_workhome_location","mart_mobile", filter, user_location_mapper.class);
        //用户基础属性数据
        //HCatMultipleInputs.addInput("user_attr_stage1_qianmin","ba_ups", filter, user_attr_mapper.class);
        //用户基础属性
        HCatMultipleInputs.addInput("user_basic_profile_new","ba_ups", filter, user_basic_profile_mapper.class);
        //消费水平
        HCatMultipleInputs.addInput("user_consumption_level","ba_ups", filter, user_consumption_level_mapper.class);
        //用户消费评价相关属性
        HCatMultipleInputs.addInput("user_feedback_attr","ba_ups", filter, user_feedback_attr_mapper.class);
        //当前生命周期
        //HCatMultipleInputs.addInput("user_lifetime","ba_ups", filter, user_lifetime_mapper.class);
        // 真实年龄和性别
        HCatMultipleInputs.addInput("user_population_property","ba_ups", filter, user_population_property_mapper.class);

        HCatMultipleInputs.build();

        job.setJarByClass(this.getClass());
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setReducerClass(mergeReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setNumReduceTasks(200);

        return (job.waitForCompletion(true) ? 0 : 1);
    }

    public static void main(final String[] args) throws Exception {
        /*
        int exitCode = SecurityUtils.doAs("hadoop-hotel/_HOST@SANKUAI.COM",
                "/etc/hadoop/keytabs/hadoop-hotel.keytab", null,
                new PrivilegedExceptionAction<Integer>() {
                    //@Override
                    public Integer run() throws Exception {
                        int exitCode = ToolRunner.run(
                                new UserFeatureMerger(), args);
                        return exitCode;
                    }
                });
        System.exit(exitCode);
        */

        int exitCode = ToolRunner.run(new UserFeatureMerger(), args);
        System.exit(exitCode);
    }
}
