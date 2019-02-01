package org.em.simple.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Log4j2LogDelegateFactory;
import io.vertx.core.spi.logging.LogDelegate;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.em.simple.annotation.Deploy;
import org.em.simple.utils.RouterUtil;

import java.util.HashSet;
import java.util.Set;

import static io.vertx.core.http.HttpHeaders.*;

/**
 * @author emperor
 */
@Deploy
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
        Router mainRouter = Router.router(vertx);
        mainRouter.route().handler(ctx -> {
            ctx.request().headers().add(CONTENT_TYPE, "charset=utf-8");
            ctx.response().headers().add(CONTENT_TYPE, "application/json; charset=utf-8");
            ctx.response().headers().add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            ctx.response().headers().add(ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS, PUT, DELETE, HEAD");
            ctx.response().headers().add(ACCESS_CONTROL_ALLOW_HEADERS, "*");
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
        mainRouter.route().handler(CorsHandler.create("*").allowedMethods(method));
        mainRouter.route().handler(BodyHandler.create().setDeleteUploadedFilesOnEnd(true));
        mainRouter.route().handler(CookieHandler.create());

        SessionStore sessionStore = LocalSessionStore.create(vertx, "em-sessions");
        SessionHandler sessionHandler = SessionHandler.create(sessionStore);
        sessionHandler.setSessionTimeout(30 * 60 * 1000L);
        sessionHandler.setNagHttps(false);
        mainRouter.route().handler(sessionHandler);

        // register mainRouter
        registerInterceptor(mainRouter);
//        registerRouter(mainRouter);
        RouterUtil.commonHandler(mainRouter);
        return mainRouter;

    }

    private void registerInterceptor(Router mainRouter) {
        mainRouter.route().handler(ctx -> {
            LOGGER.info("\n====ip: {} request: {} {}", ctx.request().remoteAddress(), ctx.request().method().name(), ctx.request().uri());
            LOGGER.info("session: {}", ctx.session().data());
            ctx.session().put("em", "emperor");
            ctx.next();
        });
        mainRouter.route("/em/*").handler(ctx -> {
            vertx.eventBus().send(AuthVerticle.class.getName(), new JsonObject().
                    put("token", ctx.request().getHeader("token")), ar -> {
                if (ar.succeeded()) {
                    ctx.next();
                } else {
                    LOGGER.error(ar.cause());
                    if (ar.cause() instanceof ReplyException) {
                        ReplyException re = (ReplyException) ar.cause();
                        ctx.response().setStatusCode(re.failureCode()).
                                end(re.getMessage());
                    } else {
                        ctx.response().end("server error");
                    }
                }
            });
        });
    }

    private void registerRouter(Router mainRouter) {
        Router subRouter = Router.router(vertx);
        subRouter.route("/hello").handler(ctx -> {
            ctx.response().putHeader("content-type", "text/plain").end("Hello EM");
        });
        mainRouter.mountSubRouter("/em/sub", subRouter);
        RouterUtil.registerRouter(vertx, mainRouter);

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
