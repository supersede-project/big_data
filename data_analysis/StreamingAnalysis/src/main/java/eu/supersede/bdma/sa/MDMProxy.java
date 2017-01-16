package eu.supersede.bdma.sa;

import com.clearspring.analytics.util.Lists;
import eu.supersede.integration.api.mdm.proxies.IMetadataManagement;
import eu.supersede.integration.api.mdm.proxies.MetadataManagementProxy;
import eu.supersede.integration.api.mdm.types.Release;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by snadal on 12/01/17.
 */
public class MDMProxy {

    public static Collection<String> getKafkaTopics() throws Exception {
        /*IMetadataManagement proxy = new MetadataManagementProxy<Object, Object>();

        Collection<String> topics = Lists.newArrayList();
        for (Release r : proxy.getAllReleases()) {
            topics.add(r.getKafkaTopic());
        }*/

        Collection<String> topics = Arrays.asList("e7d9a569-f750-4200-b94e-ccd66df9e76e","a2e0b35b-85c3-4774-bff5-48f397691cfa");
        return topics;
    }

}
