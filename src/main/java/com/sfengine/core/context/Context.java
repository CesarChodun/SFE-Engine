package com.sfengine.core.context;

import com.sfengine.core.synchronization.Dependable;
import com.sfengine.core.synchronization.Dependency;
import org.lwjgl.vulkan.VkInstance;

import java.util.concurrent.locks.Lock;

public interface Context extends Dependable {

    String getName();

    String getFactoryIdentifier();

    Lock getLock();
}
