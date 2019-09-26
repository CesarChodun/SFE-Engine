package game.game_logo;



import java.io.IOException;

import org.json.JSONException;

import core.HardwareManager;
import core.hardware.Monitor;
import core.rendering.Window;
import core.resources.Asset;
import core.resources.ConfigFile;
import core.resources.ResourceUtil;
import core.result.VulkanException;

public class GameLogoWindow {
	
	protected static final String GAME_LOGO_WINDOW_ASSET = "LogoWindowLauncher";
	protected static final String WINDOW_CONFIG_FILE = "window.cfg";
	
	protected static final String 
			WINDOW_NAME_KEY = "WINDOW_NAME",
			WIDTH_KEY = "WIDTH",
			HEIGHT_KEY = "HEIGHT",
			POSX_KEY = "POSX",
			POSY_KEY = "POSY",
			FULLSCREEN_KEY = "FULSCREEN";
	
	protected static final String 
			DEFAULT_WINDOW_NAME = "window",
			DEFAULT_WIDTH = "50%",
			DEFAULT_HEIGHT = "50%",
			DEFAULT_POSX = "0",
			DEFAULT_POSY = "0";
	protected static final Boolean
			DEFAULT_FULLSCREEN = false;
	
	protected Asset config;
	private Window window;

	public GameLogoWindow(Asset asset) throws JSONException, VulkanException, IOException {
		
		config = asset.getSubAsset(GAME_LOGO_WINDOW_ASSET);
		window = createLogoWindow(config);
		window.setVisible(false);
	}
	
	public void show() {
		window.setVisible(true);
	}
	
	public void destroy() {
		window.setVisible(false);
		window.destroyWindow();
	}
	
	private static Window createLogoWindow(Asset config) throws JSONException, VulkanException, IOException {
		ConfigFile windowcfg = config.getConfigFile(WINDOW_CONFIG_FILE);
		
		Monitor monitor = HardwareManager.getPrimaryMonitor();
		int width = ResourceUtil.toPx(windowcfg.getString(WIDTH_KEY, DEFAULT_WIDTH), monitor.getWidth());
		int height = ResourceUtil.toPx(windowcfg.getString(HEIGHT_KEY, DEFAULT_HEIGHT), monitor.getHeight());
		int x = ResourceUtil.toPx(windowcfg.getString(POSX_KEY, DEFAULT_POSX), monitor.getWidth());
		int y = ResourceUtil.toPx(windowcfg.getString(POSY_KEY, DEFAULT_POSY), monitor.getHeight());
		boolean fullscreen = windowcfg.getBoolean(FULLSCREEN_KEY, DEFAULT_FULLSCREEN);
		
		
		Window window = new Window(HardwareManager.getInstance(), windowcfg.getString(WINDOW_NAME_KEY, DEFAULT_WINDOW_NAME), x, y, 
				width, height, fullscreen);
		
		windowcfg.close();
		
		return window;
	}

	public Window getWindow() {
		return window;
	}
	
}
