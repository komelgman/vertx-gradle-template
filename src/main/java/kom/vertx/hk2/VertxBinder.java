package kom.vertx.hk2;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;


class VertxBinder extends AbstractBinder {

    private final Vertx vertx;
    private final Container container;

    public VertxBinder(Vertx vertx, Container container) {
        this.vertx = vertx;
        this.container = container;
    }

    @Override
    protected void configure() {
        bind(vertx).to(Vertx.class);
        bind(container).to(Container.class);
    }
}