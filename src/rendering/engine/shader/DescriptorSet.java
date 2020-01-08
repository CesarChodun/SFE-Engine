package rendering.engine.shader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import core.resources.Destroyable;
import core.result.VulkanException;

public abstract class DescriptorSet implements Destroyable{

	private Map<String, Descriptor> descriptors = Collections.synchronizedMap(new HashMap<String, Descriptor>());
	
	public boolean isUpToDate() {
		for (Map.Entry<String, Descriptor> i : descriptors.entrySet())
			if (!i.getValue().isUpToDate())
				return false;
		return true;
	}
	
	public void update() throws VulkanException {
		for (Map.Entry<String, Descriptor> i : descriptors.entrySet())
			i.getValue().update();
	}

	public abstract long getDescriptorSet();
	
	public Descriptor addDescriptor(String name, Descriptor descriptor) {
		return descriptors.put(name, descriptor);
	}
	
	public Descriptor get(String name) {
		return descriptors.get(name);
	}
	
	@Override
	public void destroy() {
		for (Map.Entry<String, Descriptor> i : descriptors.entrySet())
			i.getValue().destroy();
	}
}
