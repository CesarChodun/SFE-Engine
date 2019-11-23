package demos.util;

import core.EngineTask;
import core.rendering.Renderer;
import core.result.VulkanException;

public class RenderingTask implements EngineTask{
	
	private Renderer ren;
	private FPSCounter counter = new FPSCounter(10);
	
	public RenderingTask(Renderer renderer) {
		this.ren = renderer;
	}

	private int fps_tick = 0;
	
	@Override
	public void run() throws AssertionError {
		try {
			boolean res = ren.acquireNextImage();
			//TODO
			res = ren.submitToQueue();
			//TODO
			res = ren.presentKHR();
			if (res ==  true) {
				counter.newFrame();
				fps_tick++;
				if (fps_tick >= 100) {
					fps_tick = 0;
					System.out.println(counter.avrFPS);
				}
			}
		} catch (VulkanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
}
