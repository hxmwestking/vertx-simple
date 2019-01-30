package org.em.simple.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Log4j2LogDelegateFactory;
import io.vertx.core.spi.logging.LogDelegate;
import org.em.simple.annotation.Ctrl;
import org.em.simple.annotation.Deploy;
import org.em.simple.annotation.Service;

/**
 * @description user
 * @author ximan.huang
 * @date 2019/1/30 15:11
 */
@Deploy
@Ctrl
public class UserVerticle extends AbstractVerticle {

    private static final LogDelegate LOGGER = new Log4j2LogDelegateFactory().createDelegate(UserVerticle.class.getName());

    @Override
    public void start(Future<Void> startFuture) throws Exception {

        vertx.eventBus().consumer(UserVerticle.class.getName(), msgHandler());
    }

    private Handler<Message<JsonObject>> msgHandler() {
        return msg->{
           msg.reply(login(msg.body()));
        };
    }

    @Service
    private JsonObject login(JsonObject body){
        LOGGER.info(body.encodePrettily());
        return new JsonObject().put("msg","ok").put("status","200");
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }
}
