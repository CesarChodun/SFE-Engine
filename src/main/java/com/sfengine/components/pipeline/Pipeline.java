package com.sfengine.components.pipeline;

import com.sfengine.core.resources.Destroyable;

/**
 * Generalized pipeline for the engine renderer.
 *
 * @author Cezary Chodun
 * @since 10.01.2020
 */
public interface Pipeline extends Destroyable {

    /**
     * Returns the vulkan pipeline layout.
     *
     * @return handle to the layout.
     */
    public long handle();

    /**
     * Returns the vulkan pipeline bind point.
     *
     * @return flag of the bindpoint.
     */
    public int bindPoint();

    /**
     * Returns the vulkan pipeline layout.
     *
     * @return handle to the layout.
     */
    public long layout();
}
