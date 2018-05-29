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

    public static List<Event> getAllEvents() throws Exception {
        IMetadataManagement proxy = new MetadataManagementProxy<Object, Object>();
        return proxy.getAllEvents();
    }

    public static Map<String, Release> getReleasesIndexedPerKafkaTopic() throws Exception {
        IMetadataManagement proxy = new MetadataManagementProxy<Object, Object>();

        Map<String, Release> m = Maps.newConcurrentMap();
        for (Release r : proxy.getAllReleases()) {
            m.put(r.getKafkaTopic(),r);
        }
        return Collections.unmodifiableMap(m);
    }

    public static Map<String, Tuple2<Boolean,String>> getReleasesIndexedPerKafkaTopic2(String evo_adapt) throws Exception {
        IMetadataManagement proxy = new MetadataManagementProxy<Object, Object>();

        Map<String, Tuple2<Boolean,String>> m = Maps.newConcurrentMap();
        for (Release r : proxy.getAllReleases()) {
            m.put(r.getKafkaTopic(),new Tuple2<Boolean,String>(r.isDispatch(),r.getDispatcherPath()));
        }
        return Collections.unmodifiableMap(m);
    }

    public static List<ECA_Rule> getRules() throws Exception {
        IMetadataManagement proxy = new MetadataManagementProxy<Object, Object>();
        return proxy.getAllECARules();
    }

}
