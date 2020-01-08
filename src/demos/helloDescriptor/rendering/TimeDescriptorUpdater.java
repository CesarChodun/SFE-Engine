package demos.helloDescriptor.rendering;

import java.util.TimerTask;

import core.result.VulkanException;
import rendering.engine.shader.DescriptorSet;
import rendering.engine.shader.GeneralizedDescriptorValue;

public class TimeDescriptorUpdater extends TimerTask{
	
	private GeneralizedDescriptorValue val;
	Integer timeDescriptorIndex;

	public TimeDescriptorUpdater(DescriptorSet set) {
		val = (GeneralizedDescriptorValue)set.get("TimeData").getValue("miliTime");
	}
	
	@Override
	public void run() {
		int state = ((int) System.currentTimeMillis() / 500) % 10;
		state -= 5;
		if (state < 0)
			state *= -1;
		val.setUniform(0, state);
		try {
				val.update();
		} catch (VulkanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
