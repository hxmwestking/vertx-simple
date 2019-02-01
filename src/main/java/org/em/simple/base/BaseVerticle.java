package org.em.simple.base;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Log4j2LogDelegateFactory;
import io.vertx.core.spi.logging.LogDelegate;
import org.apache.commons.lang3.StringUtils;
import org.em.simple.annotation.Ctrl;
import org.em.simple.annotation.Service;
import org.em.simple.utils.RouterUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.em.simple.base.Constant.EMPTY;
import static org.em.simple.base.Constant.PREFIX;

public class BaseVerticle extends AbstractVerticle {

    private static final LogDelegate LOGGER = new Log4j2LogDelegateFactory().createDelegate(BaseVerticle.class.getName());

    protected String address;
    protected Map<String, RouteHelper> router = new HashMap<>();

    public BaseVerticle(String address) {
        this.address = address;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOGGER.info("========== Verticle start : {}", this.address);
        registerRouter();
        vertx.eventBus().consumer(this.address, this.msgHandler());
    }

    private void registerRouter() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Service.class)) {
                Service service = method.getAnnotation(Service.class);
                String name = null;
                if (StringUtils.isNotBlank(service.value())) {
                    name = service.value();
                } else {
                    name = method.getName();
                }
                router.put(RouterUtil.addPrefixAndSuffix(name, PREFIX, EMPTY), new RouteHelper(service, method));
            }
        }
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }

    protected Handler<Message<JsonObject>> msgHandler() {
        return msg -> {
            if (this.getClass().isAnnotationPresent(Ctrl.class)) {
                try {
                    @Nullable String reqMethodName = msg.headers().get("method");
                    @Nullable String rawMethod = msg.headers().get("rawMethod");
                    if (router.containsKey(reqMethodName)) {
                        RouteHelper routeHelper = router.get(reqMethodName);
                        HttpMethod httpMethod = routeHelper.getService().method();
                        if (!HttpMethod.DEFAULT.equals(httpMethod) && !httpMethod.getName().equals(rawMethod)) {
                            msg.fail(400, "incorrect error");
                        }
                        msg.reply(routeHelper.getMethod().invoke(this, msg.body()));
                    }
                    msg.fail(400, "incorrect error");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOGGER.error(e);
                    msg.fail(500, "server error or no such resource");
                }
            }
            msg.fail(404, "no such resource");
        };
    }
}
