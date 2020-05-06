package com.sfengine.components.shaders.descriptors;

import com.sfengine.core.resources.Destroyable;
import com.sfengine.core.result.VulkanException;

public interface DescriptorValue extends Destroyable {

    /**
     * Name of the descriptor value.
     *
     * @return the name.
     */
    public String name();

    /** @return true if the value is up to date and false otherwise. */
    public boolean isUpToDate();

    /**
     * Updates the descriptor value.
     *
     * @throws VulkanException
     */
    public void update() throws VulkanException;
}
