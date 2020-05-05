package main.java.components.pipeline;

import static main.java.core.result.VulkanResult.validate;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAllocLong;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memUTF8;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;

import main.java.core.result.VulkanException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

public class ShaderUtil {

    /**
     * Converts file content to byte buffer.
     *
     * @param file
     * @return
     */
    public static ByteBuffer fileToByteBuffer(File file) {
        if (file.isDirectory()) {
            return null;
        }

        ByteBuffer buffer = null;

        try {
            FileInputStream fis = new FileInputStream(file);
            FileChannel fc = fis.getChannel();
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            fc.close();
            fis.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer;
    }

    /**
     * Creates shader stage.
     *
     * @param shaderModule - Shader module.
     * @param stage - Shader stage.
     * @param invokeName - Name of the method to be invoked.
     * @return
     */
    public static VkPipelineShaderStageCreateInfo createShaderStage(
            long shaderModule, int stage, String invokeName) {
        VkPipelineShaderStageCreateInfo shaderStage =
                VkPipelineShaderStageCreateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                        .pNext(NULL)
                        .stage(stage)
                        .module(shaderModule)
                        .pName(memUTF8(invokeName));
        return shaderStage;
    }

    /**
     * @param info - Info that will be filled
     * @param shaderModule - Shader module.
     * @param stage - Shader stage.
     * @param invokeName - Name of the method to be invoked.
     */
    public static void fillShaderStage(
            VkPipelineShaderStageCreateInfo info, long shaderModule, int stage, String invokeName) {
        info.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                .pNext(NULL)
                .stage(stage)
                .module(shaderModule)
                .pName(memUTF8(invokeName));
    }

    /**
     * Creates a shader module.
     *
     * @param logicalDevice
     * @param file
     * @return handle of the created shader module.
     * @throws VulkanException
     */
    public static long createShaderModule(VkDevice logicalDevice, File file)
            throws VulkanException {
        ByteBuffer shaderData = fileToByteBuffer(file);

        VkShaderModuleCreateInfo moduleCreateInfo =
                VkShaderModuleCreateInfo.calloc()
                        .sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                        .flags(0)
                        .pNext(NULL)
                        .pCode(shaderData);

        LongBuffer pShaderModule = memAllocLong(1);
        int err = vkCreateShaderModule(logicalDevice, moduleCreateInfo, null, pShaderModule);
        validate(err, "Failed to create shader module!");
        long handle = pShaderModule.get(0);

        memFree(pShaderModule);
        moduleCreateInfo.free();

        return handle;
    }
}
