package game.game_logo;

import static org.lwjgl.glfw.GLFW.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;

import core.Application;
import core.HardwareManager;
import core.resources.Asset;
import core.result.VulkanException;
import game.GameStage;

public class GameLogoStage implements GameStage{
	
	private GameLogoWindow window;
	
	public GameLogoStage() {
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

	@Override
	public void present() throws GameStageException {
		// TODO Auto-generated method stub
		window.show();
		long ID = window.getWindow().getWindowID();
		
		for (int i = 0; i < 1000; i++) {
			if (glfwWindowShouldClose(ID))
				break;
			
			glfwPollEvents();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		window.destroy();
	}

}
