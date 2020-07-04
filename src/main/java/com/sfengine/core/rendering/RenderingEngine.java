package com.sfengine.core.rendering;

import com.sfengine.components.rendering.RenderPass;
import com.sfengine.components.window.CFrame;
import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.synchronization.Dependable;

public interface RenderingEngine extends Destroyable, Dependable {

    void add(RenderObject obj);

    boolean remove(RenderObject obj);

    void requestUpdate();

    void forceUpdate();

    void addUpdate(Updatable upd);

    void removeUpdate(Updatable upd);

    RenderPass getRenderPass();

    CFrame getCFrame();
}
