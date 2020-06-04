package com.sfengine.core.context;

import com.sfengine.core.synchronization.Dependency;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ContextDictionary {

    private final Map<String, String> entries = Collections.synchronizedMap(new HashMap<>());

    public ContextDictionary() {}

    public ContextDictionary(ContextDictionary dict) {
        addAll(dict);
    }

    public boolean contains(String factoryIdentifier) {
        return entries.containsKey(factoryIdentifier);
    }

    public String get(String factoryIdentifier) {
        return entries.get(factoryIdentifier);
    }

    public <T extends Context> String put(T context) {
        return entries.put(context.getFactoryIdentifier(), context.getName());
    }

    public void addAll(ContextDictionary dict) {
        entries.putAll(dict.entries);
    }

}
