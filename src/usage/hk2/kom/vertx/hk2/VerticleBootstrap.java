package kom.vertx.hk2;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.internal.scanning.PackageNamesScanner;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import javax.inject.Inject;
import java.net.URI;

/**
 * User: syungman
 * Date: 18.06.13
 */
public class VerticleBootstrap extends AbstractBinder {

    @Inject private Vertx vertx;
    @Inject private Container container;
    @Inject private ClassLoader classLoader;

    @Override
    protected void configure() {
        final String[] packages = {
                "kom/vertx/hk2/resources"
        };

        final ResourceConfig restConfig = new ResourceConfig()
                .registerFinder(new PackageNamesScanner(classLoader, packages, true))
                .register(JacksonFeature.class)

                // rebind context
                .register(new VertxContextBinder(classLoader, vertx, container));

        bind(restConfig).named("Rest handler config");

        bind(9090).named("Server port");
        bind(URI.create("http://localhost:9090/")).named("Base URI");
    }
}