package com.sfengine.core;

import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memUTF8;

import java.nio.ByteBuffer;
import java.util.Collection;
import org.lwjgl.PointerBuffer;

/**
 * Class with general use functions, that doesn't fit in other category within core package. Or may
 * be used for various of purposes, so it is hard to classify them.
 *
 * @author Cezary Chodun
 * @since 24.09.2019
 */
public class Util {

    /**
     * Converts a list of strings to an array of byte buffers.
     *
     * @param data Collection of strings to be converted.
     * @return Array of byte buffers.
     */
    public static ByteBuffer[] makeByteBuffers(Collection<String> data) {
        ByteBuffer[] out = new ByteBuffer[data.size()];

        int i = 0;
        for (String s : data) {
            out[i++] = memUTF8(s);
        }

        return out;
    }

    /*
     * Converts string(s) to an array of
     * byte buffers.
     *
     * @param data        String(s) to be converted.
     * @return        Array of byte buffers.
     */
    public static ByteBuffer[] makeByteBuffers(String... data) {
        ByteBuffer[] out = new ByteBuffer[data.length];

        for (int i = 0; i < data.length; i++) {
            out[i] = memUTF8(data[i]);
        }

        return out;
    }

    /**
     * Creates a pointer buffer from given buffers.
     *
     * @param buffers - Buffers to be converted into pointer buffer.
     * @return - Pointer buffer that points to given buffers.
     */
    public static PointerBuffer makePointer(ByteBuffer... buffers) {
        PointerBuffer pb = memAllocPointer(buffers.length);

        for (ByteBuffer b : buffers) {
            pb.put(b);
        }

        pb.flip();
        return pb;
    }
}
