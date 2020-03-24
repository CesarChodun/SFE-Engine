package components.shaders;

import static components.memory.RenderingMemoryUtil.*;
import static components.shaders.UniformUsage.UNIFORM_USAGE_DOUBLE;
import static components.shaders.UniformUsage.UNIFORM_USAGE_FLOAT;
import static components.shaders.UniformUsage.UNIFORM_USAGE_INT_16;
import static components.shaders.UniformUsage.UNIFORM_USAGE_INT_32;
import static components.shaders.UniformUsage.UNIFORM_USAGE_INT_64;
import static components.shaders.UniformUsage.UNIFORM_USAGE_MATRIX_4F;
import static components.shaders.UniformUsage.UNIFORM_USAGE_VECTOR_3F;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;

import core.result.VulkanException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import components.memory.GPUBuffer;
import components.shaders.descriptors.DescriptorValue;

/**
 * @author Cezary Chodun
 * @since 10.01.2020
 */
public class GeneralizedDescriptorValue implements DescriptorValue {

    private static final Logger logger =
            Logger.getLogger(GeneralizedDescriptorValue.class.getName());

    protected UniformUsage[] slots;
    private int[] prefTab;
    private int dataSize;
    protected ByteBuffer data;
    private VkWriteDescriptorSet.Buffer write;

    private VkDevice device;
    private VkDescriptorBufferInfo.Buffer bufferInfo;
    private GPUBuffer gpuBuffer = null;

    private String name;
    private boolean upToDate = false;

    public GeneralizedDescriptorValue(
            VkPhysicalDevice physicalDevice,
            VkDevice device,
            long descriptorSet,
            int binding,
            String name,
            UniformUsage... uniformUsages) {
        this.device = device;
        this.slots = uniformUsages;
        this.name = name;

        dataSize = 0;
        prefTab = new int[slots.length];
        prefTab[0] = 0;
        for (int i = 0; i < slots.length; i++) {
            dataSize += slots[i].sizeOf();
            if (i < slots.length - 1) {
                prefTab[i + 1] = prefTab[i] + slots[i].sizeOf();
            }
        }

        data = memAlloc(dataSize);

        try {
            gpuBuffer =
                    allocateBufferMemoryGPU(
                            device,
                            physicalDevice,
                            dataSize,
                            VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                            VK_SHARING_MODE_EXCLUSIVE);
        } catch (VulkanException e) {
            throw new AssertionError("Failed to allocate buffer memory!");
        }

        bufferInfo = VkDescriptorBufferInfo.calloc(slots.length);
        for (int i = 0; i < slots.length; i++) {
            bufferInfo.get(i).buffer(gpuBuffer.buffer).offset(prefTab[i]).range(slots[i].sizeOf());
        }

        write =
                VkWriteDescriptorSet.calloc(1)
                        .sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                        .pNext(NULL)
                        .dstSet(descriptorSet)
                        .descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                        .descriptorCount(1)
                        .pBufferInfo(bufferInfo)
                        .dstArrayElement(0)
                        .dstBinding(binding);

        try {
            bindBufferMemoryGPU(device, gpuBuffer, data);
        } catch (VulkanException e) {
            logger.log(Level.FINER, "Failed to bind buffer to the GPU memory.");
            e.printStackTrace();
        }
    }

    public UniformUsage getUniformUsage(int index) {
        return slots[index];
    }

    @Override
    public void update() {
        if (upToDate == true) {
            return;
        }
        upToDate = true;

        try {
            copyToBufferMemoryGPU(device, gpuBuffer, data);
        } catch (VulkanException e) {
            logger.log(Level.FINER, "Failed to copy buffer to the GPU memory", e);
            e.printStackTrace();
        }
        vkUpdateDescriptorSets(device, write, null);
    }

    public void setUniform(int index, short val) {
        checkCompatibility(index, UNIFORM_USAGE_INT_16);

        data.putShort(prefTab[index], val);
    }

    public void setUniform(int index, int val) {
        checkCompatibility(index, UNIFORM_USAGE_INT_32);

        data.putInt(prefTab[index], val);
    }

    public void setUniform(int index, long val) {
        checkCompatibility(index, UNIFORM_USAGE_INT_64);

        data.putLong(prefTab[index], val);
    }

    public void setUniform(int index, float val) {
        checkCompatibility(index, UNIFORM_USAGE_FLOAT);

        data.putFloat(prefTab[index], val);
    }

    public void setUniform(int index, double val) {
        checkCompatibility(index, UNIFORM_USAGE_DOUBLE);

        data.putDouble(prefTab[index], val);
    }

    public void setUniform(int index, Vector3f val) {
        checkCompatibility(index, UNIFORM_USAGE_VECTOR_3F);

        val.get(prefTab[index], data);
    }

    public void setUniform(int index, Vector4f val) {
        checkCompatibility(index, UNIFORM_USAGE_VECTOR_3F);

        val.get(prefTab[index], data);
    }

    public void setUniform(int index, Matrix4f val) {
        checkCompatibility(index, UNIFORM_USAGE_MATRIX_4F);

        val.get(prefTab[index], data);
    }

    private void checkCompatibility(int index, UniformUsage usage) {
        if (slots[index].dataType().compareTo(usage) != 0) {
            throw new Error(
                    "Uniform usage mismatch! Expected: "
                            + slots[index].toString()
                            + " Recived: "
                            + usage.name());
        }

        upToDate = false;
    }

    @Override
    public boolean isUpToDate() {
        return upToDate;
    }

    @Override
    public void destroy() {
        memFree(data);
        write.free();
        bufferInfo.free();
    }

    @Override
    public String name() {
        return name;
    }
}
