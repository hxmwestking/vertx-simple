package org.em.simple.utils;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.apache.commons.lang3.StringUtils;
import org.em.simple.annotation.Ctrl;
import org.em.simple.annotation.Deploy;
import org.em.simple.annotation.Service;
import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author em
 */
public class ReflectionsUtil {

    public static final String VERTICLE_PATH = "org.em.simple.verticle";

    public static Set<Class<?>> getVerticle(final Class<? extends Annotation> annotation) {
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

}
