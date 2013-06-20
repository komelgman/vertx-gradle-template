package kom.vertx.hk2;

import kom.vertx.jersey.JerseyHandler;
import org.glassfish.jersey.server.ResourceConfig;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;
import org.vertx.java.platform.impl.cli.Starter;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;

import static kom.vertx.hk2.HK2VerticleFactory.CONFIG_BOOTSTRAP_MAIN;

/**
 * User: syungman
 * Date: 10.06.13
 */
public class RestServer extends Verticle {

    @Inject @Named("Server port")
    private Integer port;

    @Inject @Named("Rest handler config")
    private ResourceConfig jerseyConfig;


    @Inject @Named("Base URI")
    private URI baseUri;

    @Override
    public void start() {
        String restHandlerPath = "/rest";
        JerseyHandler handler = new JerseyHandler(baseUri.resolve(restHandlerPath), jerseyConfig);

        vertx
            .createHttpServer()
            .requestHandler(new RouteMatcher().all(restHandlerPath + ".*", handler))
            .listen(port);
    }

    public static void main(String[] args) {
        System.setProperty(CONFIG_BOOTSTRAP_MAIN, VerticleBootstrap.class.getName());

        Starter.main(new String[] {
                "run", RestServer.class.getName()
        });
    }
}
