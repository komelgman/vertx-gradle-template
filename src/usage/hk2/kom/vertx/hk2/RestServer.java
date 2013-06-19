package kom.vertx.hk2;

import kom.vertx.hk2.beans.TestBean;
import kom.vertx.jersey.JerseyHandler;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;
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
