package com.sfengine.core.context;

import com.sfengine.core.synchronization.Dependency;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ContextDictionary {

    private Map<String, String> entries = new HashMap<>();
    private ContextDictionary parent;

    public ContextDictionary() {}

    public ContextDictionary(ContextDictionary dict) {
        parent = dict;
    }

    public boolean contains(String factoryIdentifier) {
        synchronized (entries) {
            if (entries.containsKey(factoryIdentifier))
                return true;
        }

        if (parent != null)
            return parent.contains(factoryIdentifier);

        return false;
    }

    public String get(String factoryIdentifier) {
        String out;

        synchronized(entries) {
            out = entries.get(factoryIdentifier);
        }

        if (out == null && parent != null)
            return parent.get(factoryIdentifier);

        return out;
    }

    public <T extends Context> String put(T context) {
        synchronized (entries) {
            return entries.put(context.getFactoryIdentifier(), context.getName());
        }
    }

}
