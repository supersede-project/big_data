package eu.supersede.bdma.sa.proxies;

import com.clearspring.analytics.util.Lists;
import com.google.common.collect.Maps;
import eu.supersede.integration.api.mdm.proxies.IMetadataManagement;
import eu.supersede.integration.api.mdm.proxies.MetadataManagementProxy;
import eu.supersede.integration.api.mdm.types.ECA_Rule;
import eu.supersede.integration.api.mdm.types.Release;
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

    public static Map<String, Tuple2<Boolean,String>> getReleasesIndexedPerKafkaTopic2() throws Exception {
        IMetadataManagement proxy = new MetadataManagementProxy<Object, Object>();

        Map<String, Tuple2<Boolean,String>> m = Maps.newConcurrentMap();
        for (Release r : proxy.getAllReleases()) {
            m.put(r.getKafkaTopic(),new Tuple2<Boolean,String>(r.isDispatch(),r.getDispatcherPath()));
        }
        //m.put("SergiTest", new Tuple2<Boolean,String>(false,""));
        return Collections.unmodifiableMap(m);
    }

    public static List<ECA_Rule> getRules() throws Exception {
        IMetadataManagement proxy = new MetadataManagementProxy<Object, Object>();
        return proxy.getAllECARules();
/*
        // TODO Update and get data from MDM
        ECA_Rule r = new ECA_Rule();
        r.setAction(ActionTypes.ALERT_DYNAMIC_ADAPTATION);
        r.setEca_ruleID("an id");
        r.setGlobalLevel("http://supersede/GLOBAL/rBOxtCPqD3nImChe1zRWOpO469e7Py66");
        r.setFeature("http://www.BDIOntology.com/global/Feature/Vod");
        r.setName("A RULE");
        r.setWindowTime(5);
        r.setValue(10);
        r.setGraph("http://supersede/RULES/vdeQiAhdKaFc9Ct7Vn7Sn9BCQEVujRG4");
        r.setOperator(OperatorTypes.FEEDBACK_CLASSIFIER_LABEL);
        r.setPredicate(PredicatesTypes.EQ);

        return Arrays.asList(r);*/
    }

}
