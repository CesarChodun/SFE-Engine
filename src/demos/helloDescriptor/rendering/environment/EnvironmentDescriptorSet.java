package demos.helloDescriptor.rendering.environment;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;

import components.shaders.GeneralizedDescriptorValue;
import components.shaders.UniformUsage;
import components.shaders.descriptor_sets.DescriptorSet;
import components.shaders.descriptors.Descriptor;

public class EnvironmentDescriptorSet extends DescriptorSet {

    private VkPhysicalDevice physicalDevice;
    private VkDevice device;
    private long descriptorSet;

    public EnvironmentDescriptorSet(
            VkPhysicalDevice physicalDevice, VkDevice device, long descriptorSet) {
        this.physicalDevice = physicalDevice;
        this.device = device;
        this.descriptorSet = descriptorSet;

        makeDescriptors();
    }

    private void makeDescriptors() {
        GeneralizedDescriptorValue[] descVals = new GeneralizedDescriptorValue[1];
        descVals[0] =
                new GeneralizedDescriptorValue(
                        physicalDevice,
                        device,
                        getDescriptorSet(),
                        0,
                        "miliTime",
                        UniformUsage.UNIFORM_USAGE_INT_32);
        descVals[0].setUniform(0, 5);
        descVals[0].update();
        addDescriptor("TimeData", new Descriptor(descVals[0]));
    }

    @Override
    public long getDescriptorSet() {
        return descriptorSet;
    }
}
