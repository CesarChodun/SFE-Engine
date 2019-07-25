package game.game_logo;

import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import core.HardwareManager;
import core.rendering.Window;
import core.resources.Asset;
import core.result.VulkanException;

public class GameLogoWindow{
	
	protected static final String GAME_LOGO_WINDOW_ASSET = "LogoWindowLauncher";
	protected static final String WINDOW_CONFIG_FILE = "window.cfg";
	
	protected static final String 
			WINDOW_NAME_KEY = "WindowName",
			WIDTH_KEY = "WIDTH",
			HEIGHT_KEY = "HEIGHT";
	
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
		
		Window window = new Window(HardwareManager.getInstance(), windowcfg.getString(WINDOW_NAME_KEY), 
				windowcfg.getInt(WIDTH_KEY), windowcfg.getInt(HEIGHT_KEY));
		
		return window;
	}
	
	private static void createDefaultWindowCFG(Asset asset) throws IOException {
		asset.newFile(WINDOW_CONFIG_FILE);
		
		JSONObject json = new JSONObject();
		
		json.put(WINDOW_NAME_KEY, "Window name");
		json.put(WIDTH_KEY, 100);
		json.put(HEIGHT_KEY, 100);
		
		FileWriter writer = new FileWriter(asset.get(WINDOW_CONFIG_FILE));
		json.write(writer, 4, 1);
		writer.close();
	}

	public Window getWindow() {
		return window;
	}
	
}
