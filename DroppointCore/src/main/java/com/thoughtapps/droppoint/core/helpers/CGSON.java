package com.thoughtapps.droppoint.core.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.Primitives;

import java.lang.reflect.Type;

/**
 * Created by zaskanov on 05.04.2017.
 */
public class CGSON {
    private final static Gson GSON = new Gson();

    public static <T> T fromJson(String json, Class<T> classOfT) throws JsonSyntaxException {
        return GSON.fromJson(json, classOfT);
    }

    public static String toJson(Object src) {
       return GSON.toJson(src);
    }
}
