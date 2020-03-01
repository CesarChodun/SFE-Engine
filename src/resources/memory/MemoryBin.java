package resources.memory;

import core.resources.Destroyable;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores destroyable objects for destroying later.
 *
 * @author Cezary
 * @since 22.01.2020
 */
public class MemoryBin implements Destroyable {

    // Objects to destroy
    private List<Destroyable> toDestroy = new ArrayList<>();

    /**
     * Adds a destroyable object to the bin.
     *
     * @param <T>
     * @param destroyable
     * @return the destroyable
     */
    public <T extends Destroyable> T add(T destroyable) {
        toDestroy.add(destroyable);
        return destroyable;
    }

    /**
     * Ands destroyable objects to the bin.
     *
     * @param destroyables
     */
    public void add(Destroyable... destroyables) {
        for (Destroyable d : destroyables) {
            toDestroy.add(d);
        }
    }

    @Override
    public void destroy() {
        for (Destroyable d : toDestroy) {
            d.destroy();
        }
        toDestroy.clear();
    }
}
