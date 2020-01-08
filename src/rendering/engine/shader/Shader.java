package rendering.engine.shader;

import java.util.HashMap;
import java.util.Map;

import core.result.VulkanException;

public class Shader extends HashMap<String, DescriptorSet>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean isUpToDate() {
		for (Map.Entry<String, DescriptorSet> i : entrySet())
			if (!i.getValue().isUpToDate())
				return false;
		return true;
	}
	
	public void upDate() throws VulkanException {
		for (Map.Entry<String, DescriptorSet> i : entrySet())
			i.getValue().update();
	}	
	
}
