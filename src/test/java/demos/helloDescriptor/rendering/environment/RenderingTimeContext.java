package demos.helloDescriptor.rendering.environment;

import com.sfengine.components.shaders.GeneralizedDescriptorValue;
import com.sfengine.components.shaders.UniformUsage;
import com.sfengine.components.shaders.descriptors.DescriptorBlueprint;
import com.sfengine.components.shaders.descriptors.DescriptorValue;
import com.sfengine.core.resources.Destroyable;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;

public class RenderingTimeContext implements DescriptorBlueprint, Destroyable {

    private VkPhysicalDevice physicalDevice;
    private VkDevice device;
    private long descriptorSet;

    private GeneralizedDescriptorValue[] data;

    public RenderingTimeContext(
            VkPhysicalDevice physicalDevice, VkDevice device, long descriptorSet) {
        this.physicalDevice = physicalDevice;
        this.device = device;
        this.descriptorSet = descriptorSet;

        createDescriptorValues();
    }

    private void createDescriptorValues() {
        data = new GeneralizedDescriptorValue[1];

        data[0] =
                new GeneralizedDescriptorValue(
                        physicalDevice,
                        device,
                        descriptorSet,
                        0,
                        "time",
                        UniformUsage.UNIFORM_USAGE_INT_16);
    }

    @Override
    public void destroy() {
        for (int i = 0; i < data.length; i++) {
            data[i].destroy();
        }
    }

    @Override
    public DescriptorValue[] getDescriptorValues() {
        return data;
    }
}
