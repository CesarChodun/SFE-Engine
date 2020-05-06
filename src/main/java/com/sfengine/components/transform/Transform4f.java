package com.sfengine.components.transform;

import org.joml.Matrix4f;

/**
 * Represents some transformation that can be stored in a 4x4 matrix(float).
 *
 * @author Cezary Chodun
 * @since 12.03.2020
 */
public interface Transform4f {

    /**
     * Returns the transformation in a Matrix4f representation.
     *
     * @return The transformation.
     */
    public Matrix4f getTransformation();
}
