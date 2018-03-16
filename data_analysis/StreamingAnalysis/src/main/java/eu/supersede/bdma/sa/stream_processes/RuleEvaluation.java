package eu.supersede.bdma.sa.stream_processes;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import eu.supersede.bdma.sa.Main;
import eu.supersede.bdma.sa.eca_rules.DynamicAdaptationAlert;
import eu.supersede.bdma.sa.eca_rules.FeedbackReconfigurationAlert;
import eu.supersede.bdma.sa.eca_rules.MonitorReconfigurationAlert;
import eu.supersede.bdma.sa.eca_rules.SoftwareEvolutionAlert;
import eu.supersede.bdma.sa.eca_rules.conditions.ConditionEvaluator;
import eu.supersede.bdma.sa.utils.MonitorReconfigurationJSON;
import eu.supersede.bdma.sa.utils.Sockets;
import eu.supersede.bdma.sa.utils.Utils;
import eu.supersede.integration.api.mdm.types.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import scala.Tuple2;
import java.util.*;

/**
 * Created by snadal on 28/05/17.
 */
public class RuleEvaluation {

    private static Map<String, Long> firedRulesXTimestamp = Maps.newConcurrentMap();

    private static void windowBasedRuleEvaluation(JavaPairDStream<String, Tuple2<String,Long>> window,
                                                  ActionTypes windowType, Broadcast<List<Event>> events,
                                                  Broadcast<List<ECA_Rule>> rules) {
        window
            .groupByKey()
            .foreachRDD(rdd -> {
                rdd.foreach(records -> {
                    rules.value().forEach(eca_rule -> {
                        if (eca_rule.getEca_ruleID().equals(records._1)) {
                            List<String> data = Lists.newArrayList();

                            records._2().forEach(t -> {
                                long windowSize;
                                if (windowType.val().equals(ActionTypes.ALERT_EVOLUTION.val()))
                                    windowSize = Long.parseLong(Main.properties.getProperty("WINDOW_SIZE_EVOLUTION_MS"));
                                else if (windowType.val().equals(ActionTypes.ALERT_DYNAMIC_ADAPTATION.val()))
                                    windowSize = Long.parseLong(Main.properties.getProperty("WINDOW_SIZE_DYNAMIC_ADAPTATION_MS"));
                                else if (windowType.val().equals(ActionTypes.ALERT_MONITOR_RECONFIGURATION.val()))
                                    windowSize = Long.parseLong(Main.properties.getProperty("WINDOW_SIZE_EVOLUTION_MS"));
                                else windowSize = 0;

                                if (firedRulesXTimestamp.get(eca_rule.getEca_ruleID()) < t._2() &&
                                        firedRulesXTimestamp.get(eca_rule.getEca_ruleID()) + windowSize < System.currentTimeMillis()) {
                                    data.add(t._1());
                                }
                            });
                            boolean allConditionsOK = true;
                            for (Condition condition : eca_rule.getConditions()) {
                                int valids = 0;
                                List<String> extractedData = Lists.newArrayList();
                                for (String json : data) {
                                    //This is for the monitoring reconfiguration monitor
                                    if (json.contains("disk consumption attachments")) json = MonitorReconfigurationJSON.adaptJSON(json);

                                    Utils.extractFeatures(json, condition.getAttribute()).forEach(element -> extractedData.add(element));
                                }

                                if (OperatorTypes.valueOf(condition.getOperator()).equals(OperatorTypes.VALUE)) {
                                    //Check if we are comparing numbers or strings
                                    if (!extractedData.isEmpty()) {
                                        try {
                                            Double.parseDouble(extractedData.get(0));
                                            valids = ConditionEvaluator.evaluateNumericRule(condition.getPredicate(),
                                                    condition.getValue(), Iterables.toArray(extractedData, String.class));
                                        } catch (Exception exc) {
                                            valids = ConditionEvaluator.evaluateTextualRule(condition.getPredicate(),
                                                    condition.getValue(), Iterables.toArray(extractedData, String.class));
                                        }
                                    }
                                }
                                else if (OperatorTypes.valueOf(condition.getOperator()).equals(OperatorTypes.ENGLISH_FEEDBACK_CLASSIFIER_LABEL)) {
                                    valids = ConditionEvaluator.evaluateEnglishFeedbackClassifierRule(condition.getPredicate(),
                                            condition.getValue(), Iterables.toArray(extractedData, String.class));
                                }
                                else if (OperatorTypes.valueOf(condition.getOperator()).equals(OperatorTypes.ENGLISH_OVERALL_SENTIMENT)) {
                                    valids = ConditionEvaluator.evaluateEnglishOverallSentimentRule(condition.getPredicate(),
                                            condition.getValue(), Iterables.toArray(extractedData, String.class));
                                }
                                else if (OperatorTypes.valueOf(condition.getOperator()).equals(OperatorTypes.ENGLISH_POSITIVE_SENTIMENT)) {
                                    valids = ConditionEvaluator.evaluateEnglishPositiveSentimentRule(condition.getPredicate(),
                                            condition.getValue(), Iterables.toArray(extractedData, String.class));
                                }
                                else if (OperatorTypes.valueOf(condition.getOperator()).equals(OperatorTypes.ENGLISH_NEGATIVE_SENTIMENT)) {
                                    valids = ConditionEvaluator.evaluateEnglishNegativeSentimentRule(condition.getPredicate(),
                                            condition.getValue(), Iterables.toArray(extractedData, String.class));
                                }
                                else if (OperatorTypes.valueOf(condition.getOperator()).equals(OperatorTypes.GERMAN_FEEDBACK_CLASSIFIER_LABEL)) {
                                    valids = ConditionEvaluator.evaluateGermanFeedbackClassifierRule(condition.getPredicate(),
                                            condition.getValue(), Iterables.toArray(extractedData, String.class));
                                }
                                else if (OperatorTypes.valueOf(condition.getOperator()).equals(OperatorTypes.GERMAN_OVERALL_SENTIMENT)) {
                                    valids = ConditionEvaluator.evaluateGermanOverallSentimentRule(condition.getPredicate(),
                                            condition.getValue(), Iterables.toArray(extractedData, String.class));
                                }
                                else if (OperatorTypes.valueOf(condition.getOperator()).equals(OperatorTypes.GERMAN_POSITIVE_SENTIMENT)) {
                                    valids = ConditionEvaluator.evaluateGermanPositiveSentimentRule(condition.getPredicate(),
                                            condition.getValue(), Iterables.toArray(extractedData, String.class));
                                }
                                else if (OperatorTypes.valueOf(condition.getOperator()).equals(OperatorTypes.GERMAN_NEGATIVE_SENTIMENT)) {
                                    valids = ConditionEvaluator.evaluateGermanNegativeSentimentRule(condition.getPredicate(),
                                            condition.getValue(), Iterables.toArray(extractedData, String.class));
                                }
                                else if (OperatorTypes.valueOf(condition.getOperator()).equals(OperatorTypes.ONTOLOGICAL_DISTANCE)) {
                                    valids = ConditionEvaluator.evaluateOntologicalDistanceRule(condition.getPredicate(),
                                            condition.getValue(), Iterables.toArray(extractedData, String.class), eca_rule.getParameters(),
                                            eca_rule.getEvent().getTenant());
                                    //eca_rule.getEvent().get
                                }

                                if (valids < eca_rule.getWindowSize()) allConditionsOK = false;

                            }

                            if (allConditionsOK) {
                                firedRulesXTimestamp.put(eca_rule.getEca_ruleID(),System.currentTimeMillis());
                                if (windowType.val().equals(ActionTypes.ALERT_EVOLUTION.val())) {
                                    List<String> feedbacks = Lists.newArrayList();
                                    for (String json : data) {
                                         Utils.extractFeatures(json,"Attributes/textFeedbacks/text").
                                                 forEach(e -> feedbacks.add(e));
                                    }
                                    //SoftwareEvolutionAlert.sendAlert(eca_rule, Iterables.toArray(feedbacks,String.class));
                                }
                                else if (windowType.val().equals(ActionTypes.ALERT_DYNAMIC_ADAPTATION.val())) {
                                    DynamicAdaptationAlert.sendAlert(eca_rule);
                                }
                                else if (windowType.val().equals(ActionTypes.ALERT_MONITOR_RECONFIGURATION.val())) {
                                    MonitorReconfigurationAlert.sendAlert(eca_rule,data);
                                }
                                else if (windowType.val().equals(ActionTypes.ALERT_FEEDBACK_RECONFIGURATION.val())) {
                                    FeedbackReconfigurationAlert.sendAlert(eca_rule);
                                }
                            }
                        }
                    });
                });
            });
    }

