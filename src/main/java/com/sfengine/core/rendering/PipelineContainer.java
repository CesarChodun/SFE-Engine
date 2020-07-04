package com.sfengine.core.rendering;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.*;

import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;

public class PipelineContainer implements RenderContainer {

    private final List<RenderObject> objs = Collections.synchronizedList(new ArrayList<>());
    private volatile long pipeline;
    private volatile int bindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;

    public PipelineContainer(long pipeline) {
        this.pipeline = pipeline;
    }

    private void recordPipeline(VkCommandBuffer buffer, long framebuffer) {
        vkCmdBindPipeline(buffer, bindPoint, pipeline);
    }

    @Override
    public void record(VkCommandBuffer buffer, long framebuffer) {
        recordPipeline(buffer, framebuffer);

        synchronized (objs) {
            for (RenderObject obj : objs)
                obj.record(buffer, framebuffer);
        }
    }

    @Override
    public int size() {
        return objs.size();
    }

    @Override
    public boolean isEmpty() {
        return objs.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return objs.contains(o);
    }

    @NotNull
    @Override
    public Iterator<RenderObject> iterator() {
        return objs.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return objs.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        return objs.toArray(a);
    }

    @Override
    public boolean add(RenderObject renderObject) {
        return objs.add(renderObject);
    }

    @Override
    public boolean remove(Object o) {
        return objs.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return objs.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends RenderObject> c) {
        return objs.addAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return objs.retainAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return objs.removeAll(c);
    }

    @Override
    public void clear() {
        objs.clear();
    }

    @Override
    public long getPipeline() {
        return pipeline;
    }
}
