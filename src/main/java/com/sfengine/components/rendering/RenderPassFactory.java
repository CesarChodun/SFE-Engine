package com.sfengine.components.rendering;

import com.sfengine.core.Application;
import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.rendering.AttachmentBlueprint;
import com.sfengine.core.rendering.FileAttachmentBlueprint;
import com.sfengine.core.resources.Asset;
import com.sfengine.core.resources.ConfigFile;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkSubpassDescription;

import java.io.IOException;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

public class RenderPassFactory {

    public static final String RENDER_PASS_LOCATION = "renderPasses";

    public static RenderPass createRenderPass(ContextDictionary dict, String name) {
        Asset asset = Application.getConfigAssets().getSubAsset(RENDER_PASS_LOCATION).getSubAsset(name);
        List<ConfigFile> cfgs = null;
        try {
            cfgs = asset.getAllConfigs();
        } catch (IOException e) {
            throw new AssertionError("Failed to create render pass (no attachments).", e);
        }

        AttachmentBlueprint[] atts = getBlueprints(dict, cfgs);

        return new RenderPass(dict, createSubpass(atts), null, atts);
    }

    private static AttachmentBlueprint[] getBlueprints(ContextDictionary dict, List<ConfigFile> cfgs) {
        AttachmentBlueprint[] blueprints = new AttachmentBlueprint[cfgs.size()];
        int nx = 0;

        for (ConfigFile cfg : cfgs)
            blueprints[nx++] = new FileAttachmentBlueprint(dict, cfg);

        return blueprints;
    }

    private static VkSubpassDescription.Buffer createSubpass(AttachmentBlueprint... blueprints) {
        int colorAttachmentCount = 0;

        for (AttachmentBlueprint b : blueprints) {
            if (b.getImageInfo().usage() == VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT)
                colorAttachmentCount++;
        }

        VkAttachmentReference.Buffer colorReference = VkAttachmentReference.calloc(colorAttachmentCount);

        VkAttachmentReference depthReference = VkAttachmentReference.calloc()
                .layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        int nx_color = 0;

        for (int i = 0; i < blueprints.length; i++) {
            if (blueprints[i].getImageInfo().usage() == VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT) {
                colorReference.get(nx_color++)
                        .attachment(i)
                        .layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            }
            else if (blueprints[i].getImageInfo().usage() == VK10.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT) {
                depthReference.attachment(i);
            }
        }

        return VkSubpassDescription.calloc(1)
                        .pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS)
                        .flags(0)
                        .pInputAttachments(null)
                        .colorAttachmentCount(colorReference.remaining())
                        .pColorAttachments(colorReference)
                        .pResolveAttachments(null)
                        .pDepthStencilAttachment(depthReference)
                        .pPreserveAttachments(null);
    }

}
