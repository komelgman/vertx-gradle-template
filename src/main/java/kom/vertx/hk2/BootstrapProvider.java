package kom.vertx.hk2;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.Binder;

import static kom.utils.ClassLoadHelper.loadClass;

/**
* User: syungman
* Date: 20.06.13
*/
public class BootstrapProvider implements Factory<Binder> {
    private String bootstrapName;
    private Binder bootstrap;
    private HK2VerticleFactory hk2VerticleFactory;

    public BootstrapProvider(HK2VerticleFactory hk2VerticleFactory, String bootstrapName) {
        this.hk2VerticleFactory = hk2VerticleFactory;
        this.bootstrapName = bootstrapName;
    }

    public BootstrapProvider(HK2VerticleFactory hk2VerticleFactory, Binder bootstrap) {
        this.hk2VerticleFactory = hk2VerticleFactory;
        this.bootstrap = bootstrap;
    }

    @Override
    public Binder provide() {
        if (bootstrap == null && bootstrapName != null) {
            try {
                bootstrap = loadClass(hk2VerticleFactory.cl, bootstrapName, Binder.class).newInstance();
            } catch (Exception e) {
                HK2VerticleFactory.logger.warn("Can't load bootstrap " + bootstrapName, e);
            }
        }

        return bootstrap;
    }

    @Override
    public void dispose(Binder instance) {
        // nothing
    }
}
