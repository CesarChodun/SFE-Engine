package demos.helloCube.rendering;

import org.joml.Matrix4f;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import rendering.engine.shader.Descriptor;
import rendering.engine.shader.DescriptorSet;
import rendering.engine.shader.GeneralizedDescriptorValue;
import rendering.engine.shader.UniformUsage;

/**
 * Descriptor set for camera and model transformation.
 * 
 * @author Cezary Chodun
 * @since 12.03.2020
 */
public class TransformationDescriptorSet extends DescriptorSet {

    private VkPhysicalDevice physicalDevice;
    private VkDevice device;
    private long descriptorSet;

    public TransformationDescriptorSet(
            VkPhysicalDevice physicalDevice, VkDevice device, long descriptorSet) {
        this.physicalDevice = physicalDevice;
        this.device = device;
        this.descriptorSet = descriptorSet;

        makeDescriptors();
    }

    /**
     * Creates needed descriptors.
     */
    private void makeDescriptors() {
        GeneralizedDescriptorValue[] descVals = new GeneralizedDescriptorValue[2];
        descVals[0] =
                new GeneralizedDescriptorValue(
                        physicalDevice,
                        device,
                        getDescriptorSet(),
                        0,
                        "transform",
                        UniformUsage.UNIFORM_USAGE_MATRIX_4F);
        
        addDescriptor("Model", new Descriptor(descVals[0]));
        descVals[0].setUniform(0, new Matrix4f());
        descVals[0].update();
        
        descVals[1] =
                new GeneralizedDescriptorValue(
                        physicalDevice,
                        device,
                        getDescriptorSet(),
                        1,
                        "transform",
                        UniformUsage.UNIFORM_USAGE_MATRIX_4F);
        
        addDescriptor("Camera", new Descriptor(descVals[1]));
        descVals[1].setUniform(0, new Matrix4f());
        descVals[1].update();
    }

    @Override
    public long getDescriptorSet() {
        return descriptorSet;
    }
}