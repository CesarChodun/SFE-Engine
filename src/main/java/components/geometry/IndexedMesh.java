package main.java.components.geometry;

public interface IndexedMesh extends Mesh {

    public long getIndicesHandle();

    public int getIndicesStride();

    public int indicesCount();
}
