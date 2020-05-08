package com.sfengine.components.geometry.indexed;

import com.sfengine.components.geometry.Mesh;

public interface IndexedMesh extends Mesh {

    public long getIndicesHandle();

    public int getIndicesStride();

    public int indicesCount();
}
