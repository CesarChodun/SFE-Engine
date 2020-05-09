package com.sfengine.core.context;

public class ContextFactoryProvider {

    public static Context getContext(String name) {
        return ContextFactory.getContext(name);
    }

    public static DeviceContext getDeviceContext(String name) {
        return DeviceContextFactory.getContext(name);
    }

    public static QueueContext getQueueContext(String name) {
        return QueueContextFactory.getContext(name);
    }

}
