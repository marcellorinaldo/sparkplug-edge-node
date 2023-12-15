# Sparkplug Edge Node implementation

An implementation of the Sparkplug Edge Node. Draft version.

To generate the base protobuf `sparkplug_b` Java library:

```bash
protoc --proto_path=src/main/java --java_out=src/main/java src/main/java/marcellorinaldo/sparkplug/protobuf/sparkplug_b.proto
```

The application exposes some REST endpoints to perform some simple tests. Refer to the [Sparkplug TCK](https://github.com/eclipse-sparkplug/sparkplug/blob/master/tck/README.md) for test environment setup.

Compile the project with:

```bash
mvn clean install
```

Then run the application with:

```bash
java -jar target/sparkplug-edge-node-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Set the initialization parameters:

```
POST @ http://localhost:8080/node/init
```
```json
{
    "namespace": "spBv1.0",
    "group.id": "mygroup",
    "node.id": "n1",
    "primary.host.id": "h1", // optional
    "broker.url": "tcp://localhost:1883",
    "client.id": "tck-edge-node",
    "keep.alive": 60,
    "username": "mqtt",
    "password": "mqtt"
}
```

Add devices by repeatedly calling:

```
POST @ http://localhost:8080/device/init
```
```json
{
    "device.id": "d1",
    "write.outputs.support": true,
    "metric.m1" : "true",
    "metric.m2" : "20",
    "metric.m3": "a string"
}
```

Start the session estabilishment procedure:

```
POST @ http://localhost:8080/node/start
```

Terminate the session:

```
POST @ http://localhost:8080/node/stop
```