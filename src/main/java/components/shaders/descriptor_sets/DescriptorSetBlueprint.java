package main.java.components.shaders.descriptor_sets;

/**
 * Blueprint for vulkna descriptor set.
 *
 * @author Cezary
 * @since 10.01.2020
 */
public interface DescriptorSetBlueprint {

    /**
     * Returns a descriptor set layout.
     *
     * @return handle to the layout.
     */
    public long getLayout();

    /**
     * Returns descriptor count.
     *
     * @return descriptor count.
     */
    public int descriptorCount();
}
