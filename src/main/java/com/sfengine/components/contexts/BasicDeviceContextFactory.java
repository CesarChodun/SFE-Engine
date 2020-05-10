package com.sfengine.components.contexts;

import com.sfengine.core.context.*;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.resources.Asset;
import com.sfengine.core.resources.ConfigFile;
import org.lwjgl.vulkan.EXTDescriptorIndexing;
import org.lwjgl.vulkan.VK10;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasicDeviceContextFactory {


    public static BasicDeviceContext createDeviceContext(String name, ContextDictionary dict) {
        final DeviceContextFactory factory =
                ContextFactoryProvider.getFactory(DeviceContextFactory.CONTEXT_IDENTIFIER, DeviceContextFactory.class);
        BasicDeviceContext devices = new BasicDeviceContext(name, DeviceContextFactory.CONTEXT_IDENTIFIER, dict);

        factory.putContext(devices);
        return devices;
    }

}
