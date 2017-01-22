package eu.supersede.bdma;

import eu.supersede.integration.api.analysis.proxies.DataProviderProxy;
import eu.supersede.integration.api.mdm.proxies.IMetadataManagement;
import eu.supersede.integration.api.mdm.proxies.MetadataManagementProxy;
import eu.supersede.integration.api.mdm.types.Release;

import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Map;

/**
 * Created by snadal on 22/01/17.
 */
public class JSON_Simulator extends Simulator {

    private static Collection<Release> allReleases;
    private DataProviderProxy dataProvider;

    private Map<String, RandomAccessFile> files;

    public JSON_Simulator() throws Exception {
        IMetadataManagement proxy = new MetadataManagementProxy<Object, Object>();
        for (Release R : proxy.getAllReleases()) {
            //System.out.println(R.getEvent());
            System.out.println(Thread.currentThread().getContextClassLoader().getResource("AtosAudienceMonitor"+".json"));
            //RandomAccessFile file = new RandomAccessFile(Thread.currentThread().getContextClassLoader().getResource(R.getEvent()+".json").toString(),"r");
        }

        try {
            allReleases = proxy.getAllReleases();
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataProvider = new DataProviderProxy();
    }

    @Override
    public void run() {

    }

    @Override
    public String getNextTuple() throws Exception {
return null;
    }

    @Override
    public String getNextTupleAndReset() throws Exception {
return null;
    }
}
