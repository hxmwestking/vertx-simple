package org.em.simple.annotation;

import org.em.simple.base.Method;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
    String value() default "";
    Method method() default Method.DEFAULT;
}
