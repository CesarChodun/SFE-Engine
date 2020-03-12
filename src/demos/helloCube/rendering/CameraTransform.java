package demos.helloCube.rendering;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class CameraTransform implements Transform4f {
    
    private Matrix4f projection;
    public Vector3f position;
    public Quaternionf rotation;
    
    
    public CameraTransform(int width, int height) {
        this.projection = new Matrix4f();
        this.projection = this.projection.perspective((float)Math.toRadians(70), (float)width / height, 0.01f, 1000.0f);
        this.position = new Vector3f(0.0f, 0.0f, 0.0f);
        this.rotation = new Quaternionf().lookAlong(new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 1.0f, 0.0f));
    }

    @Override
    public Matrix4f getTransformation() {
        Matrix4f posMat = new Matrix4f().translation(new Vector3f(position).mul(-1));
        Matrix4f rotMat = new Matrix4f().rotation(new Quaternionf(rotation).conjugate());
        
        return new Matrix4f(projection).mul(rotMat).mul(posMat);
    }

}
