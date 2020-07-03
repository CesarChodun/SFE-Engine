package com.sfengine.components.contexts.queue;

import com.sfengine.core.context.*;
import com.sfengine.core.context.device.DeviceContext;
import com.sfengine.core.context.queue.QueueContext;
import com.sfengine.core.context.queuefamily.QueueFamilyContext;
import com.sfengine.core.engine.Engine;
import com.sfengine.core.engine.EngineFactory;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.lwjgl.PointerBuffer;
import org.lwjgl.vulkan.VkQueue;

import java.util.concurrent.locks.Lock;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;

public class BasicQueueContext implements QueueContext {

    private String name, factoryIdentifier;
    private Lock lock;

    private final DependencyFence queueCreated = new DependencyFence();

    private volatile VkQueue queue;

    private void init(ContextDictionary dict, int queueIndex) {
        Engine engine = EngineFactory.getEngine();
        final DeviceContext deviceContext = ContextUtil.getDevice(dict);
        final QueueFamilyContext familyContext = ContextUtil.getQueueFamily(dict);

        engine.addTask(() -> {
            PointerBuffer pQueue = memAllocPointer(1);
            vkGetDeviceQueue(deviceContext.getDevice(), familyContext.getQueueFamilyIndex(), queueIndex, pQueue);
            long queueHandle = pQueue.get(0);
            memFree(pQueue);
            queue = new VkQueue(queueHandle, deviceContext.getDevice());
            queueCreated.release();
        }, deviceContext.getDependency(), familyContext.getDependency());
    }

    protected BasicQueueContext(String name, String factoryIdentifier, ContextDictionary dict, int queueIndex) {
        this.name = name;
        this.factoryIdentifier = factoryIdentifier;
        init(dict, queueIndex);
    }

    @Override
    public VkQueue getQueue() {
        return queue;
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
        return queueCreated;
    }
}
