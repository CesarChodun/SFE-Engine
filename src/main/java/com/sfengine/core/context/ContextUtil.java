package com.sfengine.core.context;

public class ContextUtil {

    public static DeviceContext getDevice(ContextDictionary dict) {
        return ContextFactoryProvider
                    .getFactory(DeviceContextFactory.CONTEXT_IDENTIFIER, DeviceContextFactory.class)
                        .getContext(dict.get(DeviceContextFactory.CONTEXT_IDENTIFIER));
    }

    public static QueueContext getQueue(ContextDictionary dict) {
        return ContextFactoryProvider
                .getFactory(QueueContextFactory.CONTEXT_IDENTIFIER, QueueContextFactory.class)
                    .getContext(dict.get(QueueContextFactory.CONTEXT_IDENTIFIER));
    }

    public static QueueFamilyContext getQueueFamily(ContextDictionary dict) {
        return ContextFactoryProvider
                .getFactory(QueueFamilyContextFactory.CONTEXT_IDENTIFIER, QueueFamilyContextFactory.class)
                    .getContext(dict.get(QueueFamilyContextFactory.CONTEXT_IDENTIFIER));
    }

}
