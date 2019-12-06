package rendering.engine.shader;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;
import static rendering.engine.shader.UniformUsage.*;
import java.nio.ByteBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import core.resources.Destroyable;
import core.result.VulkanException;
import rendering.memory.GPUBuffer;
import static rendering.memory.RenderingMemoryUtil.*;
import rendering.pipeline.Pipeline;

public class Descriptor implements Destroyable{
	
	/**
	 * TODO: Uniform usage should be obtained from shader class;
	 */
	protected UniformUsage[] slots;
	private int[] prefTab;
	private int dataSize;
	protected long descriptorSet;
	protected ByteBuffer data;
	private VkWriteDescriptorSet.Buffer write;
	
	private VkDevice device;
	private VkDescriptorBufferInfo.Buffer bufferInfo;
	private GPUBuffer gpuBuffer = null;
	
	private boolean upToDate = false;
	
	public Descriptor(VkPhysicalDevice physicalDevice, VkDevice device, Pipeline pipeline, long descriptorSet, int binding, UniformUsage...uniformUsages) {
		this.device = device;
		this.slots = uniformUsages;
		this.descriptorSet = descriptorSet;
		
		dataSize = 0;
		prefTab = new int[slots.length];
		prefTab[0] = 0;
		for(int i = 0; i < slots.length; i++) {
			dataSize += slots[i].sizeOf();
			if(i < slots.length-1)
				prefTab[i+1] = prefTab[i]+slots[i].sizeOf();
		}
		
		data = memAlloc(dataSize);

		try {
			gpuBuffer = allocateBufferMemoryGPU(device, physicalDevice, dataSize, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_SHARING_MODE_EXCLUSIVE);
		} catch (VulkanException e) {
			throw new AssertionError("Failed to allocate buffer memory!");
		}	
		
		bufferInfo = VkDescriptorBufferInfo.calloc(slots.length);
		for(int i = 0; i < slots.length; i++) {
			bufferInfo.get(i)
			.buffer(gpuBuffer.buffer)
			.offset(prefTab[i])
			.range(slots[i].sizeOf());
		}
		
		write = VkWriteDescriptorSet.calloc(1)
				.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
				.pNext(NULL)
				.dstSet(descriptorSet)
				.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
				.pBufferInfo(bufferInfo)
				.dstArrayElement(0)
				.dstBinding(binding);
	}
	
	public UniformUsage getDescriptorSlot(int index) {
		if(index >= slots.length)
			throw new Error("Array out of bounds: array size = " + slots.length + " quered index = " + index);
		return slots[index];
	}
	
	/**
	 * Can be run on multiple threads if using
	 * suitable descriptors.
	 * 
	 * @throws VulkanException
	 */
	public void update() throws VulkanException {
		if (upToDate == true)
			return;
		upToDate = true;
		
		bindBufferMemoryGPU(device, gpuBuffer, data);
		vkUpdateDescriptorSets(device, write, null);
	}
	
	public void setUniform(int index, short val) {
		checkCompatibility(index, UNIFORM_USAGE_INT_16);
		
		data.putShort(prefTab[index], val);
	}
	public void setUniform(int index, int val) {
		checkCompatibility(index, UNIFORM_USAGE_INT_32);
		
		data.putInt(prefTab[index], val);
	}
	public void setUniform(int index, long val) {
		checkCompatibility(index, UNIFORM_USAGE_INT_64);
		
		data.putLong(prefTab[index], val);
	}
	public void setUniform(int index, float val) {
		checkCompatibility(index, UNIFORM_USAGE_FLOAT);
		
		data.putFloat(prefTab[index], val);
	}
	public void setUniform(int index, double val) {
		checkCompatibility(index, UNIFORM_USAGE_DOUBLE);
		
		data.putDouble(prefTab[index], val);
	}
	public void setUniform(int index, Vector3f val) {
		checkCompatibility(index, UNIFORM_USAGE_VECTOR_3F);
		
		val.get(prefTab[index], data);
	}
	public void setUniform(int index, Vector4f val) {
		checkCompatibility(index, UNIFORM_USAGE_VECTOR_3F);

		val.get(prefTab[index], data);
	}
	public void setUniform(int index, Matrix4f val) {
		checkCompatibility(index, UNIFORM_USAGE_MATRIX_4F);
		
		val.get(prefTab[index], data);
	}
	
	private void checkCompatibility(int index, UniformUsage usage) {
		if(slots[index].dataType().compareTo(usage) != 0)
			throw new Error("Uniform usage mismatch! Expected: " + slots[index].toString() + " Recived: " + usage.name());
		
		upToDate = false;
	}
	
	public long getDescriptorSet() {
		return descriptorSet;
	}
	
	public boolean isUpToDate() {
		return upToDate;
	}

	@Override
	public void destroy() {
		write.free();
		bufferInfo.free();
	}
}
