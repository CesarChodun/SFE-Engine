package com.sfengine.core.context;

import com.sfengine.core.synchronization.Dependable;
import com.sfengine.core.synchronization.Dependency;
import org.lwjgl.vulkan.VkInstance;

import java.util.concurrent.locks.Lock;

/**
 * Provides access to resources that needs to be initialized.
 * And are unique within some specific context.
 *
 * Eg. VkPhysicalDevice(GPU) needs to be initialized and
 * only one object is used for rendering/memory transfer operations.
 */
public interface Context extends Dependable {

    /**
     * Name of the context.
     *
     * @return string representing name of the context.
     */
    String getName();

    /**
     * Name of the context factory.
     * It is guaranteed that the name will be unique.
     *
     * @return the context factory's name.
     */
    String getFactoryIdentifier();
}
