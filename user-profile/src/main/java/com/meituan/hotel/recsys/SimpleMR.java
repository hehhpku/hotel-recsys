package com.meituan.hotel.recsys;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Iterator;

import com.meituan.hadoop.SecurityUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hive.hcatalog.data.DefaultHCatRecord;
import org.apache.hive.hcatalog.data.HCatRecord;
import org.apache.hive.hcatalog.data.schema.HCatSchema;
import org.apache.hive.hcatalog.mapreduce.HCatInputFormat;
import org.apache.hive.hcatalog.mapreduce.HCatOutputFormat;
import org.apache.hive.hcatalog.mapreduce.InputJobInfo;
import org.apache.hive.hcatalog.mapreduce.OutputJobInfo;

public class SimpleMR extends Configured implements Tool {

    public static class UserMap extends Mapper<WritableComparable, HCatRecord, Text, IntWritable> {
        HCatSchema schema;
        String keyColumn;

        Text key = new Text();

        @Autowired
        protected void setup(Context context) throws IOException, InteruptedException {
            schema = HCatInputFormat.getTableSchema(context.getConfiguration());
            keyColumn = context.getConfiguration.get("keycolumn");
            if (schema == null) {
                throw new RuntimeException("schema is null");
            }
        }


    public static class Map extends Mapper<WritableComparable, HCatRecord, Text, IntWritable> {
        HCatSchema schema;
        String keyColumn;
        Text key = new Text();

        @Override
        protected void setup(org.apache.hadoop.mapreduce.Mapper.Context context)
                throws IOException, InterruptedException {
            schema = HCatInputFormat.getTableSchema(context.getConfiguration());
            keyColumn = context.getConfiguration().get("keycolumn");
            if (schema == null) {
                throw new RuntimeException("schema is null");
            }

        }

        @Override
        protected void map(WritableComparable key, HCatRecord value, Context context) throws IOException, InterruptedException {
            // another way, get data by column index
            // host.set(value.get(8));
            this.key.set(value.getString(this.keyColumn, schema));
            context.write(this.key, new IntWritable(1));
        }
    }

    public static class Reduce extends Reducer<Text, IntWritable, WritableComparable, HCatRecord> {
        HCatSchema schema;

        @Override
        protected void setup(org.apache.hadoop.mapreduce.Reducer.Context context)
                throws IOException, InterruptedException {
            schema = HCatOutputFormat.getTableSchema(context.getConfiguration());
        }

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            Iterator<IntWritable> iter = values.iterator();
            while (iter.hasNext()) {
                sum++;
                iter.next();
            }
            HCatRecord record = new DefaultHCatRecord(2);
            // another way, set data by column index
            // record.set(0, key.toString());
            // record.set(1, sum);
            record.setString("key", schema, key.toString());
            record.setInteger("count", schema, sum);

            context.write(null, record);
        }
    }


    public int run(final String[] args) throws Exception {
        String inputDb = args[0];
        String inputTable = args[1];
        String keyColumn = args[2];
        String filter = args[3];
        String outputDb = args[4];
        String outputTable = args[5];
        String outputfilter = args[6];

        Configuration conf = getConf();
        //conf.addResource("hive-site.xml");

        conf.set("keycolumn", keyColumn);

        Job job = new Job(conf, "testMR-groupby-count");
        System.out.println("filter: " + filter);
        HCatInputFormat.setInput(job, inputDb, inputTable, filter);

        job.setInputFormatClass(HCatInputFormat.class);
        job.setJarByClass(SimpleMR.class);
        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(WritableComparable.class);
        job.setOutputValueClass(DefaultHCatRecord.class);

        HashMap<String, String> partitions = new HashMap<String, String>(1);
        partitions.put("dt", outputfilter);

        HCatOutputFormat.setOutput(job, OutputJobInfo.create(outputDb, outputTable, partitions));
        HCatSchema s = HCatOutputFormat.getTableSchema(job.getConfiguration());
        HCatOutputFormat.setSchema(job, s);
        job.setOutputFormatClass(HCatOutputFormat.class);

        return (job.waitForCompletion(true) ? 0 : 1);
    }

    public static void main(final String[] args) throws Exception {
        int exitCode = ToolRunner.run(new SimpleMR(), args);
        System.exit(exitCode);
    }


}
