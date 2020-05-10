package com.sfengine.components.contexts;

import static org.lwjgl.system.MemoryUtil.*;

import com.sfengine.core.Application;
import com.sfengine.core.HardwareManager;
import com.sfengine.core.context.*;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.rendering.RenderUtil;
import com.sfengine.core.resources.Asset;
import com.sfengine.core.resources.ConfigFile;
import com.sfengine.core.result.VulkanException;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class BasicDeviceContext implements DeviceContext {
    protected static final String CFG_EXT = ".cfg";

    protected static final String
            LAYERS_KEY = "layers",
            EXTENSIONS_KEY = "extensions",
            FLAGS_KEY = "flags",
            QUEUE_PRIORITIES_KEY = "queuePriorities";

    protected static final List<Float> DEFAULT_QUEUE_PRIORITIES = new ArrayList<>(Arrays.asList(0.0f));
    protected static final List<String>
            DEFAULT_LAYERS = new ArrayList<>(),
            DEFAULT_EXTENSIONS = new ArrayList<>(Arrays.asList(EXTDescriptorIndexing.VK_EXT_DESCRIPTOR_INDEXING_EXTENSION_NAME));

    private String name;
    private String factoryIdentifier;

    private volatile VkDevice device;

    private final DependencyFence deviceCreated = new DependencyFence();

    protected BasicDeviceContext(String name, String factoryIdentifier, ContextDictionary dict){
        this.name = name;
        this.factoryIdentifier = factoryIdentifier;
        init(dict);
    }

    private void init(ContextDictionary dict) {

        final Engine engine = EngineFactory.getEngine();
        final DeviceContextFactory factory =
                ContextFactoryProvider.getFactory(DeviceContextFactory.CONTEXT_IDENTIFIER, DeviceContextFactory.class);

        final PhysicalDeviceContext physicalDeviceContext = ContextUtil.getPhysicalDevice(dict);
        final QueueFamilyContext queueFamilyContext = ContextUtil.getQueueFamily(dict);

        if (queueFamilyContext == null)
            throw new AssertionError("No queue family context.");

        engine.addTask(() -> {
            Asset configLocation = ContextFactoryProvider.getSubasset(factory, Application.getConfigAssets());

            List<Float> queuePriorities;
            List<String> layers, extensions;
            int flags;

            ConfigFile cfg = null;
            try {
                cfg = configLocation.getConfigFile(name + CFG_EXT);

                queuePriorities = cfg.getFloatArray(QUEUE_PRIORITIES_KEY, DEFAULT_QUEUE_PRIORITIES);
                layers = cfg.getStringArray(LAYERS_KEY, DEFAULT_LAYERS);
                extensions = cfg.getStringArray(EXTENSIONS_KEY, DEFAULT_EXTENSIONS);

                flags = cfg.getFlags(VK10.class, FLAGS_KEY);
            } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
                throw new AssertionError("Failed to load device data.", e);
            } finally {
                if (cfg != null)
                    cfg.close();
            }

            FloatBuffer queuePrioritiesp = memAllocFloat(queuePriorities.size());
            for (int i = 0; i < queuePriorities.size(); i++)
                queuePrioritiesp.put(queuePriorities.get(i));
            queuePrioritiesp.flip();

            if (layers.size() == 0)
                layers = null;

            try {
                device = RenderUtil.createLogicalDevice(
                        physicalDeviceContext.getPhysicalDevice(),
                        queueFamilyContext.getQueueFamilyIndex(),
                        queuePrioritiesp, flags, layers, extensions);
            } catch (VulkanException e) {
                throw new AssertionError("Failed to create vulkan device.", e);
            }
            finally {
                memFree(queuePrioritiesp);
            }

            deviceCreated.release();
            System.out.println("Device released!");
        }, HardwareManager.getDependency(), physicalDeviceContext.getDependency(), queueFamilyContext.getDependency());
    }

    @Override
    public VkDevice getDevice() {
        return device;
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
    public Lock getLock() {
        return null;
    }

    @Override
    public Dependency getDependency() {
        return deviceCreated;
    }
}
