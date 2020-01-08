package rendering.engine.shader;

public interface DescriptorSetBlueprint {

	/**
	 * Returns a descriptor set layout.
	 * 
	 * @return
	 */
	public long getLayout();
	
	public int descriptorCount();
	
}
