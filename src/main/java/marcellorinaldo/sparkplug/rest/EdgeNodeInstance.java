package marcellorinaldo.sparkplug.rest;

import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttException;

import marcellorinaldo.sparkplug.EdgeNode;

public class EdgeNodeInstance {

    private EdgeNodeInstance() {}

    private static EdgeNode node = new EdgeNode();

    public static void initEdgeNode(Map<String, Object> properties) {
        node.initEdgeNode(properties);
    }

    public static void initDevice(Map<String, Object> properties) {
        node.initDevice(properties);
    }

    public static void estabilishSession() throws MqttException {
        node.estabilishSession();
    }

    public static void terminateSession() throws MqttException {
        node.terminateSession();
    }
    
}
