package org.em.simple.utils;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

import static org.em.simple.base.Constant.PREFIX;

/**
 * @author em
 */
public class ContextUtil {

    public static String getAddress(String uri) {
        if (uri.startsWith(PREFIX)) {
            return uri.substring(1, uri.lastIndexOf(PREFIX) > 0 ? uri.indexOf(PREFIX, 1) : uri.length());
        }
        return uri.substring(0, uri.indexOf(PREFIX));
    }

    public static JsonObject param(RoutingContext ctx) {
        JsonObject obj = new JsonObject();
        if (StringUtils.isNotBlank(ctx.getBodyAsString())) {
            obj.mergeIn(ctx.getBodyAsJson());
        }
        ctx.queryParams().forEach(entry -> obj.put(entry.getKey(), entry.getValue()));
        return obj;
    }

    public static String getMethodName(String uri) {
        return uri.substring(uri.lastIndexOf("/") + 1);
    }
}