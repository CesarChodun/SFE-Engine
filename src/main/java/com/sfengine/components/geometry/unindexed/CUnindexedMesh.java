package com.sfengine.components.geometry.unindexed;

import com.sfengine.components.geometry.Util;
import com.sfengine.components.geometry.indexed.IndexedMesh;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

public abstract class CUnindexedMesh<T> implements UnindexedMesh {

    /** Number of vertices in the buffer. */
    private volatile int verticesCount;
    /** Length of a vertex description(in bytes). */
    private volatile int stride;
    /** Vertices buffer handle. */
    private long verticesBuffer;

    public CUnindexedMesh(T... vertices) {
        makeBuffer(vertices);
    }

    public CUnindexedMesh(List<T> vertices) {
        makeBuffer((T[]) vertices.toArray());
    }

    private void makeBuffer(T... vertices) {
        verticesCount = vertices.length;
        stride = getStride();


        ByteBuffer vertexBuffer = memAlloc(verticesCount * stride);
        fillBuffer(vertexBuffer);
        vertexBuffer.flip();

        // Creates a Vulkan buffer with vertices data.
//        verticesBuffer = Util.createVerticesBuffer(physicalDevice, device, vertexBuffer);

        memFree(vertexBuffer);
    }

    public abstract void fillBuffer(ByteBuffer buffer);

    @Override
    public long getVerticesHandle() {
        return 0;
    }

    @Override
    public int verticesCount() {
        return verticesCount;
    }

    @Override
    public void destroy() {

    }
}
