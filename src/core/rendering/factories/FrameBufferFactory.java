package core.rendering.factories;

/**
 * Class for creating framebuffers.
 *
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public interface FrameBufferFactory {

    /**
     * Creates a list of frame buffers for given image views.
     *
     * @param width width of the frame buffer.
     * @param height height of the frame buffer.
     * @param imageViews A list of image views for frame buffer creation.
     * @return A list of frame buffers.
     */
    public long[] createFramebuffers(int width, int height, long... imageViews);

    /**
     * Destroys given frame buffers. It is guaranteed that the frame buffers to destroy were created
     * using this factory.
     *
     * @param framebuffers a list of frame buffers handles
     */
    public void destroyFramebuffers(long... framebuffers);
}
