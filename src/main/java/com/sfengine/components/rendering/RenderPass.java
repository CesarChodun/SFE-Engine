package com.sfengine.components.rendering;

import static com.sfengine.core.result.VulkanResult.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.context.swapchain.SwapchainContext;
import com.sfengine.core.rendering.AttachmentBlueprint;
import com.sfengine.core.rendering.recording.Recordable;
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.result.VulkanException;
import java.nio.LongBuffer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.*;

/**
 * Structure storing information about the rendering work.
 *
 * @author Cezary Chodun
 * @since 19.10.2019
 */
public class RenderPass implements Recordable, Destroyable {

    /** Create info for this render pass. */
    private VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc();
    /** Begin info for this render pass. */
    private VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc();

    /** The render pass preset(a great place to set viewport) */
    private Recordable preset;
    /** Rest of the work that needs to be performed in the render pass. */
    private Recordable work;

    // command buffers
    private int contents = VK_SUBPASS_CONTENTS_INLINE;

    /** The render pass handle. */
    private long renderPass;

    private AttachmentBlueprint[] attachmentBlueprints;

    private ContextDictionary dict;

    /**
     * Creates a render pass.
     *
     * @param attachmentBlueprints Must be freed after the render pass was destroyed.
     * @param subpasses A buffer with subpasses(may be freed afterwards)
     * @param dependencies A buffer with subpass dependencies(may be freed afterwards)
     * @throws VulkanException When failed to create the render pass.
     */
    public RenderPass(
            ContextDictionary dict,
            VkSubpassDescription.Buffer subpasses,
            @Nullable VkSubpassDependency.Buffer dependencies,
            AttachmentBlueprint... attachmentBlueprints) {
        this.dict = dict;
        this.attachmentBlueprints = attachmentBlueprints;
        VkAttachmentDescription.Buffer buf = VkAttachmentDescription.calloc(attachmentBlueprints.length);
        for (int i = 0; i < attachmentBlueprints.length; i++)
            buf.put(i, attachmentBlueprints[i].getDescription());

        VkDevice logicalDevice = ContextUtil.getDevice(dict).getDevice();

        renderPassInfo
                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                .pNext(NULL)
                .pAttachments(buf)
                .pSubpasses(subpasses)
                .pDependencies(dependencies);

        LongBuffer pRenderPass = memAllocLong(1);
        int err = vkCreateRenderPass(logicalDevice, renderPassInfo, null, pRenderPass);
        assertValidate(err, "Failed to create render pass!");

        renderPass = pRenderPass.get(0);
        memFree(pRenderPass);

        VkClearValue.Buffer clbuf = VkClearValue.calloc(attachmentBlueprints.length);
        for (int i = 0; i < attachmentBlueprints.length; i++)
            clbuf.put(i, attachmentBlueprints[i].getClearValue());

        renderPassBeginInfo
                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                .pNext(NULL)
                .renderPass(renderPass)
                .pClearValues(clbuf);
    }

    /** @return the render pass handle. */
    public long handle() {
        return renderPass;
    }

    /** Frees the render pass data. <b>Note:</b> The attachments are not freed. */
    @Override
    public void destroy() {
        memFree(renderPassInfo.pSubpasses().pColorAttachments());
        renderPassInfo.pSubpasses().free();
        renderPassInfo.free();
        renderPassBeginInfo.free();
    }

    public Recordable getWork() {
        return work;
    }

    public void setWork(Recordable work) {
        this.work = work;
    }

    public Recordable getPreset() {
        return preset;
    }

    public void setPreset(Recordable preset) {
        this.preset = preset;
    }

    public AttachmentBlueprint[] getAttachmentBlueprints() {
        return attachmentBlueprints;
    }

    @Override
    public void record(VkCommandBuffer buffer, long frameBuffer) {

        SwapchainContext swc = ContextUtil.getSwapchain(dict);

        int width = swc.info().imageExtent().width();
        int height = swc.info().imageExtent().height();

        renderPassBeginInfo.renderArea().offset().set(0, 0);
        renderPassBeginInfo.renderArea().extent().set(width, height);
        renderPassBeginInfo.framebuffer(frameBuffer);

        vkCmdBeginRenderPass(buffer, renderPassBeginInfo, contents);

        if (preset != null) {
            preset.record(buffer, frameBuffer);
        }
        if (work != null) {
            work.record(buffer, frameBuffer);
        }

        vkCmdEndRenderPass(buffer);
    }
}
