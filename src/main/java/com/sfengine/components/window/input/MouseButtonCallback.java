package com.sfengine.components.window.input;

public interface MouseButtonCallback {

    public void invoke(long window, int button, int action, int mods);
    
}
