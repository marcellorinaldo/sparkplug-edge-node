package marcellorinaldo.sparkplug.util;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import marcellorinaldo.sparkplug.protobuf.SparkplugBProto.DataType;
import marcellorinaldo.sparkplug.protobuf.SparkplugBProto.Payload;

public class SparkplugPayloadUtil {

    private SparkplugPayloadUtil() {}

    public static byte[] getNodeDeathPayload(int bdSeq) {
        long timestamp = new Date().getTime();

        Payload.Builder protoMsg = Payload.newBuilder();
        
        Payload.Metric.Builder bdSeqMetric = Payload.Metric.newBuilder();
        bdSeqMetric.setName("bdSeq");
        bdSeqMetric.setLongValue(bdSeq);
        bdSeqMetric.setDatatype(DataType.Int64.getNumber());
        bdSeqMetric.setTimestamp(timestamp);
        protoMsg.addMetrics(bdSeqMetric.build());

        protoMsg.setTimestamp(timestamp);
  
        return protoMsg.build().toByteArray();
    }

    public static byte[] getNodeBirthPayload(int bdSeq, int seq) {
        long timestamp = new Date().getTime();

        Payload.Builder protoMsg = Payload.newBuilder();

        Payload.Metric.Builder bdSeqMetric = Payload.Metric.newBuilder();
        bdSeqMetric.setName("bdSeq");
        bdSeqMetric.setLongValue(bdSeq);
        bdSeqMetric.setDatatype(DataType.Int64.getNumber());
        bdSeqMetric.setTimestamp(timestamp);
        protoMsg.addMetrics(bdSeqMetric.build());

        Payload.Metric.Builder rebirthMetric = Payload.Metric.newBuilder();
        rebirthMetric.setName("Node Control/Rebirth");
        rebirthMetric.setBooleanValue(false);
        rebirthMetric.setDatatype(DataType.Boolean_VALUE);
        protoMsg.addMetrics(rebirthMetric.build());
        
        protoMsg.setSeq(seq);
        protoMsg.setTimestamp(timestamp);

        return protoMsg.build().toByteArray();
    }

    public static byte[] getDeviceBirthPayload(int seq, Map<String, String> metrics) {
        long timestamp = new Date().getTime();

        Payload.Builder protoMsg = Payload.newBuilder();

        for (Entry<String, String> metricEntry : metrics.entrySet()) {
            Payload.Metric.Builder metric = Payload.Metric.newBuilder();
            metric.setName(metricEntry.getKey());
            metric.setStringValue(metricEntry.getValue());
            metric.setDatatype(DataType.String_VALUE);
            metric.setTimestamp(timestamp);
            metric.setIsHistorical(false);
            protoMsg.addMetrics(metric.build());
        }

        protoMsg.setSeq(seq);
        protoMsg.setTimestamp(timestamp);

        return protoMsg.build().toByteArray();
    }
}
