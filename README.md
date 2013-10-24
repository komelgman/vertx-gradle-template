# Vertex 2 with Jersey 2 integration 

// todo

## Example
```java
public class RestServer {
    public static void main(String[] args) {
        Starter.main(new String[] {
                "run", VertxEntry.class.getName()
        });
    }

    public static class VertxEntry extends Verticle {
        @Override
        public void start() {
            final RouteMatcher rm = new RouteMatcher();
            rm.all("/rest.*", packageResourceHandler("http://localhost:9090/rest", "kom/vertx/jersey/resources"));

            vertx
                .createHttpServer()
                .requestHandler(rm)
                .listen(9090);
        }
    }

    public static JerseyHandler packageResourceHandler(String baseUri, String ... packages) {
        final ResourceConfig config = new ResourceConfig();
        config
                .registerFinder(new PackageNamesScanner(packages, true))
                .register(JacksonFeature.class);

        return new JerseyHandler(URI.create(baseUri), config);
    }
}
```
