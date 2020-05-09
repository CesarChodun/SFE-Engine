package com.sfengine.core.context;

import org.lwjgl.vulkan.VkInstance;

import java.util.concurrent.locks.Lock;

public interface Context {

    String getName();

    VkInstance getInstance();

    Lock getLock();
}
