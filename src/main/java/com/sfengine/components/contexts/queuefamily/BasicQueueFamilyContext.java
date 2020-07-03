package com.sfengine.components.contexts.queuefamily;

import com.sfengine.core.Application;
import com.sfengine.core.HardwareManager;
import com.sfengine.core.context.*;
import com.sfengine.core.context.physicaldevice.PhysicalDeviceContext;
import com.sfengine.core.context.queuefamily.QueueFamilyContext;
import com.sfengine.core.context.queuefamily.QueueFamilyContextFactory;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.resources.ConfigFile;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;

import static com.sfengine.core.hardware.HardwareUtil.getNextQueueFamilyIndex;
import static com.sfengine.core.hardware.HardwareUtil.newQueueFamilyProperties;

public class BasicQueueFamilyContext implements QueueFamilyContext {

    public static final String QUEUE_FAMILY_REQUIREMENTS_KEY = "queue_requirements";

    private volatile String name, factoryIdentifier;
    private volatile int index;

    private DependencyFence indexSelected = new DependencyFence();

    private int requirements;

    private void init(String name, ContextDictionary dict) {
        final Engine engine = EngineFactory.getEngine();
        final PhysicalDeviceContext physicalDeviceContext = ContextUtil.getPhysicalDevice(dict);
        final QueueFamilyContextFactory factory =
                ContextFactoryProvider.getFactory(
                        QueueFamilyContextFactory.CONTEXT_IDENTIFIER,
                        QueueFamilyContextFactory.class);

        final DependencyFence configLoaded = new DependencyFence();

        engine.addConfig(() -> {
            ConfigFile cfg = null;
            try {
                cfg = ContextFactoryProvider.getSubasset(factory, Application.getConfigAssets()).getConfigFile(name);
                requirements = cfg.getFlags(
                        VK10.class, QUEUE_FAMILY_REQUIREMENTS_KEY, new ArrayList<String>());
            } catch (NoSuchFieldException | IllegalAccessException | IOException e) {
                throw new AssertionError("Failed to obtain queue family requirements.", e);
            }
            finally {
                if (cfg != null)
                    cfg.close();
            }
            configLoaded.release();
        }, HardwareManager.getDependency());

        engine.addTask(() -> {
            VkQueueFamilyProperties.Buffer queueFamilyProperties =
                    newQueueFamilyProperties(physicalDeviceContext.getPhysicalDevice());
            index = getNextQueueFamilyIndex(0, requirements, queueFamilyProperties);
            queueFamilyProperties.free();
            System.out.println("Queue family released!");
            indexSelected.release();
        }, physicalDeviceContext.getDependency(), configLoaded);
    }

    public BasicQueueFamilyContext(String name, String factoryIdentifier, ContextDictionary dict) {
        this.name = name;
        this.factoryIdentifier = factoryIdentifier;
        init(name, dict);
    }

    @Override
    public int getQueueFamilyIndex() {
        return index;
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
        return indexSelected;
    }
}
