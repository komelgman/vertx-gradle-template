package kom.vertx.hk2.resources;

import kom.vertx.hk2.beans.TestBean;
import org.vertx.java.core.Vertx;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class RootResource {

    @Inject
    private Vertx vertx;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "ok" + vertx.getClass().getName();
    }
}