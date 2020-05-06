package com.sfengine.components.geometry;

import com.sfengine.core.resources.Destroyable;

/**
 * Interface representing a mesh that can be rendered.
 *
 * @author Cezary Chodun
 * @since 28.10.2019
 */
public interface Mesh extends Destroyable {

    /** @return Vulkan handle for the vertices buffer. */
    public long getVerticesHandle();

    /** @return Stride for the vertices buffer. */
    public int getStride();

    /** @return Number of the vertices in the mesh. */
    public int verticesCount();
}
