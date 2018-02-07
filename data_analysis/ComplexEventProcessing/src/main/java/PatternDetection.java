import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;
import org.apache.flink.shaded.guava18.com.google.common.collect.Iterables;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import java.util.List;
import java.util.Map;

public class PatternDetection {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stream = env.socketTextStream("localhost",9999);

        Pattern<String,?> pattern = Pattern
        /* (RISE | FALL) as A */
        .<String>begin("A").where(new SimpleCondition<String>() {
            public boolean filter(String s) throws Exception { return s.split(";")[0].equals("RISE") || s.split(";")[0].equals("FALL");
            }
        })
        /* RISE* as B */
        .followedBy("B")
            .where(new SimpleCondition<String>() {
                    public boolean filter(String s) throws Exception { return s.split(";")[0].equals("RISE");
                    }
                }).oneOrMore().optional()
        /* RISE as C */
        .followedBy("C")
            .where(new SimpleCondition<String>() {
                    public boolean filter(String s) throws Exception {
                        return s.split(";")[0].equals("RISE");
                    }
                })
        /* FALL* as D */
        .followedBy("D")
            .where(new SimpleCondition<String>() {
                    public boolean filter(String s) throws Exception { return s.split(";")[0].equals("FALL");
                    }
                }).oneOrMore().optional()
        /* FALL as E */
        .followedBy("E")
            .where(new SimpleCondition<String>() {
                    public boolean filter(String s) throws Exception { return s.split(";")[0].equals("FALL");
                    }
                })
            .where(new IterativeCondition<String>() {
                @Override
                public boolean filter(String value, Context ctx) throws Exception {
                    return Double.parseDouble(Iterables.getFirst(ctx.getEventsForPattern("A"),null).toString().split(";")[1]) <
                            Double.parseDouble(value.split(";")[1]);
                }
            })
        /* RISE* as F */
            .followedBy("F")
            .where(new SimpleCondition<String>() {
                    public boolean filter(String s) throws Exception { return s.split(";")[0].equals("RISE");
                    }
                }).oneOrMore().optional()
        /* RISE as G */
            .followedBy("G")
            .where(new SimpleCondition<String>() {
                    public boolean filter(String s) throws Exception {return s.split(";")[0].equals("RISE");
                    }
                })
            .where(new IterativeCondition<String>() {
                    @Override
                    public boolean filter(String value, Context ctx) throws Exception {
                return Double.parseDouble(Iterables.getFirst(ctx.getEventsForPattern("C"),null).toString().split(";")[1]) <
                    Double.parseDouble(value.split(";")[1]);
                }
            });


        PatternStream<String> patternStream = CEP.pattern(stream, pattern);
        patternStream.select(new PatternSelectFunction<String, String>() {
            public String select(Map<String, List<String>> pattern) throws Exception {
                System.out.println(pattern.toString());
                return "ok";
            }
        });

        stream.print();
        env.execute();
    }

}