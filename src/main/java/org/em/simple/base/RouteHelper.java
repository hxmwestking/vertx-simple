package org.em.simple.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.em.simple.annotation.Service;

import java.lang.reflect.Method;

/**
 * @author em
 */
@Data
@AllArgsConstructor
public class RouteHelper {

    private Service service;
    private Method method;
}
