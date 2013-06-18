package kom.vertx.hk2.beans.impl;

import kom.vertx.hk2.beans.TestBean;

/**
 * User: syungman
 * Date: 18.06.13
 */
public class TestBeanImpl implements TestBean {
    @Override
    public String getName() {
        return this.getClass().getName();
    }
}
