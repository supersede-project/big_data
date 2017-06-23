package eu.supersede.bdma.sa.eca_rules;

import eu.supersede.integration.api.mdm.types.ActionTypes;
import eu.supersede.integration.api.mdm.types.OperatorTypes;
import eu.supersede.integration.api.mdm.types.PredicatesTypes;

import java.io.Serializable;

/**
 * Created by snadal on 6/06/17.
 */
public class SerializableECA_Rule implements Serializable {
    private String eca_ruleID;
    private String graph;
    private String globalLevel;
    private String name;
    private String feature;
    private OperatorTypes operator;
    private PredicatesTypes predicate;
    private Object value;
    private int windowTime;
    private int windowSize;
    private ActionTypes action;
    private String tenant;
    private String kafkaTopic;

    public SerializableECA_Rule() {
    }

    public String getEca_ruleID() {
        return this.eca_ruleID;
    }

    public void setEca_ruleID(String eca_ruleID) {
        this.eca_ruleID = eca_ruleID;
    }

    public String getGraph() {
        return this.graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public String getGlobalLevel() {
        return this.globalLevel;
    }

    public void setGlobalLevel(String globalLevel) {
        this.globalLevel = globalLevel;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFeature() {
        return this.feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public OperatorTypes getOperator() {
        return this.operator;
    }

    public void setOperator(OperatorTypes operator) {
        this.operator = operator;
    }

    public PredicatesTypes getPredicate() {
        return this.predicate;
    }

    public void setPredicate(PredicatesTypes predicate) {
        this.predicate = predicate;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getWindowTime() {
        return this.windowTime;
    }

    public void setWindowTime(int windowTime) {
        this.windowTime = windowTime;
    }

    public int getWindowSize() {
        return this.windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public ActionTypes getAction() {
        return this.action;
    }

    public void setAction(ActionTypes action) {
        this.action = action;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }
}
