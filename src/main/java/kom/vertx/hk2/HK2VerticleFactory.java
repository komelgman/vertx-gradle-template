package kom.vertx.hk2;

import org.glassfish.hk2.api.*;
import org.glassfish.hk2.utilities.Binder;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;
import org.vertx.java.platform.VerticleFactory;

import static java.lang.System.getProperty;
import static kom.utils.ClassLoadHelper.loadClass;
import static kom.utils.ServiceLocatorHelper.bind;

/**
 * User: syungman
 * Date: 18.06.13
 */
public class HK2VerticleFactory implements VerticleFactory {

    public static final String CONFIG_BOOTSTRAP_MAIN = "kom.vertx.hk2.BOOTSTRAP_MAIN";
    public static final String CONFIG_LOCATOR_NAME = "kom.vertx.hk2.LOCATOR_NAME";
    public static final String DEFAULT_LOCATOR_NAME = "kom.vertx.hk2.ServiceLocator";

    protected static final Logger logger = LoggerFactory.getLogger(HK2VerticleFactory.class);

    protected Vertx vertx;
    protected Container container;
    protected ClassLoader cl;

    protected ServiceLocator locator;
    protected String serviceLocatorName;
    protected Factory<Binder> bootstrapProvider;


    @SuppressWarnings("UnusedDeclaration")
    public HK2VerticleFactory() {
        this(getProperty(CONFIG_LOCATOR_NAME, DEFAULT_LOCATOR_NAME),
                getProperty(CONFIG_BOOTSTRAP_MAIN, null));
    }

    public HK2VerticleFactory(String serviceLocatorName, String bootstrapName) {
        this.serviceLocatorName = serviceLocatorName;
        this.bootstrapProvider = new BootstrapProvider(this, bootstrapName);
    }

    @SuppressWarnings("UnusedDeclaration")
    public HK2VerticleFactory(String serviceLocatorName, Binder bootstrap) {
        this.serviceLocatorName = serviceLocatorName;
        this.bootstrapProvider = new BootstrapProvider(this, bootstrap);
    }

    @Override
    public void init(Vertx vertx, Container container, ClassLoader cl) {
        this.vertx = vertx;
        this.container = container;
        this.cl = cl;

        buildServiceLocator();
    }

    protected void buildServiceLocator() {
        this.locator = ServiceLocatorFactory.getInstance()
                .create(serviceLocatorName);

        bind(locator, new VertxContextBinder(cl, vertx, container));
        bind(locator, bootstrapProvider.provide());
    }

    @Override
    public Verticle createVerticle(String main) throws Exception {
        final Verticle verticle = loadAndCreateVerticleInstance(main);

        verticle.setVertx(vertx);
        verticle.setContainer(container);

        return verticle;
    }

    protected Verticle loadAndCreateVerticleInstance(String main) {
        return locator.createAndInitialize(loadClass(cl, main, Verticle.class));
    }

    @Override
    public void reportException(Logger logger, Throwable t) {
        logger.error("Exception in Java HK2 verticle", t);
    }

    @Override
    public void close() {
        // nothing
    }
}