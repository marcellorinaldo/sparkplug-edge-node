package marcellorinaldo.sparkplug;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import marcellorinaldo.sparkplug.util.SparkplugPayloadUtil;
import marcellorinaldo.sparkplug.util.SparkplugTopicUtil;

public class EdgeNode implements MqttCallback {

    private static final Logger logger = LogManager.getLogger(EdgeNode.class.getName());

    private MqttTransport transport;
    private SparkplugTopicUtil topicUtil;
    private Optional<String> primaryHostId = Optional.empty();
    private String brokerUrl;
    private String clientId;
    private MqttConnectOptions options = new MqttConnectOptions();

    private AtomicInteger currentBdSeq = new AtomicInteger();
    private AtomicInteger nextBdSeq = new AtomicInteger();
    private AtomicInteger seq = new AtomicInteger();
    private long lastStateTimestamp = 0;

    private List<Device> devices = new ArrayList<>();

    /**
     * Initialized this Edge Node
     * @param properties a map of properties that contains the following elements:
     * namespace: String
     * group.id: String
     * node.id: String
     * primary.host.id: String
     * broker.url: String
     * client.id: String
     * keep.alive: int
     * username: String
     * password: String
     */
    public void initEdgeNode(Map<String, Object> properties) {
        logger.info("Initializing Edge Node with properties {}", properties.keySet());

        String namespace = (String) properties.get("namespace");
        String groupId = (String) properties.get("group.id");
        String nodeId = (String) properties.get("node.id");
        this.primaryHostId = Optional.ofNullable((String) properties.get("primary.host.id"));

        if (this.primaryHostId.isPresent()) {
            this.topicUtil = new SparkplugTopicUtil(namespace, groupId, nodeId, this.primaryHostId.get());
        } else {
            this.topicUtil = new SparkplugTopicUtil(namespace, groupId, nodeId);
        }

        this.brokerUrl = (String) properties.get("broker.url");
        this.clientId = (String) properties.get("client.id");
        this.options.setCleanSession(true);
        this.options.setKeepAliveInterval(((Double) properties.get("keep.alive")).intValue());
        this.options.setAutomaticReconnect(false);
        this.options.setUserName((String) properties.get("username"));
        this.options.setPassword(((String) properties.get("password")).toCharArray());

        logger.info("Edge Node initialized");
    }

    /**
     * Adds creates a new device and adds it to the list of managed devices of this Edge Node
     * if no device with the same device.id already exists
     * @param properties
     */
    public void initDevice(Map<String, Object> properties) {
        Device device = new Device(properties);

        if (this.devices.stream().allMatch(d -> !d.getDeviceId().equals(device.getDeviceId()))) {
            this.devices.add(device);
            logger.info("Initialized device '{}'", device.getDeviceId());
        }
    }

    /**
     * See spec chapter 5.4 - "Edge Node Session Establishment"
     * @param brokerUrl
     * @param clientId
     * @param keepAlive
     * @param username
     * @param password
     * @throws MqttException
     */
    public void estabilishSession() throws MqttException {
        connectTransport();

        this.transport.subscribe(this.topicUtil.getNodeCommandTopic(), 1);

        if (this.primaryHostId.isPresent()) {
            this.transport.subscribe(this.topicUtil.getStateTopic(), 1);
            // the BIRTH certificates will be published when valid online STATE is received
        } else {
            confirmSession();
        }
    }

    /**
     * See spec chapter 5.5 - "Edge Node Session Termination"
     * @throws MqttException
     */
    public void terminateSession() throws MqttException {
        if (this.transport != null && this.transport.isConnected()) {
            publishNodeDeathCertificate();
            this.transport.disconnect();
            logger.info("Session terminated");
        }
    }

    /*
     * MQTTCallback APIs
     */

    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("Connection lost", cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        logger.info("Received message on topic '{}'", topic);

        if (topic.contains("STATE") && this.primaryHostId.isPresent() && topic.contains(this.primaryHostId.get())) {
            handleStateMessage(message);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.debug("MQTT message with ID {} delivered", token.getMessageId());
    }

    /*
     * Private methods
     */

    private void connectTransport() throws MqttException {
        terminateSession();

        logger.info("Connecting transport layer");

        this.currentBdSeq.set(this.nextBdSeq.getAndIncrement());

        if (this.currentBdSeq.get() == 256) {
            this.currentBdSeq.set(0);
            this.nextBdSeq.set(0);
        }

        logger.info("bdSeq={}", this.currentBdSeq.get());

        this.options.setWill(topicUtil.getNodeDeathTopic(), SparkplugPayloadUtil.getNodeDeathPayload(this.currentBdSeq.get()), 1, false);

        this.transport = new MqttTransport(brokerUrl, clientId, this.options, this);
        this.transport.connect();

        logger.info("Transport layer connected");
    }

    private void handleStateMessage(MqttMessage message) {
        int qos = message.getQos();
        String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
        
        JsonElement json = JsonParser.parseString(payload);
        boolean isOnline = json.getAsJsonObject().get("online").getAsBoolean();
        long timestamp = json.getAsJsonObject().get("timestamp").getAsLong();

        if (qos != 1 || !message.isRetained()) {
            throw new IllegalArgumentException("Invalid STATE message");
        }

        if (this.lastStateTimestamp <= timestamp) {
            this.lastStateTimestamp = timestamp;

            if (isOnline) {                
                logger.warn("Primary host application is online, confirming session");
                confirmSession();
            } else {
                logger.warn("Primary host application is offline, re-estabilishing connection");
                new Thread(() -> {
                    try {
                        estabilishSession();
                    } catch (Exception e) {
                        logger.error("Error estabilishing session", e);
                    }
                }).start();
            }
        }
    }

    /**
     * See spec chapter 5.6. - Device Session Establishment
     */
    private void estabilishDevicesSessions() {
        for (Device device : this.devices) {
            if (device.supportWriteToOutput()) {
                this.transport.subscribe(this.topicUtil.getDeviceCommandTopic(device.getDeviceId()), 1);
            }

            publishDeviceBirthCertificate(device);
        }
    }

    private void confirmSession() {
        publishNodeBirthCertificate();
        estabilishDevicesSessions();
        logger.info("Session estabilished");
    }

    private synchronized void publishNodeDeathCertificate() {
        this.transport.publish(this.topicUtil.getNodeDeathTopic(), 0, false, SparkplugPayloadUtil.getNodeDeathPayload(this.currentBdSeq.get()));
    }

    public synchronized void publishNodeBirthCertificate() {
        this.seq.set(0);
        this.transport.publish(this.topicUtil.getNodeBirthTopic(), 0, false, SparkplugPayloadUtil.getNodeBirthPayload(this.currentBdSeq.get(), this.seq.getAndIncrement()));
    }

    public synchronized void publishDeviceBirthCertificate(Device device) {
        if (this.seq.get() == 256) {
            this.seq.set(0);
        }
        this.transport.publish(this.topicUtil.getDeviceBirthTopic(device.getDeviceId()), 0, false, SparkplugPayloadUtil.getDeviceBirthPayload(this.seq.getAndIncrement(), device.getMetrics()));
    }

    /*
    public void publishDeviceDeathCertificate(Device device) {
        if (this.seq.get() == 256) {
            this.seq.set(0);
        }

        this.transport.publish(this.topicUtil.getDeviceDeathTopic(device.getDeviceId()), 0, false, SparkplugPayloadUtil.getDeviceBirthPayload(this.seq.getAndIncrement(), device.getMetrics()));
    }
    */

}
