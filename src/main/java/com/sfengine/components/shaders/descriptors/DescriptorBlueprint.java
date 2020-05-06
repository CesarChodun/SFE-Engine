package com.sfengine.components.shaders.descriptors;

/**
 * Blueprint for a descriptor.
 *
 * @author Cezary Chodun
 * @since 10.01.2020
 */
public interface DescriptorBlueprint {

    /**
     * Returns all of the values within the descriptor.
     *
     * @return a array of descriptor values.
     */
    public DescriptorValue[] getDescriptorValues();
}
