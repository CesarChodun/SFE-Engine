package game.game_hardware_info;

import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkLayerProperties;

import core.rendering.RenderUtil;
import core.result.VulkanException;
import game.GameStage;

public class HardwareInfoStage implements GameStage{

	@Override
	public void initialize() throws InitializationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void present() throws GameStageException {
		try {
			System.err.println(getAvailableValidationLayers());
			
			System.err.println(getAvailableExtensions());
		} catch (VulkanException e) {
			e.printStackTrace();
			throw new GameStageException(e.getMessage());
		}
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	
	private String getAvailableValidationLayers() throws VulkanException {
		StringBuilder sb  = new StringBuilder();
		
		sb.append("Available vulkan validation layers:\n");
		
		VkLayerProperties[] layers = RenderUtil.listAvailableValidationLayers();
		
		for (int i = 0; i < layers.length; i++)
			sb.append("\t" + layers[i].layerNameString() + "\n");
		
		return sb.toString();
	}
	
	private String getAvailableExtensions() throws VulkanException {
		StringBuilder sb  = new StringBuilder();
		
		sb.append("Available vulkan instance extensions:\n");
		
		VkExtensionProperties[] layers = RenderUtil.listAvailableExtensions();
		
		for (int i = 0; i < layers.length; i++)
			sb.append("\t" + layers[i].extensionNameString() + "\n");
		
		return sb.toString();
	}
}
