package ru.veryevilzed.tools.proto;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Descriptors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class Utils {


    public static <T extends AbstractMessage> Descriptors.Descriptor getDescriptor(Class<T> clazz) {
        try {
            Method method = clazz.getMethod("getDescriptor");
            return (Descriptors.Descriptor)method.invoke(null);
        }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            System.out.printf("Error:%s", ignored);
            return null;
        }
    }

    public static Map<String, Object> getKeyValue(Object key, Object value) {
        Map<String,Object> res = new HashMap<>();
        res.put("key", key);
        res.put("value", value);
        return res;
    }

}
