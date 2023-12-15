package marcellorinaldo.sparkplug.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class Launcher {

    private static final Logger logger = LogManager.getLogger(Launcher.class.getName());

    public static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws Exception {
        ServletContextHandler handler = getServletContextHandler();
        Server server = new Server(SERVER_PORT);
        try {
            server.setHandler(handler);
            server.start();

            logger.info("Server started at port {}", SERVER_PORT);

            server.join();
        } finally {
            server.stop();
            server.destroy();
        }
    }

    private static ServletContextHandler getServletContextHandler() {
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.setContextPath("/");

        ServletHolder servletHolder = handler.addServlet(ServletContainer.class, "/*");
        servletHolder.setInitOrder(0);
        servletHolder.setInitParameter(
            "jersey.config.server.provider.packages",
            "marcellorinaldo.sparkplug.rest"
        );

        return handler;
    }

}