package components.shaders.descriptors;

import core.resources.Destroyable;
import core.result.VulkanException;

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
