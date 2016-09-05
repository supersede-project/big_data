package eu.supersede.bdma.validation.batchprocessing.senercon;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.supersede.bdma.validation.batchprocessing.Main;
import scala.Tuple2;
import scala.Tuple7;

public class ErrorStatistics {
	//http://koalyptus.github.io/TableFilter/examples
	private static ImmutableMap<String, DataType> schemaMap = ImmutableMap.<String,DataType>builder()
			.put("mail",DataTypes.StringType)
			.put("date",DataTypes.DateType)
			.put("path",DataTypes.StringType)
			.put("user_id",DataTypes.StringType)
			.put("portal_id",DataTypes.StringType)
			.put("error_code",DataTypes.StringType)
			.put("source_code_position",DataTypes.StringType)
			.put("error_type",DataTypes.StringType)
			.build();
	
	public static String process() {
		if (!Main.cache_statistics.isEmpty()) return Main.cache_statistics;
		
		SparkConf sc = new SparkConf().setAppName("BatchProcessing_ErrorStatistics").setMaster("local[*]");;
		JavaSparkContext ctx = new JavaSparkContext(sc);
		SQLContext sqlctx = new SQLContext(ctx);
		
		JavaPairRDD<String,String> files = ctx.wholeTextFiles(Main.emails_path).cache();
		
		// Tuple2<Email name, a value>
		JavaPairRDD<String,String> sourceCodePositions = files.mapToPair(new PairFunction<Tuple2<String,String>, String, String>() {
			@Override
			public Tuple2<String, String> call(Tuple2<String, String> arg0) throws Exception {
				String pattern1 = "\\*\\*\\* STACKTRACE:[^\n]*\n+.+[^\n]*\n+.+::([0-9]+)";
				String pattern2 = "\\*\\*\\* STACKTRACE:[^\n]*\n+.+[^\n]*\n+.+[^\n]*\n+.+::([0-9]+)"; 
				
				Pattern p1 = Pattern.compile(pattern1);
				Pattern p2 = Pattern.compile(pattern2);
				
				Matcher m1 = p1.matcher(arg0._2);
				Matcher m2 = p2.matcher(arg0._2);
				
				if (m1.find()) {
					return new Tuple2<String,String>(arg0._1,m1.group(1));
				}
				if (m2.find()) {
					return new Tuple2<String,String>(arg0._1,m2.group(1));
				}
				return new Tuple2<String,String>(arg0._1,"Unknown");
			}
		});

		JavaPairRDD<String,String> errorTypes = files.mapToPair(new PairFunction<Tuple2<String,String>, String, String>() {
			@Override
			public Tuple2<String, String> call(Tuple2<String, String> arg0) throws Exception {
				String pattern1 = "\\*\\*\\* FEHLERMELDUNG: (.+)";
				
				Pattern p1 = Pattern.compile(pattern1);
				
				Matcher m1 = p1.matcher(arg0._2);
				
				if (m1.find()) {
					return new Tuple2<String,String>(arg0._1,m1.group(1));
				}
				return new Tuple2<String,String>(arg0._1,"Unknown");
			}
		});
		
		JavaPairRDD<String,String> errorCodes = files.mapToPair(new PairFunction<Tuple2<String,String>, String, String>() {
			@Override
			public Tuple2<String, String> call(Tuple2<String, String> arg0) throws Exception {
				String pattern1 = "errorcode: (.+)";
				
				Pattern p1 = Pattern.compile(pattern1);
				
				Matcher m1 = p1.matcher(arg0._2);
				
				if (m1.find()) {
					return new Tuple2<String,String>(arg0._1,m1.group(1));
				}
				return new Tuple2<String,String>(arg0._1,"Unknown");
			}
		});
		
		JavaPairRDD<String,String> portalIds = files.mapToPair(new PairFunction<Tuple2<String,String>, String, String>() {
			@Override
			public Tuple2<String, String> call(Tuple2<String, String> arg0) throws Exception {
				String pattern1 = "\\[portal_id\\] => (.+)";
				
				Pattern p1 = Pattern.compile(pattern1);
				
				Matcher m1 = p1.matcher(arg0._2);
				
				if (m1.find()) {
					return new Tuple2<String,String>(arg0._1,m1.group(1));
				}
				return new Tuple2<String,String>(arg0._1,"Unknown");
			}
		});
		
		JavaPairRDD<String,String> userIds = files.mapToPair(new PairFunction<Tuple2<String,String>, String, String>() {
			@Override
			public Tuple2<String, String> call(Tuple2<String, String> arg0) throws Exception {
				String pattern1 = "\\[user_id\\] => (.+)";
				
				Pattern p1 = Pattern.compile(pattern1);
				
				Matcher m1 = p1.matcher(arg0._2);
				
				if (m1.find()) {
					return new Tuple2<String,String>(arg0._1,m1.group(1));
				}
				return new Tuple2<String,String>(arg0._1,"Unknown");
			}
		});
		
		JavaPairRDD<String,String> pathOfErrors = files.mapToPair(new PairFunction<Tuple2<String,String>, String, String>() {
			@Override
			public Tuple2<String, String> call(Tuple2<String, String> arg0) throws Exception {
				String pattern1 = "\\*\\*\\* STACKTRACE:[^\n]*\n+.+[^\n]*\n+(.+)::";
				String pattern2 = "\\*\\*\\* STACKTRACE:[^\n]*\n+.+[^\n]*\n+.+[^\n]*\n+(.+)::"; 
				
				Pattern p1 = Pattern.compile(pattern1);
				Pattern p2 = Pattern.compile(pattern2);
				
				Matcher m1 = p1.matcher(arg0._2);
				Matcher m2 = p2.matcher(arg0._2);
				
				if (m1.find()) {
					return new Tuple2<String,String>(arg0._1,m1.group(1).substring(34));
				}
				if (m2.find()) {
					return new Tuple2<String,String>(arg0._1,m2.group(1).substring(34));
				}
				return new Tuple2<String,String>(arg0._1,"Unknown");
			}
		});
		
		JavaPairRDD<String,Date> times = files.mapToPair(new PairFunction<Tuple2<String,String>, String, Date>() {
			@Override
			public Tuple2<String, Date> call(Tuple2<String, String> arg0) throws Exception {
				String pattern1 = "Date: .+, (.+) \\+";
				
				Pattern p1 = Pattern.compile(pattern1);
				
				Matcher m1 = p1.matcher(arg0._2);
				
				if (m1.find()) {
					DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss",Locale.ENGLISH);
					LocalDate date = LocalDate.parse(m1.group(1),dateFormat);
				
					return new Tuple2<String,Date>(arg0._1,Date.valueOf(date));
				}
				return new Tuple2<String,Date>(arg0._1,null);
			}
		});
		
		
		//_1 = Date
		//_2 = Path
		//_3 = user_id
		//_4 = portal_id
		//_5 = error code
		//_6 = Source code position
		//_7 = Error type
		JavaPairRDD<String, Tuple7<Date,String,String,String,String,String,String>> joinedData = 
				sourceCodePositions.join(errorTypes).join(errorCodes).join(portalIds).join(userIds).join(pathOfErrors).join(times).mapToPair(
				new PairFunction<Tuple2<String,Tuple2<Tuple2<Tuple2<Tuple2<Tuple2<Tuple2<String,String>,String>,String>,String>,String>,Date>>, String, Tuple7<Date,String,String,String,String,String,String>>() {
					@Override
					public Tuple2<String, Tuple7<Date,String, String, String, String, String, String>> call(
							Tuple2<String, Tuple2<Tuple2<Tuple2<Tuple2<Tuple2<Tuple2<String,String>, String>, String>, String>, String>, Date>> arg0)
									throws Exception {
						return new Tuple2<String,Tuple7<Date,String,String,String,String,String,String>>(arg0._1,
								new Tuple7<Date,String,String,String,String,String,String>(
										arg0._2._2,
										arg0._2._1._2,
										arg0._2._1._1._2,
										arg0._2._1._1._1._2,
										arg0._2._1._1._1._1._2,
										arg0._2._1._1._1._1._1._1,
										arg0._2._1._1._1._1._1._2
									));
					}
				}
			)
		;
		
		
		/*String out = "";
		for (Tuple2<String,Tuple7<String,String,String,String,String,String,String>> t : joinedData.collect()) {
			out += t._1 +"\n";
			out += "	[Source code position]: "+t._2._5()+"\n";
			out += "	[Error type]: "+t._2._6()+"\n";
			out += "	[Error code]: "+t._2._4()+"\n";
			out += "	[Portal ID]: "+t._2._3()+"\n";
			out += "	[User ID]: "+t._2._2()+"\n";
			out += "	[Path]: "+t._2._1()+"\n";
			out += "	[Date]: "+t._2._1()+"\n";
			out += "###########################################################################################\n";
		}
		*/
		
		List<StructField> fields = Lists.newArrayList();
		for (String field : schemaMap.keySet()) {
			fields.add(DataTypes.createStructField(field, schemaMap.get(field), false));
		}
		StructType schema = DataTypes.createStructType(fields);
		
		JavaRDD<Row> rowRDD = joinedData.map(new Function<Tuple2<String,Tuple7<Date,String,String,String,String,String,String>>, Row>() {
			@Override
			public Row call(Tuple2<String, Tuple7<Date,String, String, String, String, String, String>> arg0)
					throws Exception {
				return RowFactory.create(arg0._1,arg0._2._1(),arg0._2._2(),arg0._2._3(),arg0._2._4(),arg0._2._5(),arg0._2._6(),arg0._2._7());
			}
		});
		DataFrame winesDataFrame = sqlctx.createDataFrame(rowRDD, schema);
		winesDataFrame.registerTempTable("errorStatistics");
		
		JSONObject out = new JSONObject();
		Row[] q1 = sqlctx.sql("SELECT * FROM errorStatistics ORDER BY date").collect();
		
		
		
		JSONArray statistics = new JSONArray();
		for (Row r : q1) {
			JSONObject obj = new JSONObject();
			obj.put("date",r.get(1));
			obj.put("path",r.get(2));
			obj.put("user_id",r.get(3));
			obj.put("portal_id",r.get(4));
			obj.put("error_code",r.get(5));
			obj.put("position",r.get(6));
			obj.put("error_type",r.get(7));
			
			statistics.put(obj);
		}
		out.put("error_statistics", statistics);
		
		Row[] q2 = sqlctx.sql("SELECT error_type,COUNT(error_type) FROM errorStatistics GROUP BY error_type").collect();
		JSONArray absolut_sum = new JSONArray();
		for (Row r : q2) {
			JSONObject obj = new JSONObject();
			obj.put("error_type",r.get(0));
			obj.put("count",r.get(1));
			
			absolut_sum.put(obj);
		}
		out.put("absolut_sum", absolut_sum);
		
		Row[] q3 = sqlctx.sql("SELECT date,error_code,COUNT(error_code) FROM errorStatistics GROUP BY date,error_code ORDER BY date").collect();
		JSONArray error_distribution = new JSONArray();
		Map<String, Map<String,Long>> outStructure = Maps.newHashMap();
		List<String> seenErrors = Lists.newArrayList();
		List<String> seenDates = Lists.newArrayList();
		for (Row r : q3) {
			if (!seenDates.contains(r.getDate(0).toString())) seenDates.add(r.getDate(0).toString());
			
			//date
			if (!outStructure.containsKey(r.getDate(0).toString())) {
				outStructure.put(r.getDate(0).toString(), Maps.newHashMap());
			}
			outStructure.get(r.getDate(0).toString()).put(r.getString(1), r.getLong(2));
			
			if (!seenErrors.contains(r.getString(1))) seenErrors.add(r.getString(1));
		}
		
		String csv = "Date";
		for (String e : seenErrors) {
			csv += "," + e;
		}
		csv += "\n";
		for (String date : seenDates) {
			csv += date.substring(5);
			for (String e : seenErrors) {
				if (!outStructure.get(date).containsKey(e)) {
					csv += ",0";
				} else {
					csv += "," + outStructure.get(date).get(e);
				}
			}
			csv += "\n";
		}
		
		Main.cache_statistics_csv = csv;
				
		
		Row[] q4 = sqlctx.sql("SELECT user_id,COUNT(error_code) FROM errorStatistics GROUP BY user_id ORDER BY COUNT(error_code) DESC").collect();
		JSONArray errors_and_users = new JSONArray();
		for (Row r : q4) {
			JSONObject obj = new JSONObject();
			obj.put("user_id",r.get(0));
			obj.put("count",r.get(1));
			
			errors_and_users.put(obj);
		}
		out.put("errors_and_users", errors_and_users);
		
		ctx.stop();
		
		Main.cache_statistics = out.toString();
		return out.toString();
	}
}
