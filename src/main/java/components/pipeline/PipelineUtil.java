package main.java.components.pipeline;

import static main.java.core.result.VulkanResult.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.EXTDescriptorIndexing.*;
import static org.lwjgl.vulkan.VK10.*;

import main.java.core.result.VulkanException;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBindingFlagsCreateInfoEXT;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;

/**
 * Package with utility methods for pipeline management.
 *
 * @author Cezary Chodun
 * @since 20.12.2019
 */
public class PipelineUtil {

    /**
     * Creates a descriptor set layout.
     *
     * @param device - Logical device
     * @param layoutBindings - Layout bindings
     * @return - Handle to descriptor set layout
     * @throws VulkanException
     */
    public static long createDescriptorSetLayout(
            VkDevice device, VkDescriptorSetLayoutBinding.Buffer layoutBindings)
            throws VulkanException {
        IntBuffer bindingFlagsBuffer = memAllocInt(layoutBindings.remaining());

        for (int i = 0; i < bindingFlagsBuffer.remaining(); i++) {
            bindingFlagsBuffer.put(i, VK_DESCRIPTOR_BINDING_UPDATE_AFTER_BIND_BIT_EXT);
        }

        VkDescriptorSetLayoutBindingFlagsCreateInfoEXT bindingFlags =
                VkDescriptorSetLayoutBindingFlagsCreateInfoEXT.calloc()
                        .sType(
                                VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_BINDING_FLAGS_CREATE_INFO_EXT)
                        .pNext(NULL)
                        .pBindingFlags(bindingFlagsBuffer);

        VkDescriptorSetLayoutCreateInfo layoutCreateInfo =
                VkDescriptorSetLayoutCreateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                        .pNext(bindingFlags.address())
                        .flags(VK_DESCRIPTOR_SET_LAYOUT_CREATE_UPDATE_AFTER_BIND_POOL_BIT_EXT)
                        // VK_DESCRIPTOR_BINDING_UPDATE_AFTER_BIND_BIT_EXT for thread
                        // independent descriptor updates
                        .pBindings(layoutBindings);

        LongBuffer pSetLayout = memAllocLong(1);
        int err = vkCreateDescriptorSetLayout(device, layoutCreateInfo, null, pSetLayout);
        validate(err, "Filed to create descriptor set layout.");

        final long ans = pSetLayout.get(0);

        memFree(pSetLayout);
        layoutCreateInfo.free();
        memFree(bindingFlagsBuffer);
        bindingFlags.free();

        return ans;
    }
}
