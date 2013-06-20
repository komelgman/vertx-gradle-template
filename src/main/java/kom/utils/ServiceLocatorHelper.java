package kom.utils;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.Binder;

/**
 * User: syungman
 * Date: 20.06.13
 */
public class ServiceLocatorHelper {
    public static void bind(ServiceLocator locator, Binder ... binders) {
        final DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        final DynamicConfiguration dc = dcs.createDynamicConfiguration();

        for (Binder binder : binders) {
            if (binder == null)
                continue;

            locator.inject(binder);
            binder.bind(dc);
        }

        dc.commit();
    }
}
