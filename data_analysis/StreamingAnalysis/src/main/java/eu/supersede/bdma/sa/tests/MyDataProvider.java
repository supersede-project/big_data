//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package eu.supersede.bdma.sa.tests;

import eu.supersede.integration.api.analysis.proxies.IDataProvider;
import eu.supersede.integration.api.analysis.proxies.KafkaClient;
import eu.supersede.integration.api.analysis.types.MonitoringData;
import eu.supersede.integration.properties.IntegrationProperty;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyDataProvider implements IDataProvider {
    private static final String SUPERSEDE_DATAPROVIDER_ENDPOINT = "147.83.192.53:9092";
    private static final Logger log = LoggerFactory.getLogger(MyDataProvider.class);
    private KafkaClient kafka;

    public MyDataProvider() {
        this.kafka = new KafkaClient(SUPERSEDE_DATAPROVIDER_ENDPOINT);
    }

    public void ingestMonitoringData(List<MonitoringData> dataList, String timeStamp, int outputId, int confId, String topic) {
        JSONArray items = new JSONArray();
        Iterator mainInfo = dataList.iterator();

        while(mainInfo.hasNext()) {
            MonitoringData jsonData = (MonitoringData)mainInfo.next();
            JSONObject jsonItem = new JSONObject();
            jsonItem.put("idItem", jsonData.getId());
            jsonItem.put("timeStamp", jsonData.getTimeStamp());
            jsonItem.put("message", jsonData.getMessage());
            jsonItem.put("author", jsonData.getAuthor());
            jsonItem.put("link", jsonData.getLink());
            items.put(jsonItem);
        }

        JSONObject mainInfo1 = new JSONObject();
        mainInfo1.put("idOutput", String.valueOf(outputId));
        mainInfo1.put("confId", String.valueOf(confId));
        mainInfo1.put("searchTimeStamp", timeStamp);
        mainInfo1.put("numDataItems", dataList.size());
        mainInfo1.put("DataItems", items);
        JSONObject jsonData1 = new JSONObject();
        jsonData1.put("SocialNetworksMonitoredData", mainInfo1);
        log.debug("Sending message " + jsonData1 + " to DataProvider on topic " + topic);
        this.kafka.sendMessage(jsonData1, topic);
    }

    public void ingestData(JSONArray items, String itemsLabel, String topic) {
        JSONObject jsonData = new JSONObject();
        jsonData.put(itemsLabel, items);
        log.debug("Sending message " + jsonData + " to DataProvider on topic " + topic);
        this.kafka.sendMessage(jsonData, topic);
    }

    public void ingestData(JSONObject data, String topic) {
        log.debug("Sending message " + data + " to DataProvider on topic " + topic);
        this.kafka.sendMessage(data, topic);
    }

    public void ingestData(String data, String topic) {
        log.debug("Sending message " + data + " to DataProvider on topic " + topic);
        JSONObject jsonData = new JSONObject(data);
        this.kafka.sendMessage(jsonData, topic);
    }
}
