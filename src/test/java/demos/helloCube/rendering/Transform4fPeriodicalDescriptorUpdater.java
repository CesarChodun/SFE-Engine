package test.java.demos.helloCube.rendering;

import main.java.components.shaders.GeneralizedDescriptorValue;
import main.java.components.shaders.descriptor_sets.DescriptorSet;
import main.java.components.transform.CameraTransform;
import main.java.components.transform.ModelTransform3D;
import java.util.TimerTask;
import org.joml.Vector3f;

/**
 * Updates the transformation descriptors.
 *
 * @author Cezary Chodun
 * @since 12.03.2020
 */
public class Transform4fPeriodicalDescriptorUpdater extends TimerTask {

    private GeneralizedDescriptorValue modelDescriptor, cameraDescriptor;
    Integer timeDescriptorIndex;

    private ModelTransform3D transform;
    private CameraTransform camera;

    private long mili = -1;

    /** @param set the descriptor to be updated. */
    public Transform4fPeriodicalDescriptorUpdater(
            DescriptorSet set, ModelTransform3D transform, CameraTransform camera, String target) {
        modelDescriptor = (GeneralizedDescriptorValue) set.get("Model").getValue(target);
        cameraDescriptor = (GeneralizedDescriptorValue) set.get("Camera").getValue(target);
        this.transform = transform;
        this.camera = camera;
    }

    @Override
    public void run() {
        // Rotates the cube based on the time that passed.
        if (mili == -1) {
            mili = System.currentTimeMillis();
        }

        long newTime = System.currentTimeMillis();
        long delta = newTime - mili;

        transform.getRotation().rotateAxis(0.006f * delta / 10, new Vector3f(1, 1, 0));
        mili = newTime;

        // Updates the descriptors.
        modelDescriptor.setUniform(0, transform.getTransformation());
        modelDescriptor.update();
        cameraDescriptor.setUniform(0, camera.getTransformation());
        cameraDescriptor.update();
    }
}
