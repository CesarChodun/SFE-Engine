package com.sfengine.core.rendering;

import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.synchronization.Dependable;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkImageCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;

public interface AttachmentBlueprint extends Dependable, Destroyable {

    VkAttachmentDescription getDescription();

    VkClearValue getClearValue();

    VkImageCreateInfo getImageInfo();

    VkImageViewCreateInfo getViewInfo();

    boolean primary();

}
