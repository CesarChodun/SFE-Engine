package com.sfengine.core.rendering;

import com.sfengine.components.window.CFrame;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RenderingEngineFactory {

    private static final Map<CFrame, RenderingEngine> renderingEngines = Collections.synchronizedMap(new HashMap<>());

    public static RenderingEngine getRenderingEngine(CFrame frame) {
        return renderingEngines.get(frame);
    }

    public static RenderingEngine setRenderingEngine(CFrame frame, RenderingEngine engine) {
        return renderingEngines.put(frame, engine);
    }
}
