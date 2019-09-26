package geometry;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

import static core.rendering.RenderUtil.*;
import static core.result.VulkanResult.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;

import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import core.result.VulkanException;


/**
 * Class for storing information about 3D mesh.
 * 
 * @author Cezary Chodun
 *
 */
public class Mesh {
	
	public static class Vertices {
		protected long verticesBuffer;
		protected VkPipelineVertexInputStateCreateInfo createInfo;
		public long getVerticesBuffer() {
			return verticesBuffer;
		}
		public void setVerticesBuffer(long verticesBuffer) {
			this.verticesBuffer = verticesBuffer;
		}
		public VkPipelineVertexInputStateCreateInfo getCreateInfo() {
			return createInfo;
		}
		public void setCreateInfo(VkPipelineVertexInputStateCreateInfo createInfo) {
			this.createInfo = createInfo;
		}
	}

	/**
	 * Vertices buffer(for 3D mesh).
	 */
	protected FloatBuffer vertices;
	
	/**
	 * Creates a new mesh with a given vertices buffer.
	 * @param ver		vertices buffer
	 */
	public Mesh(FloatBuffer ver) {
		this.vertices = ver;
	}
	
	/**
	 * Creates a new mesh with the given vertices.
	 * @param vec		list of vertices
	 */
	public Mesh(List<Vector3f> vec) {
		vertices = memAllocFloat(vec.size() * 3 * Float.BYTES);
		
		for(int i = 0; i < vec.size(); i++) 
			vertices.put(vec.get(i).x).put(vec.get(i).y).put(vec.get(i).z);
		
		vertices.flip();
	}

	/**
	 * 
	 * @return		the vertices buffer
	 */
	public FloatBuffer getVertices() {
		return vertices;
	}

	@Deprecated 
	//Update
	public Vertices getVkVertices(VkDevice device, VkPhysicalDevice physicalDevice) throws VulkanException {
		 
		 VkMemoryAllocateInfo memAlloc = VkMemoryAllocateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
				 .pNext(NULL)
				 .allocationSize(0)
				 .memoryTypeIndex(0);
		 VkMemoryRequirements memReqs = VkMemoryRequirements.calloc();
		 
		 VkBufferCreateInfo bufInfo = VkBufferCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
				 .pNext(NULL)
				 .size(vertices.remaining() * Float.BYTES)
				 .flags(0);
		 
		 LongBuffer pBuffer = memAllocLong(1);
		 int err = vkCreateBuffer(device, bufInfo, null, pBuffer);
		 validate(err, "Failed to create buffer!");
		 long verticesBuffer = pBuffer.get(0);
		 memFree(pBuffer);
		 bufInfo.free();
		 
		 vkGetBufferMemoryRequirements(device, verticesBuffer, memReqs);
		 memAlloc.allocationSize(memReqs.size());
		 IntBuffer memoryTypeIndex = memAllocInt(1);
		 if(!getMemoryType(physicalDevice, memReqs.memoryTypeBits(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, memoryTypeIndex))
			 throw new AssertionError("Failed to obtain memry type.");
		 memAlloc.memoryTypeIndex(memoryTypeIndex.get(0));
		 memFree(memoryTypeIndex);
		 memReqs.free();
		 
		 LongBuffer pMemory = memAllocLong(1);
		 err = vkAllocateMemory(device, memAlloc, null, pMemory);
		 validate(err, "Failed to alocate vertex memory!");
		 long memory = pMemory.get(0);
		 
		 PointerBuffer pData = memAllocPointer(1);
		 err = vkMapMemory(device, memory, 0, memAlloc.allocationSize(), 0, pData);
		 validate(err, "Failed to map memory!");
		 memAlloc.free();
		 long data = pData.get(0);
		 memFree(pData);
		 
		 MemoryUtil.memCopy(memAddress(vertices), data, vertices.remaining() * Float.BYTES);
		 vkUnmapMemory(device, memory);
		 err = vkBindBufferMemory(device, verticesBuffer, memory, 0);
		 validate(err, "Failed to bind memory to vertex buffer!");
		 
		 //Binding:
		 VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1)
				 .binding(0) // <- we bind our vertex buffer to point 0
				 .stride(2*4)
				 .inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
		 
		 // Attribute descriptions
		 // Describes memory layout and shader attribute locations
		 VkVertexInputAttributeDescription.Buffer attributeDescription = VkVertexInputAttributeDescription.calloc(1);
		 //Location 0 : Position
		 attributeDescription.get(0)
		 		.binding(0)
		 		.location(0)
		 		.format(VK_FORMAT_R32G32_SFLOAT)
		 		.offset(0);
		 
		 //asign to vertex buffer
		 VkPipelineVertexInputStateCreateInfo vertexCreateInfo = VkPipelineVertexInputStateCreateInfo.calloc()
				 .sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
				 .pNext(NULL)
				 .pVertexBindingDescriptions(bindingDescription)
				 .pVertexAttributeDescriptions(attributeDescription);
		 
		 Vertices ret = new Vertices();
		 ret.createInfo = vertexCreateInfo;
		 ret.verticesBuffer = verticesBuffer;
		 
		 return ret;
	}
}
