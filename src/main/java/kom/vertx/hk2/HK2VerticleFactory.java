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

import java.lang.reflect.Field;

/**
 * User: syungman
 * Date: 18.06.13
 */
public class HK2VerticleFactory implements VerticleFactory {
    private static final Logger logger = LoggerFactory.getLogger(HK2VerticleFactory.class);
    private static final String CONFIG_BOOTSTRAP_MAIN = "kom.vertx.hk2.BOOTSTRAP_MAIN";
    public static final String CONFIG_LOCATOR_NAME = "kom.vertx.hk2.LOCATOR_NAME";

    private Vertx vertx;
    private Container container;
    private ClassLoader cl;

    @Override
    public void init(Vertx vertx, Container container, ClassLoader cl) {
        this.vertx = vertx;
        this.container = container;
        this.cl = cl;
    }

    @Override
    public Verticle createVerticle(String main) throws Exception {
        final Verticle verticle = newVerticle(main);

        verticle.setVertx(vertx);
        verticle.setContainer(container);

        return verticle;
    }

    private Verticle newVerticle(String main) {
        final String serviceLocatorName = System.getProperty(CONFIG_LOCATOR_NAME, "kom.vertx.hk2.ServiceLocator");

        setDefaultServiceName(serviceLocatorName);

        final ServiceLocatorFactory factory = ServiceLocatorFactory.getInstance();
        final ServiceLocator locator = factory.create(serviceLocatorName);

        bind(locator, new VertxBinder(vertx, container));

        final Binder bootstrap = getBootstrap(System.getProperty(CONFIG_BOOTSTRAP_MAIN, null));
        if (bootstrap != null) {
            bind(locator, bootstrap);
        }

        return locator.createAndInitialize(loadClass(main, Verticle.class));
    }

    private void setDefaultServiceName(String containerName) {
        final ServiceLocatorFactory factory = new HK2ServiceLocatorFactoryImpl(containerName);
        final Class factoryClass = ServiceLocatorFactory.class;

        try {
            Field instance = factoryClass.getDeclaredField("INSTANCE");
            instance.setAccessible(true);
            instance.set(null, factory);
        } catch (NoSuchFieldException e) {
            logger.error("NoSuchFieldException while setting the service locator factory", e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException while setting the service locator factory", e);
            throw new RuntimeException(e);
        }
    }

    private Binder getBootstrap(String bootstrapClass) {
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

    private static void bind(ServiceLocator locator, Binder binder) {
        final DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        final DynamicConfiguration dc = dcs.createDynamicConfiguration();

        locator.inject(binder);
        binder.bind(dc);

        dc.commit();
    }

    private <T> Class<T> loadClass(String main, Class<T> targetClass) throws IllegalArgumentException {
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

    private Class<?> compileAndLoad(String className) throws ClassNotFoundException {
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

    private boolean isJavaSource(String main) {
        return main.endsWith(".java");
    }
}
