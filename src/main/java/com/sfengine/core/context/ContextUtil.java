package com.sfengine.core.context;

import com.sfengine.core.context.device.DeviceContext;
import com.sfengine.core.context.device.DeviceContextFactory;
import com.sfengine.core.context.physicaldevice.PhysicalDeviceContext;
import com.sfengine.core.context.physicaldevice.PhysicalDeviceContextFactory;
import com.sfengine.core.context.queue.QueueContext;
import com.sfengine.core.context.queue.QueueContextFactory;
import com.sfengine.core.context.queuefamily.QueueFamilyContext;
import com.sfengine.core.context.queuefamily.QueueFamilyContextFactory;

public class ContextUtil {

    public static PhysicalDeviceContext getPhysicalDevice(ContextDictionary dict) {
        return ContextFactoryProvider
                .getFactory(PhysicalDeviceContextFactory.CONTEXT_IDENTIFIER, PhysicalDeviceContextFactory.class)
                .getContext(dict.get(PhysicalDeviceContextFactory.CONTEXT_IDENTIFIER));
    }

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
