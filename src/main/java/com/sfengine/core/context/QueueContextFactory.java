package com.sfengine.core.context;

import java.util.HashMap;
import java.util.Map;

public class QueueContextFactory {

    private static final Map<String, QueueContext> contexts = new HashMap<>();

    public static synchronized QueueContext getContext(String name) {
        return contexts.get(name);
    }

    public static synchronized QueueContext putContext(String name, QueueContext context) {
        return contexts.put(name, context);
    }
}
