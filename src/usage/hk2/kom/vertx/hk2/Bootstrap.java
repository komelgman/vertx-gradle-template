package kom.vertx.hk2;

import kom.vertx.hk2.beans.TestBean;
import kom.vertx.hk2.beans.impl.TestBeanImpl;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * User: syungman
 * Date: 18.06.13
 */
public class Bootstrap extends AbstractBinder {
    @Override
    protected void configure() {
        bind(TestBeanImpl.class).to(TestBean.class);
    }
}
