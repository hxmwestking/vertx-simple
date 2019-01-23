package org.em.simple.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.impl.SessionHandlerImpl;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

import java.util.HashSet;
import java.util.Set;

import static io.vertx.core.http.HttpHeaders.*;

public class ServerVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(createRouter());
        server.websocketHandler();
        server.listen(serverHandler(startFuture));
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

        Set<HttpMethod> method = new HashSet<HttpMethod>();
        method.add(HttpMethod.GET);
        method.add(HttpMethod.POST);
        method.add(HttpMethod.OPTIONS);
        method.add(HttpMethod.PUT);
        method.add(HttpMethod.DELETE);
        method.add(HttpMethod.HEAD);
        router.route().handler(CorsHandler.create("*").allowedMethods(method));
        router.route().handler(BodyHandler.create().setDeleteUploadedFilesOnEnd(true));
        router.route().handler(CookieHandler.create());

        SessionStore sessionStore = LocalSessionStore.create(vertx, "org.em.sessions");
        SessionHandler sessionHandler = SessionHandler.create(sessionStore);
        sessionHandler.setSessionTimeout(30 * 60 * 1000);
        sessionHandler.setNagHttps(false);
        router.route().handler(sessionHandler);

        // 注册地址(前面的可做为拦截器)
        registerInterceptor(router);
        registerRouter(router);

        return router;

    }

    private void registerInterceptor(Router router) {
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
    public void stop(Future<Void> stopFuture) throws Exception {
        stopFuture.complete();
    }
}
