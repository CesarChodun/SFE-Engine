package com.sfengine.components.contexts.swapchain;

import com.sfengine.components.contexts.physicaldevice.BasicPhysicalDeviceContext;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextFactoryProvider;
import com.sfengine.core.context.physicaldevice.PhysicalDeviceContextFactory;
import com.sfengine.core.context.swapchain.SwapchainContextFactory;
import com.sfengine.core.properties.PropertyDictionary;
import com.sfengine.core.rendering.ColorFormatAndSpace;
import com.sfengine.core.rendering.RenderUtil;
import com.sfengine.core.result.VulkanException;
import org.lwjgl.vulkan.VK10;

import java.util.Arrays;

import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8A8_UNORM;

public class BasicSwapchainContextFactory {

    public static BasicSwapchainContext createSwapchainContext(String name, ContextDictionary dict, CFrame frame, ColorFormatAndSpace cfs) {
        SwapchainContextFactory factory =
                ContextFactoryProvider.getFactory(
                        SwapchainContextFactory.CONTEXT_IDENTIFIER,
                        SwapchainContextFactory.class);

        PropertyDictionary pdict = new PropertyDictionary();

        pdict.put("oldSwapchain", VK10.VK_NULL_HANDLE);
        pdict.put("colorFormat", cfs.colorFormat);
        pdict.put("colorSpace", cfs.colorSpace);

        BasicSwapchainContext context =
                new BasicSwapchainContext(name, dict, pdict, frame);
        factory.putContext(context);
        return context;
    }
}
