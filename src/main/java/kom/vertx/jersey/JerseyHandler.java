package kom.vertx.jersey;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import org.glassfish.jersey.internal.MapPropertiesDelegate;
import org.glassfish.jersey.server.*;
import org.glassfish.jersey.server.internal.JerseyRequestTimeoutHandler;
import org.glassfish.jersey.server.spi.ContainerResponseWriter;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.SecurityContext;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.StatusType;

public class JerseyHandler implements Handler<HttpServerRequest> {

    private final static DefaultSecurityContext DEFAULT_SECURITY_CONTEXT = new DefaultSecurityContext();

    private final ApplicationHandler jerseyRequestHandler;
    private final URI baseUri;

    public JerseyHandler(URI baseUri, ResourceConfig jerseyConfig) {
        this.baseUri = baseUri;
        this.jerseyRequestHandler = new ApplicationHandler(jerseyConfig);
    }

    @Override
    public void handle(final HttpServerRequest vertxRequest) {
        final ContainerRequest jerseyRequest = newJerseyRequest(baseUri, vertxRequest);
        final Buffer buffer = new Buffer();
        final ResponseWriter responseWriter = new ResponseWriter(vertxRequest.response());

        jerseyRequest.setEntityStream(new ByteBufInputStream(buffer.getByteBuf()));
        jerseyRequest.setWriter(responseWriter);

        vertxRequest.dataHandler(new Handler<Buffer>() {
            @Override
            public void handle(Buffer event) {
                buffer.appendBuffer(event);
            }
        });

        vertxRequest.endHandler(new VoidHandler() {
            @Override
            protected void handle() {
                try {
                    jerseyRequestHandler.handle(jerseyRequest);
                } finally {
                    responseWriter.closeAndLogWarning();
                }
            }
        });
    }

    private ContainerRequest newJerseyRequest(URI baseUri, HttpServerRequest vertxRequest) {
        final ContainerRequest result = new ContainerRequest(baseUri, baseUri.resolve(vertxRequest.uri()),
                vertxRequest.method(), getSecurityContext(vertxRequest), new MapPropertiesDelegate());

        copyHttpHeadersFromTo(vertxRequest, result);

        return result;
    }

    private void copyHttpHeadersFromTo(HttpServerRequest from, ContainerRequest to) {
        final MultivaluedMap<String, String> headers = to.getHeaders();
        for (Map.Entry<String, String> sourceHeader : from.headers()) {
            headers.putSingle(sourceHeader.getKey(), sourceHeader.getValue());
        }
    }

    @SuppressWarnings("UnusedParameters")
    protected SecurityContext getSecurityContext(HttpServerRequest vertxRequest) {
        return DEFAULT_SECURITY_CONTEXT;
    }


    private static class ResponseWriter implements ContainerResponseWriter {

        private final HttpServerResponse response;
        private final Buffer responseBuffer;
        private final AtomicBoolean closed;
        private final JerseyRequestTimeoutHandler requestTimeoutHandler;

        public ResponseWriter(HttpServerResponse response) {
            this.response = response;
            this.closed = new AtomicBoolean(false);
            this.responseBuffer = new Buffer();
            this.requestTimeoutHandler = new JerseyRequestTimeoutHandler(this);
        }

        @Override
        public OutputStream writeResponseStatusAndHeaders(long contentLength, ContainerResponse context)
                throws ContainerException {

            final StatusType statusInfo = context.getStatusInfo();
            response.setStatusCode(statusInfo.getStatusCode());
            response.setStatusMessage(statusInfo.getReasonPhrase());
            response.setChunked(context.isChunked());

            final MultivaluedMap<String, String> sourceHeaders = context.getStringHeaders();
            final MultiMap responseHeaders = response.headers();
            for (final Map.Entry<String, List<String>> e : sourceHeaders.entrySet()) {
                responseHeaders.add(e.getKey(), e.getValue());
            }

            return new ByteBufOutputStream(responseBuffer.getByteBuf());
        }

        @Override
        public void commit() {
            if (closed.compareAndSet(false, true)) {
                response.end(responseBuffer);
            }
        }

        @Override
        public void failure(Throwable error) {
            response.setStatusCode(INTERNAL_SERVER_ERROR.code());
            response.setStatusMessage(INTERNAL_SERVER_ERROR.reasonPhrase());

            commit();
            rethrow(error);
        }

        /**
         * Rethrow the original exception as required by JAX-RS, 3.3.4
         *
         * @param error throwable to be re-thrown
         */
        private void rethrow(Throwable error) {
            if (error instanceof RuntimeException) {
                throw (RuntimeException) error;
            } else {
                throw new ContainerException(error);
            }
        }

        @Override
        public boolean suspend(long time, TimeUnit unit, TimeoutHandler handler) {
            return requestTimeoutHandler.suspend(time, unit, handler);
        }

        @Override
        public void setSuspendTimeout(long time, TimeUnit unit) throws IllegalStateException {
            requestTimeoutHandler.setSuspendTimeout(time, unit);
        }

        @Override
        public boolean enableResponseBuffering() {
            return true;
        }

        /**
         * Commits the response and logs a warning message.
         *
         * This method should be called by the container at the end of the
         * handle method to make sure that the ResponseWriter was committed.
         */
        private void closeAndLogWarning() {
            if (closed.compareAndSet(false, true)) {
                response.end();
                //todo LOGGER.log(Level.WARNING, LocalizationMessages.ERROR_RESPONSEWRITER_RESPONSE_UNCOMMITED());
            }
        }
    }


    private static class DefaultSecurityContext implements SecurityContext {

        public static final Principal PRINCIPAL = new Principal() {
            @Override
            public String getName() {
                return "<not authorized>";
            }

            @Override
            public String toString() {
                return "Principal [" + getName() + "";
            }
        };

        @Override
        public Principal getUserPrincipal() {
            return PRINCIPAL;
        }

        @Override
        public boolean isUserInRole(String role) {
            return false;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public String getAuthenticationScheme() {
            return "NO_AUTH";
        }
    }
}