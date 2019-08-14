package core.rendering.factories;

public interface FrameBufferFactory {

	/**
	 * Creates a list of frame buffers
	 * for given image views.
	 * 
	 * @param images			A list of image views 
	 * 							for frame buffer creation.
	 * @return	A list of frame buffers.
	 */
	public long[] createFrameBuffers(long[] images);
	
	/**
	 * Destroys given frame buffers.
	 * It is guaranteed that the frame buffers
	 * to destroy were created using this factory.
	 * 
	 * @param farmeBuffers		a list of frame buffers handles
	 */
	public void destroyFramebuffers(long[] farmeBuffers);
	
}
