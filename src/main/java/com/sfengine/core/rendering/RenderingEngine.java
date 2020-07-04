package com.sfengine.core.rendering;

import com.sfengine.components.window.CFrame;
import com.sfengine.core.resources.Destroyable;

public interface RenderingEngine extends Destroyable {

    void add(RenderObject obj);

    boolean remove(RenderObject obj);

    void requestUpdate();

    void forceUpdate();

    CFrame getCFrame();
}
