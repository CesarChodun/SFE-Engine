package com.sfengine.components.contexts.physicaldevice;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextFactoryProvider;
import com.sfengine.core.context.physicaldevice.PhysicalDeviceContextFactory;

public class BasicPhysicalDeviceContextFactory {

    /**
     * Creates a VkPhysicalDevice with a basic configuration for rendering and memory transfer.
     * Discreet GPUs are prioritized.
     *
     * @param name  name of the <code>PhysicalDeviceContext</> object.
     * @param dict  a valid <code>ContextDictionary</> with contexts required
     *              to create a <code>VkPhysicalDevice</>.
     *
     * @return  a valid context.
     */
    public static BasicPhysicalDeviceContext createPhysicalDeviceContext(String name, ContextDictionary dict) {
        PhysicalDeviceContextFactory factory =
                ContextFactoryProvider.getFactory(
                        PhysicalDeviceContextFactory.CONTEXT_IDENTIFIER,
                        PhysicalDeviceContextFactory.class);

        BasicPhysicalDeviceContext context =
                new BasicPhysicalDeviceContext(name, PhysicalDeviceContextFactory.CONTEXT_IDENTIFIER);
        factory.putContext(context);
        return context;
    }

}
