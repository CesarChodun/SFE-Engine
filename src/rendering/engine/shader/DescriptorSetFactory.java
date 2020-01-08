package rendering.engine.shader;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorPool;

import static core.result.VulkanResult.*;

import java.nio.LongBuffer;

import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDevice;

import core.result.VulkanException;

public class DescriptorSetFactory {

	public static long[] createDescriptorSets(VkDevice device, DescriptorSetBlueprint... blueprints) throws VulkanException {
		int descriptors = 0;
		for (int i = 0; i < blueprints.length; i++)
			descriptors += blueprints[i].descriptorCount();
		
		VkDescriptorPoolSize.Buffer descriptorPoolSizes = VkDescriptorPoolSize.calloc(1)
				.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
				.descriptorCount(descriptors);
		
		VkDescriptorPoolCreateInfo descriptorPoolCreateInfo = VkDescriptorPoolCreateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
				.pNext(NULL)
				.maxSets(blueprints.length)
				.pPoolSizes(descriptorPoolSizes);
		
		LongBuffer pDescriptorPool = memAllocLong(1);
		int err = vkCreateDescriptorPool(device, descriptorPoolCreateInfo, null, pDescriptorPool);
		validate(err, "Failed to create descriptor pool.");
		long descriptorPool = pDescriptorPool.get(0);
		
		long[] dscs = new long[blueprints.length];
		for (int i = 0; i < blueprints.length; i++)
			dscs[i] = createDescriptorSet(device, descriptorPool, blueprints[i].getLayout());
		
		descriptorPoolSizes.free();
		descriptorPoolCreateInfo.free();
		memFree(pDescriptorPool);
		
		return dscs;
	}
	
	public static long createDescriptorSet(VkDevice device, long descriptorPool, long descriptorSetLayout) {
		LongBuffer pDescriptorSetLayout = memAllocLong(1);
		pDescriptorSetLayout.put(descriptorSetLayout);
		pDescriptorSetLayout.flip();
		
		VkDescriptorSetAllocateInfo descriptorSetAllocateInfo = VkDescriptorSetAllocateInfo.calloc()
				.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
				.pNext(NULL)
				.descriptorPool(descriptorPool)
				.pSetLayouts(pDescriptorSetLayout);

		LongBuffer pDescriptorSet = memAllocLong(1);
		int err = vkAllocateDescriptorSets(device, descriptorSetAllocateInfo, pDescriptorSet);
		if(err != VK_SUCCESS)
			throw new AssertionError("Failed to allocate descriptor set.");
		long ans = pDescriptorSet.get(0);
		
		memFree(pDescriptorSet);
		descriptorSetAllocateInfo.free();
		memFree(pDescriptorSetLayout);
		
		return ans;
	}
}
