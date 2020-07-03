package com.sfengine.core.context;

import com.sfengine.core.context.device.DeviceContext;
import com.sfengine.core.context.device.DeviceContextFactory;
import com.sfengine.core.context.framebufferfactory.FrameBufferFactoryContextFactory;
import com.sfengine.core.context.physicaldevice.PhysicalDeviceContextFactory;
import com.sfengine.core.context.queue.QueueContext;
import com.sfengine.core.context.queue.QueueContextFactory;
import com.sfengine.core.context.queuefamily.QueueFamilyContext;
import com.sfengine.core.context.queuefamily.QueueFamilyContextFactory;
import com.sfengine.core.context.renderjob.RenderJobContextFactory;
import com.sfengine.core.context.swapchain.SwapchainContextFactory;
import com.sfengine.core.resources.Asset;
import com.sfengine.core.resources.ConfigFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ContextFactoryProvider {

    private static final int MAX_CONTEXT_LOCKS = 1 * 1000 * 1000;
    private static final String SUBASSET_LOCATION = "contexts";

    private static final String CONFIG_EXTENSION = "cfg";

    private static final Map<String, ContextFactory> factories = new HashMap<>();

    static {
        setFactory(PhysicalDeviceContextFactory.class.getSimpleName(), new PhysicalDeviceContextFactory());
        setFactory(DeviceContextFactory.class.getSimpleName(), new DeviceContextFactory());
        setFactory(QueueContextFactory.class.getSimpleName(), new QueueContextFactory());
        setFactory(QueueFamilyContextFactory.class.getSimpleName(), new QueueFamilyContextFactory());
        setFactory(RenderJobContextFactory.class.getSimpleName(), new RenderJobContextFactory());
        setFactory(FrameBufferFactoryContextFactory.class.getSimpleName(), new FrameBufferFactoryContextFactory());
        setFactory(SwapchainContextFactory.class.getSimpleName(), new SwapchainContextFactory());
    }

    public static <T extends ContextFactory> T getFactory(String identifier, Class<T> cls) {
        synchronized (factories) {
            ContextFactory factory = factories.get(identifier);

            try {
                return cls.cast(factory);
            } catch(ClassCastException e) {
                throw new AssertionError("Wrong factory identifier.");
            }
        }
    }

    public static void setFactory(String name, ContextFactory contextFactory) {
        synchronized (factories) {
            factories.put(name, contextFactory);
        }
    }

    public static ConfigFile getConfig(Context context, Asset asset) throws IOException {
        return factories.get(context.getFactoryIdentifier())
                .getSubasset(asset.getSubAsset(SUBASSET_LOCATION))
                        .getConfigFile(context.getName() + '.' + CONFIG_EXTENSION);
    }

    public static Asset getSubasset(ContextFactory factory, Asset asset) {
        return factory.getSubasset(asset.getSubAsset(SUBASSET_LOCATION));
    }

}
