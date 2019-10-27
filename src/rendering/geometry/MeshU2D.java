package rendering.geometry;

import static core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memAllocInt;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;

import org.joml.Vector2f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;

import core.result.VulkanException;

/**
 * Unindexed 2D mesh.
 * 
 * @author Cezary Chodun
 *
 */
public class MeshU2D implements Mesh{

	private long verticesBuffer;
	private int verticesCount;
	
	public MeshU2D(VkPhysicalDevice physicalDevice, VkDevice logicalDevice, List<Vector2f> vertices) throws VulkanException {
		createMesh(physicalDevice, logicalDevice, vertices);
	}
	
	private void createMesh(VkPhysicalDevice physicalDevice, VkDevice logicalDevice, List<Vector2f> vertices) throws VulkanException {
		
		verticesCount = vertices.size();
		
		VkPhysicalDeviceMemoryProperties pMemoryProperties = VkPhysicalDeviceMemoryProperties.calloc();
		vkGetPhysicalDeviceMemoryProperties(physicalDevice, pMemoryProperties);
		
		ByteBuffer vertexBuffer = memAlloc(verticesCount * 2 * 4);		

		FloatBuffer fb = vertexBuffer.asFloatBuffer();
		for (int i = 0; i < verticesCount; i++)
			fb.put(vertices.get(i).x).put(vertices.get(i).y);
		fb.flip();
		 
		VkMemoryAllocateInfo memAlloc = VkMemoryAllocateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
				.pNext(NULL)
				.allocationSize(0)
				.memoryTypeIndex(0);
		VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
		 
		 VkBufferCreateInfo bufInfo = VkBufferCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
				.pNext(NULL)
				.usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
				.size(vertexBuffer.remaining())
				.flags(0);
		 
		 LongBuffer pBuffer = memAllocLong(1);
		 int err = vkCreateBuffer(logicalDevice, bufInfo, null, pBuffer);
		 validate(err, "Failed to create buffer!");
		 verticesBuffer = pBuffer.get(0);
		 memFree(pBuffer);
		 bufInfo.free();
		 
		 vkGetBufferMemoryRequirements(logicalDevice, verticesBuffer, memReqs);
		 memAlloc.allocationSize(memReqs.size());
		 IntBuffer memoryTypeIndex = memAllocInt(1);
		 if(!Util.getMemoryType(pMemoryProperties, memReqs.memoryTypeBits(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, memoryTypeIndex))
			 throw new AssertionError("Failed to obtain memry type.");
		 
		 memAlloc.memoryTypeIndex(memoryTypeIndex.get(0));
		 memFree(memoryTypeIndex);
		 memReqs.free();
		 
		 LongBuffer pMemory = memAllocLong(1);
		 err = vkAllocateMemory(logicalDevice, memAlloc, null, pMemory);
		 long memory = pMemory.get(0);
		 validate(err, "Failed to alocate vertex memory!");
		 
		 PointerBuffer pData = memAllocPointer(1);
		 err = vkMapMemory(logicalDevice, memory, 0, memAlloc.allocationSize(), 0, pData);
		 validate(err, "Failed to map memory!");
		 memAlloc.free();
		 long data = pData.get(0);
		 memFree(pData);
		 
		 MemoryUtil.memCopy(memAddress(vertexBuffer), data, vertexBuffer.remaining());
		 memFree(vertexBuffer);
		 vkUnmapMemory(logicalDevice, memory);
		 err = vkBindBufferMemory(logicalDevice, verticesBuffer, memory, 0);
		 validate(err, "Failed to bind memory to vertex buffer!");
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getVerticesHandle() {
		return verticesBuffer;
	}

	@Override
	public int getStride() {
		return 2 * 4;
	}

	@Override
	public int verticesCount() {
		return verticesCount;
	}	
	
}
