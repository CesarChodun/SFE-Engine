package demos.helloCube.rendering;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Stores information about the model's
 * position, rotation and scale.
 * 
 * @author Cezary Chodun
 * @since 12.03.2020
 */
public class ModelTransform3D implements Transform4f {
    
    private final Vector3f scale = new Vector3f(1f, 1f, 1f);
    private final Vector3f position = new Vector3f(0f, 0f, 0f);
    private final Quaternionf rotation = new Quaternionf();
    
    /**
     * Creates a basic model transform.
     */
    public ModelTransform3D() {}

    @Override
    public Matrix4f getTransformation() {
        Matrix4f rotMat = new Matrix4f().rotation(rotation);
        Matrix4f posMat = new Matrix4f().translation(position);
        Matrix4f scaleMat = new Matrix4f().scale(scale);
        
        return new Matrix4f(scaleMat).mul(posMat).mul(rotMat);
    }

    public Vector3f getScale() {
        return scale;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Quaternionf getRotation() {
        return rotation;
    }
}
