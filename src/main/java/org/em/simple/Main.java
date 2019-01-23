package org.em.simple;

import io.vertx.core.Vertx;
import org.em.simple.verticle.ServerVerticle;

/**
 * @author emperor
 */
public class Main {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(ServerVerticle.class.getName());

    }
}
