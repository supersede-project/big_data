package eu.supersede.bdma.sa.proxies;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Maps;
import eu.supersede.bdma.sa.eca_rules.SerializableECA_Rule;
import eu.supersede.integration.api.mdm.proxies.IMetadataManagement;
import eu.supersede.integration.api.mdm.proxies.MetadataManagementProxy;
import eu.supersede.integration.api.mdm.types.*;
import scala.Tuple2;

import java.util.*;

/**
 * Created by snadal on 12/01/17.
 */
public class MDMProxy {

    public static Map<String, Release> getReleasesIndexedPerKafkaTopic() throws Exception {
        IMetadataManagement proxy = new MetadataManagementProxy<Object, Object>();

        Map<String, Release> m = Maps.newConcurrentMap();
        for (Release r : proxy.getAllReleases()) {
            m.put(r.getKafkaTopic(),r);
        }

        // TODO obtain that from IF MDM Proxy
        //Release R = new Release();
        //R.setKafkaTopic("f7b78d75-21b3-4c6b-82d9-4d59b4f92f1d");
        // TODO update once Yosu changes Release class
        //R.setReleaseID("/home/snadal/Bolster/DispatcherData/b3defa3b-ba7b-457e-bc5c-87cd284dd2b3.txt");

        //m.put(R.getKafkaTopic(),R);
        return Collections.unmodifiableMap(m);
    }

    public static Map<String, Tuple2<Boolean,String>> getReleasesIndexedPerKafkaTopic2(String evo_adapt) throws Exception {
        IMetadataManagement proxy = new MetadataManagementProxy<Object, Object>();

        Map<String, Tuple2<Boolean,String>> m = Maps.newConcurrentMap();
        /*for (Release r : proxy.getAllReleases()) {
            m.put(r.getKafkaTopic(),new Tuple2<Boolean,String>(r.isDispatch(),r.getDispatcherPath()));
        }*/

        // Evolution
        if (evo_adapt.equals("evolution")) {
            m.put("5ff7d393-e2a5-49fd-a4de-f4e1f7480bf4", new Tuple2<Boolean, String>(false, ""));
        }
        // Adaptation
        if (evo_adapt.equals("adaptation")) {
            m.put("cbdc0cc2-9c08-46a3-8cb5-d2924b02bcfd", new Tuple2<Boolean, String>(false, ""));
            m.put("6433f460-48dc-425f-9200-04464db56ce7", new Tuple2<Boolean, String>(false, ""));
        }
        // Feedback
        //m.put("5ff7d393-e2a5-49fd-a4de-f4e1f7480bf4", new Tuple2<Boolean,String>(false,""));
        // HTTP Monitor
        //m.put("cbdc0cc2-9c08-46a3-8cb5-d2924b02bcfd", new Tuple2<Boolean,String>(false,""));
        return Collections.unmodifiableMap(m);
    }

    public static List<SerializableECA_Rule> getRules() throws Exception {
        //IMetadataManagement proxy = new MetadataManagementProxy<Object, Object>();
        //return proxy.getAllECARules();

        // Evolution demo rule
        /*
        SerializableECA_Rule r = new SerializableECA_Rule();
        r.setAction(ActionTypes.ALERT_EVOLUTION);
        r.setEca_ruleID(UUID.randomUUID().toString());
        r.setGlobalLevel("http://supersede/GLOBAL/rBOxtCPqD3nImChe1zRWOpO469e7Py66");
        r.setFeature("http://www.BDIOntology.com/global/Feature/ratingFeedbacks/rating");
        r.setName("3 Feedbacks with a rating smaller than 3");
        r.setWindowTime(5);
        r.setWindowSize(3);
        r.setValue(3);
        r.setGraph("http://supersede/RULES/vdeQiAhdKaFc9Ct7Vn7Sn9BCQEVujRG4");
        r.setOperator(OperatorTypes.VALUE);
        r.setPredicate(PredicatesTypes.LT);
        */
        // Adaptation demo rule

        // AtoS adaptation rule
        SerializableECA_Rule r1 = new SerializableECA_Rule();
        r1.setAction(ActionTypes.ALERT_DYNAMIC_ADAPTATION);
        r1.setEca_ruleID(UUID.randomUUID().toString());
        r1.setGlobalLevel("http://supersede/GLOBAL/rBOxtCPqD3nImChe1zRWOpO469e7Py66");
        r1.setFeature("http://www.BDIOntology.com/global/Feature/HttpMonitoredData/DataItems/responseCode");
        r1.setName("AtoS - Response code greater than 200 more than 3 times");
        r1.setWindowTime(5); // Hardcoded to 5 right now
        r1.setWindowSize(3); // Number of elements in the window to trigger alert
        r1.setValue(200);
        r1.setGraph("http://supersede/RULES/vdeQiAhdKaFc9Ct7Vn7Sn9BCQEVujRG4");
        r1.setOperator(OperatorTypes.VALUE);
        r1.setPredicate(PredicatesTypes.GT);
        r1.setTenant("atos_hsk");
        r1.setKafkaTopic("cbdc0cc2-9c08-46a3-8cb5-d2924b02bcfd");

        // SIEMENS adaptation rule
        SerializableECA_Rule r2 = new SerializableECA_Rule();
        r2.setAction(ActionTypes.ALERT_DYNAMIC_ADAPTATION);
        r2.setEca_ruleID(UUID.randomUUID().toString());
        r2.setGlobalLevel("http://supersede/GLOBAL/rBOxtCPqD3nImChe1zRWOpO469e7Py66");
        r2.setFeature("http://www.BDIOntology.com/global/Feature/HttpMonitoredData/DataItems/responseCode");
        r2.setName("SIEMENS - Response code greater than 200 more than 6 times");
        r2.setWindowTime(5); // Hardcoded to 5 right now
        r2.setWindowSize(6); // Number of elements in the window to trigger alert
        r2.setValue(200);
        r2.setGraph("http://supersede/RULES/vdeQiAhdKaFc9Ct7Vn7Sn9BCQEVujRG4");
        r2.setOperator(OperatorTypes.VALUE);
        r2.setPredicate(PredicatesTypes.GT);
        r2.setTenant("siemens");
        r2.setKafkaTopic("6433f460-48dc-425f-9200-04464db56ce7");

        // SEnerCon evolution rule
        SerializableECA_Rule r3 = new SerializableECA_Rule();
        r3.setAction(ActionTypes.ALERT_DYNAMIC_ADAPTATION);
        r3.setEca_ruleID(UUID.randomUUID().toString());
        r3.setGlobalLevel("http://supersede/GLOBAL/rBOxtCPqD3nImChe1zRWOpO469e7Py66");
        r3.setFeature("http://www.BDIOntology.com/global/Feature/ratingFeedbacks/rating");
        r3.setName("SEnerCon - 3 feedbacks with a rating less than 3");
        r3.setWindowTime(5);
        r3.setWindowSize(3);
        r3.setValue(3);
        r3.setGraph("http://supersede/RULES/vdeQiAhdKaFc9Ct7Vn7Sn9BCQEVujRG4");
        r3.setOperator(OperatorTypes.VALUE);
        r3.setPredicate(PredicatesTypes.LT);
        r3.setTenant("senercon");
        r3.setKafkaTopic("5ff7d393-e2a5-49fd-a4de-f4e1f7480bf4");

        List<SerializableECA_Rule> l = new java.util.concurrent.CopyOnWriteArrayList<SerializableECA_Rule>();
        l.add(r1);l.add(r2);l.add(r3);

        return l;
    }

}
