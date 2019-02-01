package org.em.simple.annotation;

import org.em.simple.base.HttpMethod;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
    String value() default "";
    HttpMethod method() default HttpMethod.DEFAULT;
}
