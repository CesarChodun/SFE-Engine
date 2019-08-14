package game.game_logo;

import static org.lwjgl.glfw.GLFW.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueue;

import core.Application;
import core.Engine;
import core.EngineTask;
import core.HardwareManager;
import core.rendering.Renderer;
import core.rendering.Window;
import core.rendering.factories.CommandBufferFactory;
import core.rendering.factories.SwapchainFactory;
import core.rendering.recording.Recordable;
import core.resources.Asset;
import core.result.VulkanException;
import game.GameStage;
import game.factories.BasicCommandBufferFactory;
import game.factories.BasicSwapchainFactory;
import game.rendering.GLFWTask;
import game.rendering.WindowTask;
import game.rendering.WindowTask.WindowCloseCallback;

public class GameLogoStage implements GameStage{
	
	private GameLogoWindow window;
	private Engine engine;
	
	public GameLogoStage(Engine engine) {
		this.engine = engine;
	}
	
	public static void initializeHardware() throws VulkanException, IOException {
		
		// Game data initialization(in the current folder)
		Application.init(new File(""));
		
		//Initialize hardware
		HardwareManager.init(Application.getApplicationInfo(), Application.getConfigAssets());
		
		
	}

	@Override
	public void initialize() throws InitializationException {
		// TODO Auto-generated method stub
		
		try {
			initializeHardware();
		} catch (Exception e) {
			e.printStackTrace();
			throw new InitializationException("Failed to initialize hardware!");
		}
		
		try {
			window = new GameLogoWindow(Application.getConfigAssets());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (VulkanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class WindowShutDown implements EngineTask {

		private Window window;
		private EngineTask windowTask;
		
		public WindowShutDown(Window window, WindowTask windowTask) {
			this.window = window;
			this.windowTask = windowTask;
		}
		
		@Override
		public void run() throws AssertionError {
			engine.removeTickTask(windowTask);
			window.setVisible(false);
			window.destroyWindow();
		}
		
	}
	
	@Override
	public void present() throws GameStageException {
		// TODO Auto-generated method stub
		
		VkPhysicalDevice physicalDevice = null;//TODO
		VkDevice device = null; //TODO
		VkQueue queue = null;//TODO
		
		
		Recordable cmdRecord = null;//TODO
		long renderPass;//TODO
		int renderQueueFamilyIndex = HardwareManager.getMostSuitableQueueFamily(physicalDevice);//TODO
		int bufferFlags;//TODO
		
		CommandBufferFactory basicCMD = new CommandBufferFactory(device, cmdRecord, renderPass, renderQueueFamilyIndex, bufferFlags, null);
		Renderer winRenderer = new Renderer(window, device, queue, basicCMD, new BasicSwapchainFactory());
		
		window.show();
		long ID = window.getWindow().getWindowID();
		
		//GLFW task
		GLFWTask task = new GLFWTask();
		engine.addTickTask(task);
		
		//Window task
		WindowTask windowTask = new WindowTask(ID);
		windowTask.setCloseCall(new WindowCloseCallback() {

			@Override
			public void close(long windowID) {
				WindowShutDown sdOperation = new WindowShutDown(window.getWindow(), windowTask);
				engine.addTask(sdOperation);
			}
			
		});
		engine.addTickTask(windowTask);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
//		window.destroy();
	}

}
