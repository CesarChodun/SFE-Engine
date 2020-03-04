package rendering.engine.shader;

import core.resources.Destroyable;
import core.result.VulkanException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Vulkan descriptor set.
 *
 * @author Cezary Chodun
 * @since 10.01.2020
 */
public abstract class DescriptorSet implements Destroyable {

    /** A map with the descriptors contained within the set. */
    private Map<String, Descriptor> descriptors =
            Collections.synchronizedMap(new HashMap<String, Descriptor>());

    /**
     * Tells whether all of the descriptors within the set are up to date.
     *
     * @return true if all of the descriptors are up to date and false otherwise.
     */
    public boolean isUpToDate() {
        for (Map.Entry<String, Descriptor> i : descriptors.entrySet()) {
            if (!i.getValue().isUpToDate()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates all of the descriptors within the set.
     *
     * @throws VulkanException
     */
    public void update() throws VulkanException {
        for (Map.Entry<String, Descriptor> i : descriptors.entrySet()) {
            i.getValue().update();
        }
    }

    /**
     * Returns vulkan descriptor set.
     *
     * @return handle of the descriptor set.
     */
    public abstract long getDescriptorSet();

    /**
     * Adds a descriptor to the set.
     *
     * @param name descriptor name.
     * @param descriptor the descriptor.
     * @return the previous descriptor associated with the name or null if there is no such a
     *     descriptor.
     */
    public Descriptor addDescriptor(String name, Descriptor descriptor) {
        return descriptors.put(name, descriptor);
    }

    /**
     * Gets a descriptor with specified name.
     *
     * @param name descriptor name.
     * @return the descriptor.
     */
    public Descriptor get(String name) {
        return descriptors.get(name);
    }

    @Override
    public void destroy() {
        for (Map.Entry<String, Descriptor> i : descriptors.entrySet()) {
            i.getValue().destroy();
        }
    }
}
