package com.sfengine.core.context;

import java.util.HashMap;
import java.util.Map;

public class DeviceContextFactory {

    private static final Map<String, DeviceContext> contexts = new HashMap<>();

    public static synchronized DeviceContext getContext(String name) {
        return contexts.get(name);
    }

    public static synchronized DeviceContext putContext(String name, DeviceContext context) {
        return contexts.put(name, context);
    }
}
