package com.sfengine.components.contexts;

import com.sfengine.core.context.Context;
import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.synchronization.Dependency;

import java.util.ArrayList;
import java.util.List;

public class DefaultContexts {

    private static final ContextDictionary dict = new ContextDictionary();
    private static final List<Dependency> deps = new ArrayList<>();

    static {
        synchronized (dict) {
            putDep(BasicPhysicalDeviceContextFactory.createPhysicalDeviceContext("Default", dict));
            putDep(BasicQueueFamilyContextFactory.createQueueFamilyContext("Default", dict));
            putDep(BasicDeviceContextFactory.createDeviceContext("Default", dict));
            putDep(BasicQueueContextFactory.createQueueContext("Default", dict));
        }
    }

    private static void putDep(Context c) {
        dict.put(c);
        deps.add(c.getDependency());
    }

    public static ContextDictionary getDictionary() {
        synchronized (dict) {
            return dict;
        }
    }

    public static List<Dependency> getDependencies() {
        synchronized (deps) {
            return new ArrayList<>(deps);
        }
    }

}
