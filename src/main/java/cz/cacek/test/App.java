package cz.cacek.test;

import static com.hazelcast.jet.datamodel.Tuple2.tuple2;
import static java.util.concurrent.TimeUnit.HOURS;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import com.hazelcast.config.MapConfig;
import com.hazelcast.internal.json.Json;
import com.hazelcast.internal.json.JsonObject;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.SourceBuilder;
import com.hazelcast.jet.pipeline.StreamSource;

/**
 * Hazelcast Hello world!
 */
public class App {

    public static void main(String[] args) throws InterruptedException {
        StreamSource<JsonObject> mqttSource = SourceBuilder
                .stream("mqtt-source", ctx -> newMqttClient())
                .<JsonObject>fillBufferFn((mqttc, buf) -> {
                    mqttc.subscribe("gps/#", (topic, msg)->buf.add(Json.parse(new String(msg.getPayload())).asObject()));
                })
                .destroyFn(mqttc->destroyMqttClient(mqttc))
                .build();

        Pipeline p = Pipeline.create();
        p.drawFrom(mqttSource)
            .withoutTimestamps()
            .map(App::jsonToEntry)
            .drainTo(Sinks.map("gps"));

        JetConfig config = new JetConfig();
        config.getHazelcastConfig().addMapConfig(new MapConfig().setName("gps").setTimeToLiveSeconds((int) HOURS.toSeconds(6)));
        JetInstance jet = Jet.newJetInstance(config);
        try {
            // Perform the computation
            jet.newJob(p);

            // Check the results
            Map<String, Long> gps = jet.getMap("gps");
            while (true) {
                TimeUnit.SECONDS.sleep(15);
                System.out.println("Count of vehicle numbers: " + gps.size());
            }
//            System.out.println("Topics");
//            for (Map.Entry<String, Long> e: gps.entrySet()) {
//                System.out.println(e.getKey() + ": " + e.getValue());
//            }
        } finally {
            Jet.shutdownAll();
        }
    }

    private static void destroyMqttClient(IMqttClient mqttc) throws MqttException {
        try {
            mqttc.disconnect();
            mqttc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static IMqttClient newMqttClient() throws MqttException, MqttSecurityException {
        IMqttClient client = new MqttClient("tcp://171.111.158.2:1883",UUID.randomUUID().toString());
        client.connect();
        return client;
    }

    private static Map.Entry<String, CarInfo> jsonToEntry(JsonObject json) {
        CarInfo car = new CarInfo();
        car.lng = Double.parseDouble(json.getString("lng", "0.0"));
        car.lat = Double.parseDouble(json.getString("lat", "0.0"));
        return tuple2(json.getString("vehicle_no", "UNDEFINED"), car);
    }
    
    public static class CarInfo implements Serializable{
        public double lat;
        public double lng;
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("CarInfo [lat=").append(lat).append(", lng=").append(lng).append("]");
            return builder.toString();
        }
    }
}
