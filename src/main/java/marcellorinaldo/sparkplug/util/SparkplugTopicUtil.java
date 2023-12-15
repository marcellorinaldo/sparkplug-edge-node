package marcellorinaldo.sparkplug.util;

public class SparkplugTopicUtil {

    private String namespace;
    private String groupId;
    private String nodeId;
    private String primaryHostId;
    
    public SparkplugTopicUtil(String namespace, String groupId, String nodeId) {
        this.namespace = namespace;
        this.groupId = groupId;
        this.nodeId = nodeId;
    }

    public SparkplugTopicUtil(String namespace, String groupId, String nodeId, String primaryHostId) {
        this.namespace = namespace;
        this.groupId = groupId;
        this.nodeId = nodeId;
        this.primaryHostId = primaryHostId;
    }

    public String getNodeDeathTopic() {
        return String.format("%s/%s/NDEATH/%s", this.namespace, this.groupId, this.nodeId);
    }

    public String getStateTopic() {
        return String.format("%s/STATE/%s", this.namespace, this.primaryHostId);
    }

    public String getNodeCommandTopic() {
        return String.format("%s/%s/NCMD/%s", this.namespace, this.groupId, this.nodeId);
    }

    public String getNodeBirthTopic() {
        return String.format("%s/%s/NBIRTH/%s", this.namespace, this.groupId, this.nodeId);
    }

    public String getDeviceCommandTopic(String deviceId) {
        return String.format("%s/%s/DCMD/%s/%s", this.namespace, this.groupId, this.nodeId, deviceId);
    }

    public String getDeviceBirthTopic(String deviceId) {
        return String.format("%s/%s/DBIRTH/%s/%s", this.namespace, this.groupId, this.nodeId, deviceId);
    }

    public String getDeviceDeathTopic(String deviceId) {
        return String.format("%s/%s/DDEATH/%s/%s", this.namespace, this.groupId, this.nodeId, deviceId);
    }

}
