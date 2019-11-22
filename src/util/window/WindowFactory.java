package util.window;

import core.HardwareManager;
import core.hardware.Monitor;
import core.rendering.Window;
import core.resources.ConfigFile;
import core.resources.ResourceUtil;
import core.result.VulkanException;

public class WindowFactory {
	
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

	public static void loadFromFile(Window window, ConfigFile config) throws VulkanException {		
		Monitor monitor = HardwareManager.getPrimaryMonitor();
		int width = ResourceUtil.toPx(config.getString(WIDTH_KEY, DEFAULT_WIDTH), monitor.getWidth());
		int height = ResourceUtil.toPx(config.getString(HEIGHT_KEY, DEFAULT_HEIGHT), monitor.getHeight());
		int x = ResourceUtil.toPx(config.getString(POSX_KEY, DEFAULT_POSX), monitor.getWidth());
		int y = ResourceUtil.toPx(config.getString(POSY_KEY, DEFAULT_POSY), monitor.getHeight());
		boolean fullscreen = config.getBoolean(FULLSCREEN_KEY, DEFAULT_FULLSCREEN);
		
		window.setBounds(x, y, width, height);
		window.setName(config.getString(WINDOW_NAME_KEY, DEFAULT_WINDOW_NAME));
		window.setFullScreen(fullscreen);
		
		config.close();
	}
	
}
