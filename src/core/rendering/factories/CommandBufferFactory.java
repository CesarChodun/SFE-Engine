package core.rendering.factories;

import static core.result.VulkanResult.*;
import static core.rendering.RenderUtil.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.util.HashMap;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;

import core.result.VulkanException;
import rendering.recording.RenderPass;

/**
 * Class for command buffer creation.
 * 
 * @author Cezary Chodun
 * @since 10.19.2019
 */
public class CommandBufferFactory{
    
    
    
    /** The render pass for command buffers to use. */
    private RenderPass renderPass;
    /** The Vulkan logical device. */
    private VkDevice device;
    /***/
    private int queueFamilyIndex, flags;
    
    /** A map of command buffers pools. */
    private HashMap<VkCommandBuffer, Long> cmdPools = new HashMap<VkCommandBuffer, Long>();
    
    /**
     * Creates a new Command Buffer factory.
     * 
     * @param device            Vulkan device.
     * @param renderPass        The current render pass.
     * @param queueFamilyIndex    Index of the render queue family.
     * @param flags                Command buffer flags(for command pool creation).
     */
    public CommandBufferFactory(VkDevice device, RenderPass renderPass, int queueFamilyIndex, int flags) {
        this.device = device;
        this.renderPass = renderPass;
        this.queueFamilyIndex = queueFamilyIndex;
        this.flags = flags;
    }

    /**
     * Creates command buffers(one for each frame buffer).
     * 
     * @param width            Image width.
     * @param height        Image height.
     * @param framebuffers    The frame buffers.
     * @return        The command buffers.
     */
    public VkCommandBuffer[] createCmdBuffers(int width, int height, long... framebuffers) {
        long commandPool;
        try {
            commandPool = createCommandPool(device, queueFamilyIndex, flags);
        } catch (VulkanException e) {
            e.printStackTrace();
            throw new AssertionError(e.getMessage());
        }
        
        VkCommandBuffer[] commandBuffers;
        
        VkCommandBufferAllocateInfo cbai = VkCommandBufferAllocateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .pNext(NULL)
                .commandPool(commandPool)
                .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandBufferCount(framebuffers.length);

        VkCommandBufferBeginInfo cbbi = VkCommandBufferBeginInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                .pNext(NULL);
        
        
        PointerBuffer pCommandBuffer = memAllocPointer(framebuffers.length);
        int err = vkAllocateCommandBuffers(device, cbai, pCommandBuffer);
        try {
            validate(err, "Failed to allocate command buffers!");
        } catch (VulkanException e) {
            e.printStackTrace();
            throw new AssertionError(e.getMessage());
        }
        cbai.free();
        
        commandBuffers = new VkCommandBuffer[framebuffers.length];
        
        for(int i = 0; i < framebuffers.length; i++) {
            
            commandBuffers[i] = new VkCommandBuffer(pCommandBuffer.get(i), device);
            
            
            err = vkBeginCommandBuffer(commandBuffers[i], cbbi);
            try {
                validate(err, "Failed to begin command buffer!");
            } catch (VulkanException e) {
                e.printStackTrace();
                throw new AssertionError(e.getMessage());
            }
            
            renderPass.record(commandBuffers[i], framebuffers[i], 0, 0, width, height);

            err = vkEndCommandBuffer(commandBuffers[i]);
            try {
                validate(err, "Failed to end command buffer!");
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
     * Destroys the command buffers allocated from this factory.
     * It is required that the command buffer list
     * contains the same command buffer set. 
     * As returned in the createCmdBuffers().
     * 
     * Cannot be invoked if any of the command buffers
     * is in the pending state!
     * 
     * @param buffers        The list of buffers returned by 
     * createCmdBuffers().
     */
    public void destroyCmdBuffers(VkCommandBuffer[] buffers) {
        
        for (VkCommandBuffer cmd : buffers) 
            if (cmdPools.containsKey(cmd)){
                Long pool = cmdPools.get(cmd);
                vkDestroyCommandPool(device, pool, null);
            }
        
    }
    
    
    
}
