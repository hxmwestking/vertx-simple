package org.em.simple.verticle;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Log4j2LogDelegateFactory;
import io.vertx.core.spi.logging.LogDelegate;
import org.em.simple.annotation.Ctrl;
import org.em.simple.annotation.Deploy;
import org.em.simple.annotation.Service;
import org.em.simple.base.BaseVerticle;


/**
 * @author ximan.huang
 * @description user
 * @date 2019/1/30 15:11
 */
@Deploy
@Ctrl
public class UserVerticle extends BaseVerticle {

    private static final LogDelegate LOGGER = new Log4j2LogDelegateFactory().createDelegate(UserVerticle.class.getName());

    public UserVerticle() {
        super(UserVerticle.class.getSimpleName());
    }

    @Service
    public JsonObject login(JsonObject body) {
        LOGGER.info("===== login ===== {}", body.encodePrettily());
        return new JsonObject().put("msg", "ok").put("status", "200");
    }


}
