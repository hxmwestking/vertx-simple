package org.em.simple.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Log4j2LogDelegateFactory;
import io.vertx.core.spi.logging.LogDelegate;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.em.simple.Main;

import java.util.HashSet;
import java.util.Set;

import static io.vertx.core.http.HttpHeaders.*;

/**
 * @author emperor
 */
public class ServerVerticle extends AbstractVerticle {

    private static final LogDelegate LOGGER = new Log4j2LogDelegateFactory().createDelegate(ServerVerticle.class.getName());

    @Override
    public void start(Future<Void> startFuture) {

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(createRouter());
        server.websocketHandler();
        server.listen(8080, serverHandler(startFuture));
    }

    private Router createRouter() {
        Router router = Router.router(vertx);
        router.route().handler(ctx -> {
            ctx.request().headers().add(CONTENT_TYPE, "charset=utf-8");
            ctx.response().headers().add(CONTENT_TYPE, "application/json; charset=utf-8");
            ctx.response().headers().add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            ctx.response().headers().add(ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS, PUT, DELETE, HEAD");
            ctx.response().headers().add(ACCESS_CONTROL_ALLOW_HEADERS, "X-PINGOTHER, Origin,Content-Type, Accept, X-Requested-With, session_id,Version,token");
            ctx.response().headers().add(ACCESS_CONTROL_MAX_AGE, "1728000");
            ctx.next();
        });

        Set<HttpMethod> method = new HashSet<>();
        method.add(HttpMethod.GET);
        method.add(HttpMethod.POST);
        method.add(HttpMethod.OPTIONS);
        method.add(HttpMethod.PUT);
        method.add(HttpMethod.DELETE);
        method.add(HttpMethod.HEAD);
        router.route().handler(CorsHandler.create("*").allowedMethods(method));
        router.route().handler(BodyHandler.create().setDeleteUploadedFilesOnEnd(true));
        router.route().handler(CookieHandler.create());

        SessionStore sessionStore = LocalSessionStore.create(vertx, "em-sessions");
        SessionHandler sessionHandler = SessionHandler.create(sessionStore);
        sessionHandler.setSessionTimeout(30 * 60 * 1000L);
        sessionHandler.setNagHttps(false);
        router.route().handler(sessionHandler);

        // register router
        registerInterceptor(router);
        registerRouter(router);
        router.route("/*").handler(ctx -> {
            ctx.response().putHeader("content-type", "text/plain")
                    .end("Hello World!");
        });
        return router;

    }

    private void registerInterceptor(Router router) {
        router.route().handler(ctx -> {
            LOGGER.info("\nmethod: {} absoluteURI: {}", ctx.request().method().name(), ctx.request().absoluteURI());
            LOGGER.info("session: {}",ctx.session().data());
            ctx.session().put("em","emperor");
            ctx.next();
        });
        router.route("/em/*").handler(ctx -> {
            ctx.response().putHeader("em","emperor");
            ctx.next();
        });
    }

    private void registerRouter(Router router) {
    }


    private Handler<AsyncResult<HttpServer>> serverHandler(Future<Void> startFuture) {
        return res -> {
            if (res.succeeded()) {
                startFuture.complete();
            } else {
                startFuture.fail(res.cause());
            }
        };
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        stopFuture.complete();
    }
}
