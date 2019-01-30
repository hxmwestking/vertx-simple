package org.em.simple.base;


/**
 * @description request method
 * @author ximan.huang
 * @date 2019/1/30 17:50
 */
public enum Method {
    GET("GET"),
    PUT("PUT"),
    POST("POST"),
    OPTIONS("OPTIONS"),
    DELETE("DELETE"),
    HEAD("HEAD"),
    ;
    private String name;

    Method(String name){
        this.name = name;
    }
}