    public static void process(JavaInputDStream<ConsumerRecord<String, String>> kafkaStream,
                               Broadcast<List<Event>> events,
                               Broadcast<List<ECA_Rule>> rules) {
        for (ECA_Rule r : rules.value()) {
            firedRulesXTimestamp.put(r.getEca_ruleID(), Long.valueOf(0));
        }

        JavaDStream<ConsumerRecord<String, String>> nonEmptyStream = kafkaStream.filter(record -> !record.value().isEmpty());

        JavaPairDStream<String, Tuple2<String,Long>> evolutionWindow = nonEmptyStream
                .flatMapToPair(record -> {
                    List<Tuple2<String, Tuple2<String,Long>>> recordsPerRule = Lists.newArrayList();
                    rules.value().forEach(rule -> {
                        if (rule.getEvent().getKafkaTopic().equals(record.topic()) &&
                                rule.getAction().val().equals(ActionTypes.ALERT_EVOLUTION.val())) {
                            recordsPerRule.add(new Tuple2<String,Tuple2<String,Long>>(rule.getEca_ruleID(),
                                    new Tuple2<String, Long>(record.value(),System.currentTimeMillis())));
                        }
                    });
                    return recordsPerRule.iterator();
                }).window(new Duration(Long.parseLong(Main.properties.getProperty("WINDOW_SIZE_EVOLUTION_MS"))),new Duration(5000));

        JavaPairDStream<String, Tuple2<String,Long>> adaptationWindow = nonEmptyStream
                .flatMapToPair(record -> {
                    List<Tuple2<String, Tuple2<String,Long>>> recordsPerRule = Lists.newArrayList();
                    rules.value().forEach(rule -> {
                        if (rule.getEvent().getKafkaTopic().equals(record.topic()) &&
                                rule.getAction().val().equals(ActionTypes.ALERT_DYNAMIC_ADAPTATION.val())) {
                            recordsPerRule.add(new Tuple2<String,Tuple2<String,Long>>(rule.getEca_ruleID(),
                                    new Tuple2<String, Long>(record.value(),System.currentTimeMillis())));
                        }
                    });
                    return recordsPerRule.iterator();
                }).window(new Duration(Long.parseLong(Main.properties.getProperty("WINDOW_SIZE_DYNAMIC_ADAPTATION_MS"))),new Duration(5000));

        JavaPairDStream<String, Tuple2<String,Long>> monitorReconfigurationWindow = nonEmptyStream
                .flatMapToPair(record -> {
                    List<Tuple2<String, Tuple2<String,Long>>> recordsPerRule = Lists.newArrayList();
                    rules.value().forEach(rule -> {
                        if (rule.getEvent().getKafkaTopic().equals(record.topic()) &&
                                rule.getAction().val().equals(ActionTypes.ALERT_MONITOR_RECONFIGURATION.val())) {
                            recordsPerRule.add(new Tuple2<String,Tuple2<String,Long>>(rule.getEca_ruleID(),
                                    new Tuple2<String, Long>(record.value(),System.currentTimeMillis())));
                        }
                    });
                    return recordsPerRule.iterator();
                }).window(new Duration(Long.parseLong(Main.properties.getProperty("WINDOW_SIZE_MONITOR_RECONF_MS"))),new Duration(5000));

        JavaPairDStream<String, Tuple2<String,Long>> feedbackReconfigurationWindow = nonEmptyStream
                .flatMapToPair(record -> {
                    List<Tuple2<String, Tuple2<String,Long>>> recordsPerRule = Lists.newArrayList();
                    rules.value().forEach(rule -> {
                        if (rule.getEvent().getKafkaTopic().equals(record.topic()) &&
                                rule.getAction().val().equals(ActionTypes.ALERT_FEEDBACK_RECONFIGURATION.val())) {
                            recordsPerRule.add(new Tuple2<String,Tuple2<String,Long>>(rule.getEca_ruleID(),
                                    new Tuple2<String, Long>(record.value(),System.currentTimeMillis())));
                        }
                    });
                    return recordsPerRule.iterator();
                }).window(new Duration(Long.parseLong(Main.properties.getProperty("WINDOW_SIZE_FEEDBACK_RECONF_MS"))),new Duration(5000));

        windowBasedRuleEvaluation(evolutionWindow,ActionTypes.ALERT_EVOLUTION,events,rules);
        windowBasedRuleEvaluation(adaptationWindow,ActionTypes.ALERT_DYNAMIC_ADAPTATION,events,rules);
        windowBasedRuleEvaluation(monitorReconfigurationWindow,ActionTypes.ALERT_MONITOR_RECONFIGURATION,events,rules);
        windowBasedRuleEvaluation(feedbackReconfigurationWindow,ActionTypes.ALERT_FEEDBACK_RECONFIGURATION,events,rules);
    }
}
