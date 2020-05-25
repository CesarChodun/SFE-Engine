package com.sfengine.components.util;

import com.sfengine.core.engine.EngineTask;
import com.sfengine.core.rendering.Renderer;
import com.sfengine.core.result.VulkanException;

public class RenderingTask implements EngineTask {

    private Renderer ren;
    private FPSCounter counter = new FPSCounter(10);

    public RenderingTask(Renderer renderer) {
        this.ren = renderer;
    }

    private int fpsTick = 0;

    @Override
    public void run() throws AssertionError {
        try {
            boolean res = ren.acquireNextImage();
            // TODO
            res = ren.submitToQueue();
            // TODO
            res = ren.presentKHR();
            if (res == true) {
                counter.newFrame();
                fpsTick++;
                if (fpsTick >= 100) {
                    fpsTick = 0;
                    System.out.println(counter.avrFPS);
                }
            }
        } catch (VulkanException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
