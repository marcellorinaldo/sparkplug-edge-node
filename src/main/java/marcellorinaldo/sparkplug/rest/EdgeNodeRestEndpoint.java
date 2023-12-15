package marcellorinaldo.sparkplug.rest;

import java.lang.reflect.Type;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/")
public class EdgeNodeRestEndpoint {

    private static final Logger logger = LogManager.getLogger(EdgeNodeRestEndpoint.class.getName());

    @Path("test")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response test() {
        logger.info("Got request GET@test");
        return Response.ok("Rest endpoint works!").build();
    }

    @Path("node/init")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response initNode(String jsonString) {
        logger.info("Got request POST@node/init");

        Gson gson = new Gson();
        Type propertiesType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> properties = gson.fromJson(jsonString, propertiesType);

        try {
            EdgeNodeInstance.initEdgeNode(properties);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Error initializing Edge Node", e);
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    @Path("device/init")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response initDevice(String jsonString) {
        logger.info("Got request POST@device/init");

        Gson gson = new Gson();
        Type propertiesType = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> properties = gson.fromJson(jsonString, propertiesType);

        try {
            EdgeNodeInstance.initDevice(properties);
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Error initializing Device", e);
            return Response.status(Status.BAD_REQUEST).build();
        }
    }

    @Path("node/start")
    @POST
    public Response start() {
        logger.info("Got request POST@node/start");

        try {
            EdgeNodeInstance.estabilishSession();
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Error estabilishing session", e);
            return Response.serverError().build();
        }
    }

    @Path("node/stop")
    @POST
    public Response stop() {
        logger.info("Got request POST@node/stop");

        try {
            EdgeNodeInstance.terminateSession();
            return Response.ok().build();
        } catch (Exception e) {
            logger.error("Error estabilishing session", e);
            return Response.serverError().build();
        }
    }

}
