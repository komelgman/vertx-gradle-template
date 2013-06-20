package kom.vertx.hk2;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.Binder;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Container;
import org.vertx.java.platform.Verticle;
import org.vertx.java.platform.VerticleFactory;
import org.vertx.java.platform.impl.java.CompilingClassLoader;

import static java.lang.System.getProperty;

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
    protected String bootstrapName;


    @SuppressWarnings("UnusedDeclaration")
    public HK2VerticleFactory() {
        this(getProperty(CONFIG_LOCATOR_NAME, DEFAULT_LOCATOR_NAME),
                getProperty(CONFIG_BOOTSTRAP_MAIN, null));
    }

    public HK2VerticleFactory(String serviceLocatorName, String bootstrapName) {
        this.serviceLocatorName = serviceLocatorName;
        this.bootstrapName = bootstrapName;
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
        bind(locator, newBootstrap(bootstrapName));
    }

    @Override
    public Verticle createVerticle(String main) throws Exception {
        final Verticle verticle = locator.createAndInitialize(loadClass(main, Verticle.class));

        verticle.setVertx(vertx);
        verticle.setContainer(container);

        return verticle;
    }

    protected Binder newBootstrap(String bootstrapClass) {
        if (bootstrapClass == null) {
            return null;
        }

        try {
            return loadClass(bootstrapClass, Binder.class).newInstance();
        } catch (Exception e) {
            logger.warn("Can't load bootstrap " + bootstrapClass, e);
        }

        return null;
    }

    protected void bind(ServiceLocator locator, Binder binder) {
        if (binder == null) {
            return;
        }

        final DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        final DynamicConfiguration dc = dcs.createDynamicConfiguration();

        locator.inject(binder);
        binder.bind(dc);

        dc.commit();
    }

    protected <T> Class<T> loadClass(String main, Class<T> targetClass) throws IllegalArgumentException {
        Class<?> result;

        try {
            if (isJavaSource(main)) {
                    result = compileAndLoad(main);
            } else {
                    result = cl.loadClass(main);
            }
        } catch (ClassNotFoundException e) {
            final String message = "Class " + main + " not found";
            logger.error(message);
            throw new IllegalArgumentException(message);
        }

        if (!targetClass.isAssignableFrom(result)) {
            final String message = "Class " + main
                    + " does not implement " + targetClass.getName();

            logger.error(message);
            throw new IllegalArgumentException(message);
        }

        //noinspection unchecked
        return (Class<T>)result;
    }

    protected Class<?> compileAndLoad(String className) throws ClassNotFoundException {
        CompilingClassLoader compilingLoader = new CompilingClassLoader(cl, className);
        return compilingLoader.loadClass(compilingLoader.resolveMainClassName());
    }

    @Override
    public void reportException(Logger logger, Throwable t) {
        logger.error("Exception in Java HK2 verticle", t);
    }

    @Override
    public void close() {
        // nothing
    }

    protected boolean isJavaSource(String main) {
        return main.endsWith(".java");
    }
}