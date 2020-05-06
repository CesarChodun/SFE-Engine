package demos.helloDescriptor.rendering;

import com.sfengine.components.shaders.GeneralizedDescriptorValue;
import com.sfengine.components.shaders.descriptor_sets.DescriptorSet;
import java.util.TimerTask;

/**
 * Updates the time descriptor.
 *
 * @author Cezary Chodun
 * @since 10.01.2020
 */
public class TimeDescriptorUpdater extends TimerTask {

    private GeneralizedDescriptorValue val;
    Integer timeDescriptorIndex;

    /** @param set the descriptor to be updated. */
    public TimeDescriptorUpdater(DescriptorSet set) {
        val = (GeneralizedDescriptorValue) set.get("TimeData").getValue("miliTime");
    }

    @Override
    public void run() {
        int state = ((int) (System.currentTimeMillis() / 250)) % 20;
        state -= 10;
        if (state < 0) {
            state *= -1;
        }

        val.setUniform(0, state);
        val.update();
    }
}
