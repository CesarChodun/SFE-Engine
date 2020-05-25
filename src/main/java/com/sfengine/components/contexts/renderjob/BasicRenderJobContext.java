package com.sfengine.components.contexts.renderjob;

import com.sfengine.core.context.renderjob.RenderJobContext;
import com.sfengine.core.rendering.Renderer;
import com.sfengine.core.rendering.factories.CommandBufferFactory;
import com.sfengine.core.rendering.recording.RenderJob;
import com.sfengine.core.synchronization.Dependency;
import com.sfengine.core.synchronization.DependencyFence;
import com.sun.tracing.dtrace.DependencyClass;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

public class BasicRenderJobContext implements RenderJobContext {

    private volatile String name, factoryIdentifier;
    private volatile CommandBufferFactory cmdFactory;

    private final DependencyFence created = new DependencyFence();

    private final Map<Long, RenderJob> cmds = new HashMap<>();

    private static class BasicRenderJob implements RenderJob {

        private VkCommandBuffer cmd;

        public BasicRenderJob(VkCommandBuffer cmd) {
            this.cmd = cmd;
        }

        @Override
        public VkCommandBuffer getCMD() {
            return cmd;
        }

        @Override
        public void performUpdate() {

        }
    }

    public BasicRenderJobContext(String name, String factoryIdentifier, CommandBufferFactory cmdFactory) {
        this.name = name;
        this.factoryIdentifier = factoryIdentifier;
        this.cmdFactory = cmdFactory;
        created.release();
    }

    @Override
    public void recreateJobs(long... frameBuffers) {
        VkCommandBuffer[] bufs = cmdFactory.createCmdBuffers(Renderer.width, Renderer.height, frameBuffers);

        for (int i = 0; i < frameBuffers.length; i++)
            cmds.put(frameBuffers[i], new BasicRenderJob(bufs[i]));
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
    public Lock getLock() {
        return null;
    }

    @Override
    public Dependency getDependency() {
        return created;
    }
}
