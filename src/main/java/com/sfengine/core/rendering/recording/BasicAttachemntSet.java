package com.sfengine.core.rendering.recording;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.context.swapchain.SwapchainContext;
import com.sfengine.core.rendering.AttachmentBlueprint;
import com.sfengine.core.rendering.AttachmentSet;
import com.sfengine.core.rendering.RenderUtil;
import com.sfengine.core.result.VulkanException;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDevice;

import java.util.ArrayList;
import java.util.List;

public class BasicAttachemntSet implements AttachmentSet {

    private final List<long[]> images = new ArrayList<>();
    private final List<long[]> imageViews = new ArrayList<>();

    private ContextDictionary dict;

    public BasicAttachemntSet(ContextDictionary dict, long[] swapchainImages, AttachmentBlueprint... blueprints) {
        this.dict = dict;
        VkDevice device = ContextUtil.getDevice(dict).getDevice();
        VkPhysicalDevice physicalDevice = ContextUtil.getPhysicalDevice(dict).getPhysicalDevice();
        SwapchainContext swc = ContextUtil.getSwapchain(dict);

        for (int i = 0; i < blueprints.length; i++) {
            AttachmentBlueprint blueprint = blueprints[i];

            if (blueprint.primary()) {
                images.add(swapchainImages);
            }
            else {
                try {
                    VkImageCreateInfo info = blueprint.getImageInfo();
                    info.extent()
                            .width(swc.info().imageExtent().width())
                            .height(swc.info().imageExtent().height());
                    images.add(RenderUtil.createImages(physicalDevice, device, info, swapchainImages.length));
                } catch (VulkanException e) {
                    throw new AssertionError("Failed to create images.", e);
                }
            }

            try {

                if (blueprint.primary()) {
                    System.err.println(VK10.VK_FORMAT_B8G8R8A8_UNORM);
                    System.err.println(blueprint.getViewInfo().format());
                    blueprint.getViewInfo().format(VK10.VK_FORMAT_B8G8R8A8_UNORM);
                    if (blueprint.getViewInfo().format() != VK10.VK_FORMAT_B8G8R8A8_UNORM)
                        throw new AssertionError();
                }
                imageViews.add(RenderUtil.createImageViews(device, blueprint.getViewInfo(), images.get(i)));
            } catch (VulkanException e) {
                throw new AssertionError("Failed to create image views.", e);
            }
        }
    }


    @Override
    public long[] getViews(int frame) {
        int len = imageViews.size();
        long[] out = new long[len];

        for (int i = 0; i < len; i++)
            out[i] = imageViews.get(i)[frame];

        return out;
    }

    @Override
    public int framesCount() {
        return images.get(0).length;
    }

    @Override
    public void destroy() {
        VkDevice device = ContextUtil.getDevice(dict).getDevice();

        for (int i = 0; i < images.size(); i++) {
            RenderUtil.destroyImages(device, images.get(i));
            RenderUtil.destroyImageViews(device, imageViews.get(i));
        }
    }
}
