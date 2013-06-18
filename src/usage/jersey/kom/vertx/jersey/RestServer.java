package kom.vertx.jersey;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;
import org.vertx.java.platform.impl.cli.Starter;

import java.net.URI;

/**
 * User: syungman
 * Date: 10.06.13
 */
public class RestServer {
    public static void main(String[] args) {
        Starter.main(new String[] {
                "run", VertxEntry.class.getName()
        });
    }

    public static class VertxEntry extends Verticle {
        @Override
        public void start() {
            final RouteMatcher rm = new RouteMatcher();
            rm.all("/rest.*", packageResourceHandler("http://localhost:9090/rest", "kom/vertx/jersey/resources"));

            vertx
                .createHttpServer()
                .requestHandler(rm)
                .listen(9090);
        }
    }

    public static JerseyHandler packageResourceHandler(String baseUri, String ... packages) {
        final ResourceConfig config = new ResourceConfig();
        config
                .registerFinder(new PackageNamesScanner(packages, true))
                .register(JacksonFeature.class);

        return new JerseyHandler(URI.create(baseUri), config);
    }
}
