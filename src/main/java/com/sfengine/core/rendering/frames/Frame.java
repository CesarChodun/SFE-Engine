package com.sfengine.core.rendering.frames;

import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.synchronization.Dependable;

public interface Frame extends Dependable, Destroyable {

    enum State {
        ACQUIRED, SUBMITTED, WAITING;
    }

    void render(long swapchain, long... ImageAcquireSemaphore);

    long[] getRenderCompleteSemaphores();

    State getState();
}
