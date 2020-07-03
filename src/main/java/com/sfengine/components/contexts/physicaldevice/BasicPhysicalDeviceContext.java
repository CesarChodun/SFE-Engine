package com.sfengine.components.contexts.physicaldevice;

import com.sfengine.core.HardwareManager;
import com.sfengine.core.context.physicaldevice.PhysicalDeviceContext;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

import java.util.concurrent.locks.Lock;

import static org.lwjgl.vulkan.VK10.*;

public class BasicPhysicalDeviceContext implements PhysicalDeviceContext {

    private String name;
    private String factoryIdentifier;

    private volatile VkPhysicalDevice physicalDevice;

    private final DependencyFence deviceCreated = new DependencyFence();

    protected BasicPhysicalDeviceContext(String name, String factoryIdentifier) {
        this.name = name;
        this.factoryIdentifier = factoryIdentifier;

        init();
    }

    private void init() {
        final Engine engine = EngineFactory.getEngine();

        engine.addTask(() -> {
            physicalDevice = HardwareManager.getBestPhysicalDevice(
                    device -> {
                        VkPhysicalDeviceProperties props =
                                VkPhysicalDeviceProperties.calloc();
                        vkGetPhysicalDeviceProperties(device, props);

                        int out = 0;

                        if (props.deviceType() == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                            out = 2;
                        } else if (props.deviceType()
                                == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU) {
                            out = 1;
                        }

                        props.free();
                        return out;
                    });
            deviceCreated.release();
            System.out.println("Physical device released!");
        }, HardwareManager.getDependency());
    }

    @Override
    public VkPhysicalDevice getPhysicalDevice() {
        return physicalDevice;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFactoryIdentifier() {
        return factoryIdentifier;
    }

    @Override
    public Dependency getDependency() {
        return deviceCreated;
    }
}
