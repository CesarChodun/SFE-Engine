package game.game_logo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import core.Application;
import core.HardwareManager;
import core.result.VulkanException;
import game.GameStage;

public class GameLogoStage implements GameStage{
	
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
	}

	@Override
	public void present() throws GameStageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

}
