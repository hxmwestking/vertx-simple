package org.em.simple.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Log4j2LogDelegateFactory;
import io.vertx.core.spi.logging.LogDelegate;
import org.em.simple.annotation.Deploy;

/**
 * @author emperor
 */
@Deploy
public class AuthVerticle extends AbstractVerticle {

    private static final LogDelegate LOGGER = new Log4j2LogDelegateFactory().createDelegate(AuthVerticle.class.getName());
    private static final String ADMIN = "admin";
    private static final String TOKEN = "token";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.eventBus().<JsonObject>consumer(AuthVerticle.class.getName(), msg -> {
            JsonObject body = msg.body();
            if (body.isEmpty() || !body.containsKey(TOKEN)) {
                msg.fail(400, "Illegal Argument");
            } else {
                if (ADMIN.equals(body.getString(TOKEN))) {
                    msg.replyAddress();
                } else {
                    msg.fail(203, "Permission denied");
                }
            }
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }
}
