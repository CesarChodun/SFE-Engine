package com.sfengine.components.contexts.device;

import com.sfengine.core.context.*;
import com.sfengine.core.context.device.DeviceContextFactory;

public class BasicDeviceContextFactory {


    public static BasicDeviceContext createDeviceContext(String name, ContextDictionary dict) {
        final DeviceContextFactory factory =
                ContextFactoryProvider.getFactory(DeviceContextFactory.CONTEXT_IDENTIFIER, DeviceContextFactory.class);
        BasicDeviceContext devices = new BasicDeviceContext(name, DeviceContextFactory.CONTEXT_IDENTIFIER, dict);

        factory.putContext(devices);
        return devices;
    }

}
