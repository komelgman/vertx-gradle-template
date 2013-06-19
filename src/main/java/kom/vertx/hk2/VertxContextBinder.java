package kom.vertx.hk2;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import javax.inject.Inject;


class VertxContextBinder extends AbstractBinder {

    private final ClassLoader cl;
    private final Vertx vertx;
    private final Container container;

    public VertxContextBinder(ClassLoader cl, Vertx vertx, Container container) {
        this.cl = cl;
        this.vertx = vertx;
        this.container = container;
    }

    @Override
    protected void configure() {
        bind(vertx).to(Vertx.class);
        bind(container).to(Container.class);
        bind(cl).to(ClassLoader.class);
    }
}