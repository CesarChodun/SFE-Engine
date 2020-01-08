package rendering.engine.shader;

import core.resources.Destroyable;
import core.result.VulkanException;

public interface DescriptorValue extends Destroyable{
	
	public String name();
	
	public boolean isUpToDate();
	
	public void update() throws VulkanException;
}
