package kom.vertx.hk2;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extension.ServiceLocatorGenerator;
import org.glassfish.hk2.internal.ServiceLocatorFactoryImpl;

/**
 * User: syungman
 * Date: 18.06.13
 */
public class HK2ServiceLocatorFactoryImpl extends ServiceLocatorFactoryImpl {

    private final String defaultName;

    public HK2ServiceLocatorFactoryImpl(String defaultName) {
        this.defaultName = defaultName;
    }

    @Override
    public ServiceLocator create(String name, ServiceLocator parent, ServiceLocatorGenerator generator) {
        if (name == null) {
            name = defaultName;
        }

        return super.create(name, parent, generator);
    }
}