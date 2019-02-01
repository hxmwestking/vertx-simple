package org.em.simple.base;


/**
 * @description request method
 * @author ximan.huang
 * @date 2019/1/30 17:50
 */
public enum HttpMethod {
    DEFAULT("DEFAULT"),
    GET("GET"),
    PUT("PUT"),
    POST("POST"),
    OPTIONS("OPTIONS"),
    DELETE("DELETE"),
    HEAD("HEAD"),
    ;
    private String name;

    HttpMethod(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }}
