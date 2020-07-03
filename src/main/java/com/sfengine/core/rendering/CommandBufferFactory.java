package com.sfengine.core.rendering;

import static com.sfengine.core.rendering.RenderUtil.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.rendering.recording.Recordable;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.result.VulkanResult;
import com.sfengine.components.rendering.RenderPass;

import java.util.HashMap;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;

/**
 * Class for command buffer creation.
 *
 * @author Cezary Chodun
 * @since 19.10.2019
 */
public class CommandBufferFactory {



    /** A map of command buffers pools. */
    private HashMap<VkCommandBuffer, Long> cmdPools = new HashMap<VkCommandBuffer, Long>();

    /** */
    private int queueFamilyIndex, flags;
    private Recordable rec;

    private ContextDictionary dict;

    /**
     * Creates a new Command Buffer factory.
     *
     * @param queueFamilyIndex Index of the render queue family.
     * @param flags Command buffer flags(for command pool creation).
     */
    public CommandBufferFactory(ContextDictionary dict, Recordable rec, int queueFamilyIndex, int flags) {
        this.queueFamilyIndex = queueFamilyIndex;
        this.flags = flags;
        this.dict = dict;
        this.rec = rec;
    }

    /**
     * Creates command buffers(one for each frame buffer).
     *
     * @param frameBuffers The frame buffers.
     * @return The command buffers.
     */
    public VkCommandBuffer[] createCmdBuffers(long... frameBuffers) {
        final VkDevice device = ContextUtil.getDevice(dict).getDevice();

        long commandPool;
        try {
            commandPool = createCommandPool(device, queueFamilyIndex, flags);
        } catch (VulkanException e) {
            e.printStackTrace();
            throw new AssertionError(e.getMessage());
        }

        VkCommandBuffer[] commandBuffers;

        VkCommandBufferAllocateInfo cbai =
                VkCommandBufferAllocateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                        .pNext(NULL)
                        .commandPool(commandPool)
                        .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                        .commandBufferCount(frameBuffers.length);

        VkCommandBufferBeginInfo cbbi =
                VkCommandBufferBeginInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                        .pNext(NULL);

        PointerBuffer pCommandBuffer = memAllocPointer(frameBuffers.length);
        int err = vkAllocateCommandBuffers(device, cbai, pCommandBuffer);
        try {
            VulkanResult.validate(err, "Failed to allocate command buffers!");
        } catch (VulkanException e) {
            e.printStackTrace();
            throw new AssertionError(e.getMessage());
        }
        cbai.free();

        commandBuffers = new VkCommandBuffer[frameBuffers.length];

        for (int i = 0; i < frameBuffers.length; i++) {

            commandBuffers[i] = new VkCommandBuffer(pCommandBuffer.get(i), device);

            err = vkBeginCommandBuffer(commandBuffers[i], cbbi);
            try {
                VulkanResult.validate(err, "Failed to begin command buffer!");
            } catch (VulkanException e) {
                e.printStackTrace();
                throw new AssertionError(e.getMessage());
            }

            rec.record(commandBuffers[i], frameBuffers[i]);

            err = vkEndCommandBuffer(commandBuffers[i]);
            try {
                VulkanResult.validate(err, "Failed to end command buffer!");
            } catch (VulkanException e) {
                e.printStackTrace();
                throw new AssertionError(e.getMessage());
            }
        }

        cmdPools.put(commandBuffers[0], commandPool);

        cbbi.free();
        pCommandBuffer.free();

        return commandBuffers;
    }

    /**
     * Destroys the command buffers allocated from this factory. It is required that the command
     * buffer list contains the same command buffer set. As returned in the createCmdBuffers().
     *
     * <p>Cannot be invoked if any of the command buffers is in the pending state!
     *
     * @param buffers The list of buffers returned by createCmdBuffers().
     */
    public void destroyCmdBuffers(VkCommandBuffer[] buffers) {
        final VkDevice device = ContextUtil.getDevice(dict).getDevice();

        for (VkCommandBuffer cmd : buffers) {
            if (cmdPools.containsKey(cmd)) {
                Long pool = cmdPools.get(cmd);
                vkDestroyCommandPool(device, pool, null);
            }
        }
    }
}
