package org.em.simple.base;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Log4j2LogDelegateFactory;
import io.vertx.core.spi.logging.LogDelegate;
import org.em.simple.annotation.Ctrl;
import org.em.simple.annotation.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BaseVerticle extends AbstractVerticle {

    private static final LogDelegate LOGGER = new Log4j2LogDelegateFactory().createDelegate(BaseVerticle.class.getName());

    protected String address;

    public BaseVerticle(String address) {
        this.address = address;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOGGER.info("========== Verticle start : {}", this.address);
        vertx.eventBus().consumer(this.address, this.msgHandler());
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
                    Method method = this.getClass().getDeclaredMethod(reqMethodName, JsonObject.class);
                    if (method.isAnnotationPresent(Service.class)) {
                        Service service = method.getAnnotation(Service.class);
                        String annName = service.method().getName();
                        if (annName.equals(org.em.simple.base.Method.DEFAULT.getName()) || annName.equals(reqMethodName)) {
                            msg.reply(method.invoke(this, msg.body()));
                            return;
                        }
                        msg.fail(400, "incorrect error");
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    LOGGER.error(e);
                    msg.fail(500, "server error or no such resource");
                }
            }
            msg.fail(404, "no such resource");
        };
    }
}
