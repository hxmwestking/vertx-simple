package org.em.simple.utils;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.apache.commons.lang3.StringUtils;
import org.em.simple.annotation.Ctrl;
import org.em.simple.annotation.Deploy;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @description ReflectionsUtil
 * @author ximan.huang
 * @date 2019/1/30 17:35
 */
public class ReflectionsUtil {

    public static final String VERTICLE_PATH = "org.em.simple.verticle";

    private static Set<Class<?>> getVerticle(final Class<? extends Annotation> annotation) {
        Reflections reflections = new Reflections(VERTICLE_PATH);
        return reflections.getTypesAnnotatedWith(annotation);
    }

    public static void deploy(Vertx vertx) {
        Set<Class<?>> annotated = getVerticle(Deploy.class);
        annotated.forEach(clz -> {
            Deploy annotation = clz.getAnnotation(Deploy.class);
            if (StringUtils.isBlank(annotation.value())) {
                vertx.deployVerticle(clz.getName());
            } else {
                vertx.deployVerticle(annotation.value());
            }
        });
    }

    public static void registerRouter(Vertx vertx, Router mainRouter) {
        Set<Class<?>> annotated = getVerticle(Ctrl.class);
        annotated.forEach(clz -> {
            Ctrl annotation = clz.getAnnotation(Ctrl.class);
            String prefix;
            Router subRouter = Router.router(vertx);
            if (StringUtils.isBlank(annotation.value())) {
                prefix = addPrefix(clz.getSimpleName(),"/");
            } else {
                prefix = addPrefix(annotation.value(),"/");
            }
            // todo
            mainRouter.mountSubRouter(prefix,subRouter);
        });
    }

    private static String addPrefix(String path, String prefix) {
        if (path.startsWith(prefix)) {
            return path;
        }
        return prefix + path;
    }
}
