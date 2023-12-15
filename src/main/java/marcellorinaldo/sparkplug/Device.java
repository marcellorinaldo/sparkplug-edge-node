package marcellorinaldo.sparkplug;

import java.util.HashMap;
import java.util.Map;

public class Device {

    private String deviceId;
    private boolean supportWriteToOutput;
    private Map<String, String> metrics = new HashMap<>();
    
    /**
     * Initialized a device
     * @param properties a map of properties that contains the following elements:
     * device.id: String
     * write.outputs.support: boolean
     * metric.*: denotes a string metric, the part after '.' will be used as key
     */
    public Device(Map<String, Object> properties) {
        this.deviceId = (String) properties.get("device.id");
        this.supportWriteToOutput = (boolean) properties.get("write.outputs.support");
        properties.forEach((name, value) -> {
            if (name.startsWith("metric.")) {
                Device.this.metrics.put(name, (String) value);
            }
        });
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public boolean supportWriteToOutput() {
        return this.supportWriteToOutput;
    }

    public Map<String, String> getMetrics() {
        return this.metrics;
    }

}
