package rendering.memory;

import static core.result.VulkanResult.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import core.rendering.RenderUtil;
import core.result.VulkanException;

/**
 * Memory utilities for rendering tasks.
 * 
 * @author Cezary
 * @since 21.12.2019
 */
public class RenderingMemoryUtil {

    /**
     * 
     * <p>
     *         Binds data to gpu buffer.
     * </p>
     * @param device    - Logical device
     * @param buff        - Destination gpu buffer
     * @param data        - Source buffer
     * @throws VulkanException 
     */
    public static void bindBufferMemoryGPU(VkDevice device, GPUBuffer buff, ByteBuffer data) throws VulkanException {
        PointerBuffer ppData = memAllocPointer(1);
        int err = vkMapMemory(device, buff.memory, 0, buff.allocationSize, 0, ppData);
        validate(err, "Failed to map memory.");
        
        long pData = ppData.get(0);
        memFree(ppData);
        
        memCopy(memAddress(data), pData, data.remaining());
        
        vkUnmapMemory(device, buff.memory);
        
        err = vkBindBufferMemory(device, buff.buffer, buff.memory, 0);
        validate(err, "Failed to bind buffer memory.");
    }
    
    /**
     *     Copies data to gpu buffer.
     * 
     * @param device    - Logical device
     * @param buff        - Destination gpu buffer
     * @param data        - Source buffer
     * @throws VulkanException 
     */
    public static void copyToBufferMemoryGPU(VkDevice device, GPUBuffer buff, ByteBuffer data) throws VulkanException {
        PointerBuffer ppData = memAllocPointer(1);
        int err = vkMapMemory(device, buff.memory, 0, buff.allocationSize, 0, ppData);
        validate(err, "Failed to map memory.");
        
        long pData = ppData.get(0);
        memFree(ppData);
        
        memCopy(memAddress(data), pData, data.remaining());
        
        vkUnmapMemory(device, buff.memory);
    }
    
    /**
     *     Allocates buffer memory for future use.
     * 
     * @param device                            - Logical device
     * @param physicalDevice                    - The physical device
     * @param buferSize                            - Buffer size
     * @param bufferUsage                        - Buffer usage 
     * @param sharingMode                        - Sharing mode
     * @return                                    - returns GPUBuffer allocated from given logical device.
     * @throws VulkanException 
     */
    public static GPUBuffer allocateBufferMemoryGPU(VkDevice device, VkPhysicalDevice physicalDevice, int buferSize, int bufferUsage, int sharingMode) throws VulkanException {
        GPUBuffer ans = new GPUBuffer();
        
        VkPhysicalDeviceMemoryProperties memoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
        vkGetPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties);
        
        VkBufferCreateInfo bufferCreateInfo = VkBufferCreateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .pNext(NULL)
                .flags(0)
                .usage(bufferUsage)
                .size(buferSize)
                .pQueueFamilyIndices(null)
                .sharingMode(sharingMode);
        
        LongBuffer pBuffer = memAllocLong(1);
        int err = vkCreateBuffer(device, bufferCreateInfo, null, pBuffer);
        validate(err, "Failed to create buffer.");
        ans.buffer = pBuffer.get(0);
        memFree(pBuffer);
        
        VkMemoryRequirements memoryRequirements = VkMemoryRequirements.calloc();
        vkGetBufferMemoryRequirements(device, ans.buffer, memoryRequirements);
        ans.allocationSize = memoryRequirements.size();

        VkMemoryAllocateInfo memAlloc = VkMemoryAllocateInfo.calloc()
                .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                .pNext(NULL)
                .allocationSize(ans.allocationSize);
        
        IntBuffer typeIndex = memAllocInt(1);
        RenderUtil.getMemoryType(physicalDevice, memoryRequirements.memoryTypeBits(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, typeIndex);
        memAlloc.memoryTypeIndex(typeIndex.get(0));
        memFree(typeIndex);
        
        LongBuffer pMemory = memAllocLong(1);
        err = vkAllocateMemory(device, memAlloc, null, pMemory);
        validate(err, "Failed to allocate memory.");
        ans.memory = pMemory.get(0);
        memFree(pMemory);
        
        bufferCreateInfo.free();
        memoryProperties.free();
        memoryRequirements.free();
        memAlloc.free();
        
        return ans;
    }
    
}
