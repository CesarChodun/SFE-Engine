package rendering.pipeline;

import static core.result.VulkanResult.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.*;

import core.result.VulkanException;
import java.nio.LongBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import rendering.engine.shader.DescriptorSetBlueprint;

/**
 * @author Cezary Chodun
 * @since 20.12.2019
 */
public class PipelineLayout {

    private DescriptorSetBlueprint[] layouts;
    private long pipelineLayout;

    private VkDevice device;

    /**
     * Creates a pipeline layout based on the descriptor set blueprints.
     *
     * @param device <b>must</b> be a valid logical device.
     * @param layouts <b>must</b> be valid descriptor layouts.
     */
    public PipelineLayout(VkDevice device, DescriptorSetBlueprint... layouts) {
        this.layouts = layouts;
        this.device = device;

        try {
            createPipelineLayout();
        } catch (VulkanException e) {
            System.err.println("Failed to create pipeline layout.");
            e.printStackTrace();
        }
    }

    private void createPipelineLayout() throws VulkanException {

        VkPipelineLayoutCreateInfo pPipelineLayoutCreateInfo =
                VkPipelineLayoutCreateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                        .pNext(NULL)
                        .flags(0)
                        .pPushConstantRanges(null)
                        .pSetLayouts(null);

        LongBuffer pSetLayouts = null;
        if (layouts.length != 0) {
            pSetLayouts = memAllocLong(layouts.length);
            for (int i = 0; i < layouts.length; i++) {
                pSetLayouts.put(i, layouts[i].getLayout());
            }

            pPipelineLayoutCreateInfo.pSetLayouts(pSetLayouts);
        }

        LongBuffer pPipelineLayout = memAllocLong(1);
        int err = vkCreatePipelineLayout(device, pPipelineLayoutCreateInfo, null, pPipelineLayout);
        validate(err, "Failed to create pipeline layout!");

        pipelineLayout = pPipelineLayout.get(0);

        if (pSetLayouts != null) {
            memFree(pSetLayouts);
        }
        memFree(pPipelineLayout);
        pPipelineLayoutCreateInfo.free();
    }

    /**
     * Returns the vulkan pipeline layout.
     *
     * @return handle to the layout.
     */
    public long getPipelineLayout() {
        return pipelineLayout;
    }
}
