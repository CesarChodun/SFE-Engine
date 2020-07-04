package com.sfengine.components.rendering;

import com.sfengine.components.window.CFrame;
import com.sfengine.core.rendering.PipelineContainer;
import com.sfengine.core.rendering.RenderObject;
import com.sfengine.core.rendering.RenderingEngine;
import com.sfengine.core.rendering.RenderingEngineFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BasicRenderingEngine implements RenderingEngine {

    private CFrame frame;

    private final Map<Long, PipelineContainer> pipelines = Collections.synchronizedMap(new HashMap<>());

    public BasicRenderingEngine(CFrame frame) {
        this.frame = frame;
    }

    private void initialize() {
        RenderingEngineFactory.setRenderingEngine(frame, this);
    }

    @Override
    public void add(RenderObject obj) {
        if (pipelines.containsKey(obj.getPipeline()))
            pipelines.get(obj.getPipeline()).add(obj);
        else {
            pipelines.put(obj.getPipeline(), new PipelineContainer(obj.getPipeline()));
        }
    }

    @Override
    public boolean remove(RenderObject obj) {
        if (!pipelines.containsKey(obj.getPipeline()))
            return false;

        return pipelines.get(obj.getPipeline()).remove(obj);
    }

    @Override
    public void requestUpdate() {
        throw new NotImplementedException();
    }

    @Override
    public void forceUpdate() {
        throw new NotImplementedException();
    }

    @Override
    public CFrame getCFrame() {
        return frame;
    }

    @Override
    public void destroy() {
        throw new NotImplementedException();
    }
}
