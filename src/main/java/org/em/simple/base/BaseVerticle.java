package org.em.simple.base;

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

    public BaseVerticle(String address){
        this.address = address;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.eventBus().consumer(this.address,this.msgHandler());
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }

    protected Handler<Message<JsonObject>> msgHandler() {
        return msg -> {
            if (this.getClass().isAnnotationPresent(Ctrl.class)) {
                try {
                    Method method = this.getClass().getDeclaredMethod(msg.headers().get("method"), JsonObject.class);
                    if (method.isAnnotationPresent(Service.class)) {
                        msg.reply(method.invoke(this, msg.body()));
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
