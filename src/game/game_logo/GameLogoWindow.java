package game.game_logo;

import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import core.HardwareManager;
import core.hardware.Monitor;
import core.rendering.Window;
import core.resources.Asset;
import core.resources.Dimension;
import core.result.VulkanException;

public class GameLogoWindow{
	
	protected static final String GAME_LOGO_WINDOW_ASSET = "LogoWindowLauncher";
	protected static final String WINDOW_CONFIG_FILE = "window.cfg";
	
	protected static final String 
			WINDOW_NAME_KEY = "WindowName",
			WIDTH_KEY = "WIDTH",
			HEIGHT_KEY = "HEIGHT",
			POSX_KEY = "POSX",
			POSY_KEY = "POSY";
	
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
		
		if (!config.exists(WINDOW_CONFIG_FILE))
			createDefaultWindowCFG(config);
		
		JSONObject windowcfg = config.getJSON(WINDOW_CONFIG_FILE);
		
		Monitor monitor = HardwareManager.getPrimaryMonitor();
		int width = Dimension.toPx(windowcfg.getString(WIDTH_KEY), monitor.getWidth());
		int height = Dimension.toPx(windowcfg.getString(HEIGHT_KEY), monitor.getHeight());
		int x = Dimension.toPx(windowcfg.getString(POSX_KEY), monitor.getWidth());
		int y = Dimension.toPx(windowcfg.getString(POSY_KEY), monitor.getHeight());
		
		Window window = new Window(HardwareManager.getInstance(), windowcfg.getString(WINDOW_NAME_KEY), x, y, 
				width, height);
		
		return window;
	}
	
	private static void createDefaultWindowCFG(Asset asset) throws IOException {
		asset.newFile(WINDOW_CONFIG_FILE);
		
		JSONObject json = new JSONObject();
		
		json.put(WINDOW_NAME_KEY, "Window name");
		json.put(WIDTH_KEY, "200");
		json.put(HEIGHT_KEY, "200");
		json.put(POSX_KEY, "200");
		json.put(POSY_KEY, "200");
		
		FileWriter writer = new FileWriter(asset.get(WINDOW_CONFIG_FILE));
		json.write(writer, 4, 1);
		writer.close();
	}

	public Window getWindow() {
		return window;
	}
	
}
