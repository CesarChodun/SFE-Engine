package rendering.engine.geometry;

import static core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import core.result.VulkanException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

/**
 * Util class that helps with the memory management within the geometry package.
 *
 * @author Cezary Chodun
 * @since 28.10.2019
 */
public class Util {

    /**
     * Returns memory type that meets requirements.
     *
     * @param memoryProperties Memory properties.
     * @param bits Interesting indices.
     * @param properties Properties that memory type should meet.
     * @param typeIndex Integer buffer for returned value.
     * @return Information about successfulness of the operation(true - success, false - fail).
     */
    public static boolean getMemoryType(
            VkPhysicalDeviceMemoryProperties memoryProperties,
            int bits,
            int properties,
            IntBuffer typeIndex) {
        for (int i = 0; i < 32; i++) {
            if ((bits & (1 << i)) > 0) {
                if ((memoryProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                    typeIndex.put(0, i);
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Creates a Vulkan buffer.
     *
     * @param device Device that will own the buffer memory.
     * @param size Size of the buffer.
     * @param usage Buffer usage.
     * @param flags Buffer flags.
     * @return Handle to the buffer.
     * @throws VulkanException When failed to create a buffer.
     */
    public static long createBuffer(VkDevice device, long size, int usage, int flags)
            throws VulkanException {
        VkBufferCreateInfo bufInfo =
                VkBufferCreateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                        .pNext(NULL)
                        .usage(usage)
                        .size(size)
                        .flags(flags);

        LongBuffer pBuffer = memAllocLong(1);
        int err = vkCreateBuffer(device, bufInfo, null, pBuffer);
        validate(err, "Failed to create buffer!");

        long handle = pBuffer.get(0);

        memFree(pBuffer);
        bufInfo.free();

        return handle;
    }

    /**
     * Obtains the memory allocate info.
     *
     * @param physicalDevice
     * @param device
     * @param bufferHandle Handle to the memory buffer.
     * @return Allocate info.
     */
    public static VkMemoryAllocateInfo getMemoryAllocateInfo(
            VkPhysicalDevice physicalDevice, VkDevice device, long bufferHandle) {
        VkPhysicalDeviceMemoryProperties pMemoryProperties =
                VkPhysicalDeviceMemoryProperties.calloc();
        vkGetPhysicalDeviceMemoryProperties(physicalDevice, pMemoryProperties);

        final VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
        vkGetBufferMemoryRequirements(device, bufferHandle, memReqs);

        IntBuffer memoryTypeIndex = memAllocInt(1);
        if (!Util.getMemoryType(
                pMemoryProperties,
                memReqs.memoryTypeBits(),
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT,
                memoryTypeIndex)) {
            throw new AssertionError("Failed to obtain memry type.");
        }

        final VkMemoryAllocateInfo memAlloc =
                VkMemoryAllocateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                        .pNext(NULL)
                        .allocationSize(memReqs.size())
                        .memoryTypeIndex(memoryTypeIndex.get(0));

        pMemoryProperties.free();
        memReqs.free();
        memFree(memoryTypeIndex);

        return memAlloc;
    }

    /**
     * Allocates memory.
     *
     * @param device The logical device that owns the memory.
     * @param memAlloc A pointer to an instance of the VkMemoryAllocateInfo structure describing
     *     parameters of the allocation.
     * @return VkDeviceMemory handle in which information about the allocated memory is returned.
     * @throws VulkanException When failed to allocate memory.
     */
    public static long allocateMemory(VkDevice device, VkMemoryAllocateInfo memAlloc)
            throws VulkanException {
        LongBuffer pMemory = memAllocLong(1);
        int err = vkAllocateMemory(device, memAlloc, null, pMemory);
        long memory = pMemory.get(0);
        validate(err, "Failed to allocate memory!");

        memFree(pMemory);

        return memory;
    }

    /**
     * Map a memory object into application address space.
     *
     * @param device The logical device that owns the memory.
     * @param memory Memory the VkDeviceMemory object to be mapped.
     * @param memAlloc A pointer to an instance of the VkMemoryAllocateInfo structure describing
     *     parameters of the allocation.
     * @return A host-accessible pointer to the beginning of the mapped range.
     * @throws VulkanException When failed to map memory.
     */
    public static long mapMemory(VkDevice device, long memory, VkMemoryAllocateInfo memAlloc)
            throws VulkanException {
        PointerBuffer pData = memAllocPointer(1);
        int err = vkMapMemory(device, memory, 0, memAlloc.allocationSize(), 0, pData);
        validate(err, "Failed to map memory!");
        memAlloc.free();
        long bufferData = pData.get(0);
        memFree(pData);

        return bufferData;
    }

    /**
     * Creates a vertices buffer and fills it with provided data.
     *
     * @param physicalDevice A physical device that will be using the mesh.
     * @param device A device that will store the vertices information.
     * @param verticesData A buffer with the vertices data.
     * @return A handle to the vertices buffer.
     * @throws VulkanException When failed to create the buffer.
     */
    public static long createVerticesBuffer(
            VkPhysicalDevice physicalDevice, VkDevice device, ByteBuffer verticesData)
            throws VulkanException {

        long handle =
                createBuffer(
                        device, verticesData.remaining(), VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, 0);
        VkMemoryAllocateInfo memAlloc = getMemoryAllocateInfo(physicalDevice, device, handle);
        long memory = allocateMemory(device, memAlloc);
        long bufferData = mapMemory(device, memory, memAlloc);

        MemoryUtil.memCopy(memAddress(verticesData), bufferData, verticesData.remaining());
        vkUnmapMemory(device, memory);

        int err = vkBindBufferMemory(device, handle, memory, 0);
        validate(err, "Failed to bind memory to vertex buffer!");

        return handle;
    }
    
    /**
     * Creates a vertices buffer and fills it with provided data.
     *
     * @param physicalDevice A physical device that will be using the mesh.
     * @param device A device that will store the vertices information.
     * @param indicesData A buffer with the indices data.
     * @return A handle to the vertices buffer.
     * @throws VulkanException When failed to create the buffer.
     */
    public static long createIndicesBuffer(
            VkPhysicalDevice physicalDevice, VkDevice device, ByteBuffer indicesData)
            throws VulkanException {

        long handle =
                createBuffer(
                        device, indicesData.remaining(), VK_BUFFER_USAGE_INDEX_BUFFER_BIT, 0);
        VkMemoryAllocateInfo memAlloc = getMemoryAllocateInfo(physicalDevice, device, handle);
        long memory = allocateMemory(device, memAlloc);
        long bufferData = mapMemory(device, memory, memAlloc);

        MemoryUtil.memCopy(memAddress(indicesData), bufferData, indicesData.remaining());
        vkUnmapMemory(device, memory);

        int err = vkBindBufferMemory(device, handle, memory, 0);
        validate(err, "Failed to bind memory to index buffer!");

        return handle;
    }
}
