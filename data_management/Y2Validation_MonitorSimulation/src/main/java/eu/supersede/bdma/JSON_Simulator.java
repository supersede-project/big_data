package eu.supersede.bdma;

import com.google.common.collect.Maps;
import eu.supersede.integration.api.analysis.proxies.DataProviderProxy;
import eu.supersede.integration.api.mdm.proxies.IMetadataManagement;
import eu.supersede.integration.api.mdm.proxies.MetadataManagementProxy;
import eu.supersede.integration.api.mdm.types.Release;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.io.File;
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
        files = Maps.newHashMap();
        for (Release R : proxy.getAllReleases()) {
            System.out.println(R.getKafkaTopic());
            String kafka = R.getKafkaTopic();
            RandomAccessFile file = new RandomAccessFile(
                    new File(Thread.currentThread().getContextClassLoader().getResource(R.getEvent()+".json").toString().replace("file:",""))
                    ,"r");

            files.put(kafka,file);
        }

        /*files.put("snf",  new RandomAccessFile(
                new File(Thread.currentThread().getContextClassLoader().getResource("AtosAudienceMonitor.json").toString().replace("file:",""))
                ,"r"));*/
        dataProvider = new DataProviderProxy();
    }

    @Override
    public void run() {
        while (true) {
            for (String topic : files.keySet()) {
                String tuple = null;
                try {
                    tuple = getNextTuple(topic);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                try {
                    System.out.println("sending data to "+topic);
                    //WP2KafkaProducer.writeToKafka(tuple,topic);
                    dataProvider.ingestData(tuple,topic);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }
    }

    @Override
    public String getNextTuple(String topic) throws Exception {
        String line = files.get(topic).readLine();
        if (line == null || line.isEmpty()) {
            files.get(topic).seek(0);
            line = files.get(topic).readLine();
        }
        return line;

    }


}
