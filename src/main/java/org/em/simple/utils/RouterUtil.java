package org.em.simple.utils;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Log4j2LogDelegateFactory;
import io.vertx.core.spi.logging.LogDelegate;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.em.simple.annotation.Ctrl;
import org.em.simple.annotation.Service;

import java.lang.reflect.Method;
import java.util.Set;

import static org.em.simple.base.Constant.*;
import static org.em.simple.utils.ContextUtil.*;

/**
 * @author em
 */
public class RouterUtil {

    private static final LogDelegate LOGGER = new Log4j2LogDelegateFactory().createDelegate(RouterUtil.class.getName());

    public static void registerRouter(Vertx vertx, Router mainRouter) {
        Set<Class<?>> annotated = ReflectionsUtil.getVerticle(Ctrl.class);
        annotated.forEach(clz -> {
            Ctrl annotation = clz.getAnnotation(Ctrl.class);
            String prefix;
            Router subRouter = Router.router(vertx);
            if (StringUtils.isBlank(annotation.value())) {
                prefix = addPrefixAndSuffix(clz.getSimpleName(), PREFIX, EMPTY);
            } else {
                prefix = addPrefixAndSuffix(annotation.value(), PREFIX, EMPTY);
            }
            registerSubRouter(subRouter, clz);
            mainRouter.mountSubRouter(prefix, subRouter);
        });
    }

    private static void registerSubRouter(Router subRouter, Class<?> clz) {
        Method[] methods = clz.getDeclaredMethods();
        for (Method method : methods) {
            Service service = method.getAnnotation(Service.class);
            if (service == null) {
                continue;
            }
            String path = null;
            if (StringUtils.isNotBlank(service.value())) {
                path = addPrefixAndSuffix(service.value(), PREFIX, EMPTY);
            } else {
                path = addPrefixAndSuffix(method.getName(), PREFIX, EMPTY);
            }
            switch (service.method()) {
                case GET:
                    subRouter.get(path);
                    break;
                case POST:
                    subRouter.post(path);
                    break;
                case PUT:
                    subRouter.put(path);
                    break;
                case DELETE:
                    subRouter.delete(path);
                    break;
                case HEAD:
                    subRouter.head(path);
                    break;
                case OPTIONS:
                    subRouter.options(path);
                    break;
                default:
                    subRouter.route(path);
            }
        }
    }

    public static String addPrefixAndSuffix(String path, String prefix, String suffix) {
        if (path.startsWith(prefix)) {
            if (path.endsWith(suffix)) {
                return path;
            } else {
                return path + suffix;
            }
        }
        if (path.endsWith(suffix)) {
            return prefix + path;
        }
        return prefix + path + suffix;
    }

    public static void commonHandler(Router mainRouter) {
        mainRouter.route().handler(handler()).failureHandler(failureHandler());
    }

    private static Handler<RoutingContext> handler() {
        return ctx -> {
            String uri = ctx.request().uri();
            ctx.vertx().eventBus().<JsonObject>send(getAddress(uri), param(ctx),
                    new DeliveryOptions().addHeader("method", getMethodName(uri)).
                            addHeader("rawMethod", ctx.request().rawMethod()), ar -> {
                        if (ar.failed()) {
                            ctx.response().setStatusCode(500);
                            ctx.fail(ar.cause());
                            return;
                        }
                        ctx.response().setStatusCode(200).end(ar.result().body().encode());
                    });
        };
    }

    private static Handler<RoutingContext> failureHandler() {
        return ctx -> {
            JsonObject fail = new JsonObject();
            if (ctx.failed()) {
                fail.put(CODE, 404);
                fail.put(MSG, "resource not found!");
                if (ctx.failure() instanceof ReplyException) {
                    if (ReplyFailure.TIMEOUT.equals(((ReplyException) ctx.failure()).failureType())) {
                        fail.put(CODE, 504);
                        fail.put(MSG, "TIMEOUT");
                        LOGGER.error("TIMEOUT");
                    } else if (ReplyFailure.NO_HANDLERS.equals(((ReplyException) ctx.failure()).failureType())) {
                        fail.put(CODE, 503);
                        fail.put(MSG, "NO_HANDLERS");
                        LOGGER.error("NO_HANDLERS");
                    } else if (ReplyFailure.RECIPIENT_FAILURE.equals(((ReplyException) ctx.failure()).failureType())) {
                        fail.put(CODE, 503);
                        fail.put(MSG, "RECIPIENT_FAILURE");
                        LOGGER.error("RECIPIENT_FAILURE");
                    }
                }
            } else {
                fail.put(CODE, 500);
                fail.put(MSG, "server error");
            }
            ctx.response().setStatusCode(200).end(fail.encode());
        };
    }
}
