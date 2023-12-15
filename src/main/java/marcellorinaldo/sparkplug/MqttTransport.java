package marcellorinaldo.sparkplug;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttTransport {

    private static final Logger logger = LogManager.getLogger(MqttTransport.class.getName());

    private MqttClient client;
    private MqttConnectOptions options;
    
    /**
     * Initialize the Paho MQTT Client
     * @param brokerUrl the URL of the MQTT broker
     * @param clientId the client identifier to use for connection
     * @param options the {@link MqttConnectOptions} containing all the parameters (LWT, keepAlive, etc.) to use for connection
     * @throws MqttException if the initialization fails
     */
    public MqttTransport(String brokerUrl, String clientId, MqttConnectOptions options, MqttCallback callback) throws MqttException {
        this.client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        this.client.setCallback(callback);
        this.options = options;
        logger.info("Paho MQTT Client initialized");
    }

    public void connect() throws MqttException {
        if (!this.client.isConnected()) {
            logger.info("Connecting Paho MQTT Client to broker '{}' and client ID '{}' with options: {}", this.client.getServerURI(), this.client.getClientId(), this.options);
            this.client.connect(this.options);
            logger.info("Paho MQTT Client connected");
        }
    }

    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    public void disconnect() throws MqttException {
        this.client.disconnect();
        logger.info("Paho MQTT Client disconnected");
    }

    public void publish(String topic, int qos, boolean retained, byte[] payload) {
        try {
            if (this.client.isConnected()) {
                MqttMessage message = new MqttMessage(payload);
                message.setQos(qos);
                message.setRetained(retained);
                this.client.publish(topic, message);

                logger.info("Published message on topic '{}' with QoS {} and retain {}", topic, qos, retained);
            } else {
                logger.error("Paho MQTT Client not connected, message is not published");
            }
        } catch (MqttException e) {
            logger.error("Error publishing", e);
        }
    }

    public void subscribe(String topicFilter, int qos) {
        try {
            if (this.client.isConnected()) {
                this.client.unsubscribe(topicFilter);
                this.client.subscribe(topicFilter, qos);
                logger.info("Subscribed to topic '{}' with QoS {}", topicFilter, qos);
            } else {
                logger.error("Paho MQTT Client not connected, not subscribed");
            }
        } catch (MqttException e) {
            logger.error("Error subscribing", e);
        }
    }

}
