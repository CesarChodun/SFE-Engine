package rendering.recording;

import static core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import core.rendering.Recordable;
import core.resources.Destroyable;
import core.result.VulkanException;
import java.nio.LongBuffer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import rendering.config.Attachments;

/**
 * Structure storing information about the rendering work.
 *
 * @author Cezary Chodun
 * @since 19.10.2019
 */
public class RenderPass implements Destroyable {

    /** Create info for this render pass. */
    private VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc();
    /** Begin info for this render pass. */
    private VkRenderPassBeginInfo renderPassBeginInfo = VkRenderPassBeginInfo.calloc();
    /** Render pass attachments. */
    private Attachments attachments;

    /** The render pass preset(a great place to set viewport) */
    private Recordable preset;
    /** Rest of the work that needs to be performed in the render pass. */
    private Recordable work;
 
    // TODO: Change the architecture to utilize secondary
    // command buffers
    private int contents =
            VK_SUBPASS_CONTENTS_INLINE; 

    /** The render pass handle. */
    private long renderPass;

    /**
     * Creates a render pass.
     *
     * @param logicalDevice Valid logical device.
     * @param attachments Must be freed after the render pass was destroyed.
     * @param subpasses A buffer with subpasses(may be freed afterwards)
     * @param dependencies A buffer with subpass dependencies(may be freed afterwards)
     * @throws VulkanException When failed to create the render pass.
     */
    public RenderPass(
            VkDevice logicalDevice,
            Attachments attachments,
            VkSubpassDescription.Buffer subpasses,
            @Nullable VkSubpassDependency.Buffer dependencies)
            throws VulkanException {
        VkAttachmentDescription.Buffer buf = attachments.getBuffer();

        renderPassInfo
                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                .pNext(NULL)
                .pAttachments(buf)
                .pSubpasses(subpasses)
                .pDependencies(dependencies);

        this.attachments = attachments;

        LongBuffer pRenderPass = memAllocLong(1);
        int err = vkCreateRenderPass(logicalDevice, renderPassInfo, null, pRenderPass);
        validate(err, "Failed to create render pass!");

        renderPass = pRenderPass.get(0);
        memFree(pRenderPass);

        renderPassBeginInfo
                .sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                .pNext(NULL)
                .renderPass(renderPass)
                .pClearValues(this.attachments.getClearValues());
    }

    /**
     * Records the data to the command buffer.
     *
     * @param cmd Target command buffer.
     * @param frameBuffer Frame buffer handle.
     * @param offsetX X offset for the render area.
     * @param offsetY Y offset for the render area.
     * @param width Width of the render area.
     * @param height Height of the render area.
     */
    public void record(
            VkCommandBuffer cmd,
            long frameBuffer,
            int offsetX,
            int offsetY,
            int width,
            int height) {
        renderPassBeginInfo.renderArea().offset().set(offsetX, offsetY);
        renderPassBeginInfo.renderArea().extent().set(width, height);
        renderPassBeginInfo.framebuffer(frameBuffer);

        vkCmdBeginRenderPass(cmd, renderPassBeginInfo, contents);

        if (preset != null) preset.record(cmd);
        if (work != null) work.record(cmd);

        vkCmdEndRenderPass(cmd);
    }

    /** @return the render pass handle. */
    public long handle() {
        return renderPass;
    }

    @Override
    /** Frees the render pass data. <b>Note:</b> The attachments are not freed. */
    public void destroy() {
        attachments.destroy();
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
}
