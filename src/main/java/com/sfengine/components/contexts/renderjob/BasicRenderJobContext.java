package com.sfengine.components.contexts.renderjob;

import com.sfengine.core.context.ContextDictionary;
import com.sfengine.core.context.ContextUtil;
import com.sfengine.core.context.renderjob.RenderJobContext;
import com.sfengine.core.context.swapchain.SwapchainContext;
import com.sfengine.core.rendering.CommandBufferFactory;
import com.sfengine.core.rendering.recording.RenderJob;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

public class BasicRenderJobContext implements RenderJobContext {

    private volatile String name, factoryIdentifier;
    private volatile CommandBufferFactory cmdFactory;

    private final DependencyFence created = new DependencyFence();

    private final Map<Long, RenderJob> cmds = new HashMap<>();

    private ContextDictionary dict;
    private Runnable update;

    private static class BasicRenderJob implements RenderJob {

        private VkCommandBuffer cmd;
        private Runnable update;

        public BasicRenderJob(VkCommandBuffer cmd, @Nullable Runnable update) {
            this.cmd = cmd;
            this.update = update;
        }

        @Override
        public VkCommandBuffer getCMD() {
            return cmd;
        }

        @Override
        public void performUpdate() {
            if (update != null)
                update.run();
        }
    }

    public BasicRenderJobContext(String name, ContextDictionary dict, String factoryIdentifier, CommandBufferFactory cmdFactory, @Nullable Runnable update) {
        this.name = name;
        this.factoryIdentifier = factoryIdentifier;
        this.cmdFactory = cmdFactory;
        created.release();
        this.dict = dict;
        this.update = update;
    }

    @Override
    public void recreateJobs(long... frameBuffers) {
        VkCommandBuffer[] bufs = cmdFactory.createCmdBuffers(frameBuffers);

        for (int i = 0; i < frameBuffers.length; i++)
            cmds.put(frameBuffers[i], new BasicRenderJob(bufs[i], update));
    }

    @Override
    public RenderJob getJob(long frameBuffer) {
        return cmds.get(frameBuffer);
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
        return created;
    }
}
