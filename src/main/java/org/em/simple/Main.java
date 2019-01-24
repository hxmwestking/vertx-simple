package org.em.simple;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.logging.Log4j2LogDelegateFactory;
import io.vertx.core.spi.logging.LogDelegate;
import org.em.simple.verticle.ServerVerticle;

/**
 * @author emperor
 */
public class Main {

    private static final LogDelegate LOGGER = new Log4j2LogDelegateFactory().createDelegate(Main.class.getName());

    public static void main(String[] args) {
        LOGGER.info("=====vertx-simple start=====");
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(ServerVerticle.class.getName());

    }
}
