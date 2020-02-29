package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import org.json.JSONException;
import org.lwjgl.vulkan.VkApplicationInfo;

import core.resources.Asset;
import core.resources.ConfigFile;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * 
 * Class providing basic information
 * about the application and engine.
 * 
 * @author Cezary Chodun
 * @since 19.11.2019
 */
public class Application {

	/** Tells whether the application is run in release mode. */
	public static final boolean RELEASE = Boolean.valueOf(System.getProperty("application_release", "true"));
	/** Application debug mode(provides extra logging information). */
	public static final boolean DEBUG = Boolean.valueOf(System.getProperty("application_debug", "false"));
	/** Location of the configuration file. */
	public static final String CONFIG_FOLDER_NAME = "config"; 
	/** Location of the file containing basic information about an application. */
	public static final String APPLICATION_INFO_FILE = "appInfo.cfg";
	
	/** The configuration property names. */
	protected static final String 
		ENGINE_NAME_KEY = "ENGINE_NAME", 
		APPLICATION_NAME_KEY = "APPLICATION_NAME",
		API_MAJOR_KEY = "API_VERSION_MAJOR", 
		API_MINOR_KEY = "API_VERSION_MINOR", 
		API_PATCH_KEY = "API_VERSION_PATCH",
		ENGINE_MAJOR_KEY = "ENGINE_VERSION_MAJOR", 
		ENGINE_MINOR_KEY = "ENGINE_VERSION_MINOR", 
		ENGINE_PATCH_KEY = "ENGINE_VERSION_PATCH",
		APP_MAJOR_KEY = "APPLICATION_VERSION_MAJOR", 
		APP_MINOR_KEY = "APPLICATION_VERSION_MINOR", 
		APP_PATCH_KEY = "APPLICATION_VERSION_PATCH",
		DEFAULT_ENGINE_NAME = "Engine",
		DEFAULT_APPLICATION_NAME = "Application";
	
	/** Default configuration values.*/
	protected static final Integer
		DEFAULT_API_MAJOR = 1,
		DEFAULT_API_MINOR = 0,
		DEFAULT_API_PATCH = 1,
		DEFAULT_ENGINE_MAJOR = 0, 
		DEFAULT_ENGINE_MINOR = 1, 
		DEFAULT_ENGINE_PATCH = 1,
		DEFAULT_APP_MAJOR = 0, 
		DEFAULT_APP_MINOR = 1, 
		DEFAULT_APP_PATCH = 1;
	
	/** Basic logger for the class. */
	protected static Logger appLogger = Logger.getLogger(Application.class.getName());
	
	/** File with the application data. */
	private static File applicationLocation;
	/** File with application info. */
	private static VkApplicationInfo applicationInfo; 
	/** File with application assets. */
	private static Asset appAssets;
	/** File with application configuration assets. */
	private static Asset configAssets;
	
	/**
	 * Initializes the application and pools data from the config files.
	 * 
	 * @param appLocation				File containing the application.
	 * 
	 * @throws FileNotFoundException	When the asset wasn't found.
	 */
	public static void init(File appLocation) throws FileNotFoundException {
		if (applicationInfo != null)
			throw new AssertionError("Failed to initialize the application. As it was initialized earlier.");
		
		applicationLocation = appLocation;
		
		appAssets = new Asset(applicationLocation);
		configAssets = appAssets.getSubAsset(CONFIG_FOLDER_NAME);
		if (configAssets == null)
			throw new FileNotFoundException();

		try {
			applicationInfo = createAppInfo(configAssets);
		} catch (JSONException | IOException | AssertionError e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * Initializes the application and pools data from the config files.
	 * 
	 * @param appLocation				File containing the application.
	 * 
	 * @throws FileNotFoundException	When the asset wasn't found.
	 */
	public static void init(String configName) throws FileNotFoundException {
		if (applicationInfo != null)
			throw new AssertionError("Failed to initialize the application. As it was initialized earlier.");
		
		applicationLocation = new File("");
		
		appAssets = new Asset(new File(applicationLocation.getAbsolutePath() + "/" + CONFIG_FOLDER_NAME));
		configAssets = appAssets.getSubAsset(configName);
		if (configAssets == null)
			throw new FileNotFoundException();

		try {
			applicationInfo = createAppInfo(configAssets);
		} catch (JSONException | IOException | AssertionError e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * 
	 * @return		Location of the application.
	 */
	public static File getApplicationLocation() {
		return applicationLocation;
	}

	/**
	 * 
	 * Changes the application location.
	 * 
	 * @param appLocation Application location.
	 */
	public static void setApplicationLocation(File appLocation) {
		applicationLocation = appLocation;
	}

	/**
	 * 
	 * @return 	Vulkan Application info.
	 */
	public static VkApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}
	
	/**
	 * 
	 * @return		File with configuration assets.
	 */
	public static Asset getConfigAssets() {
		return configAssets;
	}

	/**
	 * Creates a vulkan application info.
	 * 
	 * @param appInfoFile		Configuration file.
	 * 
	 * @return		The vulkan application info.
	 * 
	 * @throws JSONException			
	 * 		If there is a syntax error in the source string
	 * 		or a duplicated key.
	 * @throws AssertionError 	If failed to create JSON file.
	 * @throws IOException 		If an I/O error occurred.
	 */
	private static VkApplicationInfo createAppInfo(Asset asset) throws JSONException, IOException, AssertionError {
		
		ConfigFile cfg = asset.getConfigFile(APPLICATION_INFO_FILE);
		
		VkApplicationInfo appInfo = VkApplicationInfo.calloc();
		appInfo
			.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
			.apiVersion(VK_MAKE_VERSION(cfg.getInteger(API_MAJOR_KEY, DEFAULT_API_MAJOR), cfg.getInteger(API_MINOR_KEY, DEFAULT_API_MINOR), cfg.getInteger(API_PATCH_KEY, DEFAULT_API_PATCH)));
		appInfo
			.pEngineName(memUTF8(cfg.getString(ENGINE_NAME_KEY, DEFAULT_ENGINE_NAME)))
			.engineVersion(VK_MAKE_VERSION(cfg.getInteger(ENGINE_MAJOR_KEY, DEFAULT_ENGINE_MAJOR), cfg.getInteger(ENGINE_MINOR_KEY, DEFAULT_ENGINE_MINOR), cfg.getInteger(ENGINE_PATCH_KEY, DEFAULT_ENGINE_PATCH)))
			.pApplicationName(memUTF8(cfg.getString(APPLICATION_NAME_KEY, DEFAULT_APPLICATION_NAME)))
			.applicationVersion(VK_MAKE_VERSION(cfg.getInteger(APP_MAJOR_KEY, DEFAULT_APP_MAJOR), cfg.getInteger(APP_MINOR_KEY, DEFAULT_APP_MINOR), cfg.getInteger(APP_PATCH_KEY, DEFAULT_APP_PATCH)));
		
		cfg.close();
		
		return appInfo;
	}

	/**
	 * Destroys the allocated data.
	 * <b>Note:</b> must be invoked on the main thread.
	 */
	public static void destroy() {
		if (applicationInfo != null) {
			memFree(applicationInfo.pApplicationName());
			memFree(applicationInfo.pEngineName());
			applicationInfo.free();
		}
	}
}
