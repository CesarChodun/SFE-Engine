package com.sfengine.core.context;

import java.util.HashMap;
import java.util.Map;

public class ContextFactory {

    private static final Map<String, Context> contexts = new HashMap<>();

    public static synchronized Context getContext(String name) {
        return contexts.get(name);
    }

    public static synchronized Context putContext(String name, Context context) {
        return contexts.put(name, context);
    }
}
