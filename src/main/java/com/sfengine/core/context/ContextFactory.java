package com.sfengine.core.context;

import com.sfengine.core.resources.Asset;

import java.util.HashMap;
import java.util.Map;

public abstract class ContextFactory<T extends Context> {

    protected final Map<String, T> contexts = new HashMap<>();

    public T getContext(String name) {
        synchronized (contexts) {
            return contexts.get(name);
        }
    }

    public boolean containsContext(String name) {
        synchronized (contexts) {
            return contexts.containsKey(name);
        }
    }

    public synchronized T putContext(T context) {
        synchronized (contexts) {
            return contexts.put(context.getName(), context);
        }
    }

    public abstract String getContextIdentifier();

    public abstract Asset getSubasset(Asset asset);
}
