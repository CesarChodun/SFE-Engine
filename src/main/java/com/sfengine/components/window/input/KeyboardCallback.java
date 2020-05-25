package com.sfengine.components.window.input;

public interface KeyboardCallback {

    public void invoke(long window, int scancode, int action, int mods);
    
    public int getKey();
    
}
