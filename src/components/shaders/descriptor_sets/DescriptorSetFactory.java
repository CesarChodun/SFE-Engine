package components.shaders.descriptor_sets;

import static core.result.VulkanResult.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorPool;

import core.result.VulkanException;
import java.nio.LongBuffer;
import org.lwjgl.vulkan.EXTDescriptorIndexing;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDevice;

/**
 * Factory for creating descriptor sets.
 *
 * @author Cezary Chodun
 * @since 10.01.2020
 */
public class DescriptorSetFactory {

    /**
     * Creates descriptor sets.
     *
     * @param device <b>must</b> be a valid logical device.
     * @param blueprints a list of descriptor set blueprints for the descriptor sets that will be
     *     created.
     * @return An array of descriptor set handles.
     * @throws VulkanException if failed to create descriptor pool.
     */
    public static long[] createDescriptorSets(VkDevice device, DescriptorSetBlueprint... blueprints)
            throws VulkanException {
        int descriptors = 0;
        for (int i = 0; i < blueprints.length; i++) {
            descriptors += blueprints[i].descriptorCount();
        }

        VkDescriptorPoolSize.Buffer descriptorPoolSizes =
                VkDescriptorPoolSize.calloc(1)
                        .type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                        .descriptorCount(descriptors);

        VkDescriptorPoolCreateInfo descriptorPoolCreateInfo =
                VkDescriptorPoolCreateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                        .pNext(NULL)
                        .flags(
                                EXTDescriptorIndexing
                                        .VK_DESCRIPTOR_POOL_CREATE_UPDATE_AFTER_BIND_BIT_EXT)
                        .maxSets(blueprints.length)
                        .pPoolSizes(descriptorPoolSizes);

        LongBuffer pDescriptorPool = memAllocLong(1);
        int err = vkCreateDescriptorPool(device, descriptorPoolCreateInfo, null, pDescriptorPool);
        validate(err, "Failed to create descriptor pool.");
        long descriptorPool = pDescriptorPool.get(0);

        long[] dscs = new long[blueprints.length];
        for (int i = 0; i < blueprints.length; i++) {
            dscs[i] = createDescriptorSet(device, descriptorPool, blueprints[i].getLayout());
        }

        descriptorPoolSizes.free();
        descriptorPoolCreateInfo.free();
        memFree(pDescriptorPool);

        return dscs;
    }

    /**
     * Creates a descriptor set using an existing pool.
     *
     * @param device <b>must</b> be a valid logical device.
     * @param descriptorPool handle to the descriptor pool.
     * @param descriptorSetLayout handle to the descriptor set layout.
     * @return a handle to the created descriptor set.
     */
    public static long createDescriptorSet(
            VkDevice device, long descriptorPool, long descriptorSetLayout) {
        LongBuffer pDescriptorSetLayout = memAllocLong(1);
        pDescriptorSetLayout.put(descriptorSetLayout);
        pDescriptorSetLayout.flip();

        VkDescriptorSetAllocateInfo descriptorSetAllocateInfo =
                VkDescriptorSetAllocateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                        .pNext(NULL)
                        .descriptorPool(descriptorPool)
                        .pSetLayouts(pDescriptorSetLayout);

        LongBuffer pDescriptorSet = memAllocLong(1);
        int err = vkAllocateDescriptorSets(device, descriptorSetAllocateInfo, pDescriptorSet);
        if (err != VK_SUCCESS) {
            throw new AssertionError("Failed to allocate descriptor set.");
        }
        final long ans = pDescriptorSet.get(0);

        memFree(pDescriptorSet);
        descriptorSetAllocateInfo.free();
        memFree(pDescriptorSetLayout);

        return ans;
    }
}
