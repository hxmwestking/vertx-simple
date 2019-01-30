package org.em.simple;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Log4j2LogDelegateFactory;
import io.vertx.core.spi.logging.LogDelegate;
import org.apache.commons.lang3.StringUtils;
import org.em.simple.annotation.Deploy;
import org.em.simple.utils.ReflectionsUtil;
import org.reflections.Reflections;

import java.util.Set;

/**
 * @author emperor
 */
public class Main {

    private static final LogDelegate LOGGER = new Log4j2LogDelegateFactory().createDelegate(Main.class.getName());


    public static void main(String[] args) {
        LOGGER.info("=====vertx-simple start=====");
        Vertx vertx = Vertx.vertx();

        ReflectionsUtil.deploy(vertx);

    }
}
