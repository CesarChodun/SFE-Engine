package components.transform;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Stores basic information about the position, rotation, and projection of the camera.
 *
 * @author Cezary Chodun
 * @since 12.03.2020
 */
public class CameraTransform implements Transform4f {

    private Matrix4f projection;
    private Vector3f position;
    private Quaternionf rotation;

    /**
     * Creates a basic camera transform with the perspective projection. The camera is located at
     * the point (0, 0, 0). And it is looking along (0, 0, 1) with the up value equal to (0, 1, 0).
     *
     * @param width Render area width.
     * @param height Render area height.
     */
    public CameraTransform(int width, int height) {
        this.projection =
                new Matrix4f()
                        .perspective(
                                (float) Math.toRadians(70), (float) width / height, 0.01f, 1000.0f);
        this.position = new Vector3f(0.0f, 0.0f, 0.0f);
        this.rotation =
                new Quaternionf()
                        .lookAlong(new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 1.0f, 0.0f));
    }

    @Override
    public Matrix4f getTransformation() {
        Matrix4f posMat = new Matrix4f().translation(new Vector3f(position).mul(-1));
        Matrix4f rotMat = new Matrix4f().rotation(new Quaternionf(rotation).conjugate());

        return new Matrix4f(projection).mul(rotMat).mul(posMat);
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quaternionf getRotation() {
        return rotation;
    }
}
