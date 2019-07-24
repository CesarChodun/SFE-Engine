package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.lwjgl.vulkan.VkApplicationInfo;

import core.resources.Asset;

import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.system.MemoryUtil.*;


public class Application {

	public static final boolean DEBUG = true;
	public static final String CONFIG_FOLDER_NAME = "config"; 
	public static final String APPLICATION_INFO_FILE = "appInfo.cfg";
	
	protected static final String 
		ENGINE_NAME = "ENGINE_NAME", APPLICATION_NAME = "APPLICATION_NAME",
		API_MAJOR = "API_VERSION_MAJOR", API_MINOR = "API_VERSION_MINOR", API_PATCH = "API_VERSION_PATCH",
		ENGINE_MAJOR = "ENGINE_VERSION_MAJOR", ENGINE_MINOR = "ENGINE_VERSION_MINOR", ENGINE_PATCH = "ENGINE_VERSION_PATCH",
		APP_MAJOR = "APPLICATION_VERSION_MAJOR", APP_MINOR = "APPLICATION_VERSION_MINOR", APP_PATCH = "APPLICATION_VERSION_PATCH";
	protected static Logger appLogger = Logger.getLogger(Application.class.getName());
	
	private static File applicationLocation;
	private static VkApplicationInfo applicationInfo; 
	private static Asset appAssets;
	private static Asset configAssets;
	
	public static void init(File appLocation) throws FileNotFoundException {
		applicationLocation = appLocation;
		
		appAssets = new Asset(applicationLocation);
		configAssets = appAssets.getSubAsset(CONFIG_FOLDER_NAME);
		if (configAssets == null)
			throw new FileNotFoundException();

		createAppInfo();	
	}
	
	
	
	public static File getApplicationLocation() {
		return applicationLocation;
	}



	public static void setApplicationLocation(File appLocation) {
		applicationLocation = appLocation;
	}



	public static VkApplicationInfo getApplicationInfo() {
		return applicationInfo;
	}



	public static void setApplicationInfo(VkApplicationInfo appInfo) {
		applicationInfo = appInfo;
	}

	private static void createAppInfo() throws FileNotFoundException {
		// Creating applicationInfo
		File appInfoFile = configAssets.get(APPLICATION_INFO_FILE);
		if (!appInfoFile.exists())
			try {
				appLogger.log(Level.WARNING, "Failed to locate app info file!");
				
				FileWriter out = new FileWriter(appInfoFile);
				createJSONAppInfo().write(out);
				out.close();
			} catch (JSONException | IOException e) {
				System.err.println("Failed to locate and create the app info file!");
				e.printStackTrace();
			}
		
		try {
			applicationInfo = createAppInfo(appInfoFile);
		} catch (JSONException | FileNotFoundException e) {
			System.err.println("Failed to locate(or read) app info file!");
			e.printStackTrace();
		}
	}
	
	public static Asset getConfigAssets() {
		return configAssets;
	}



	private static VkApplicationInfo createAppInfo(File appInfoFile) throws JSONException, FileNotFoundException {
		JSONObject obj = new JSONObject(new JSONTokener(new FileReader(appInfoFile)));
		
		VkApplicationInfo appInfo = VkApplicationInfo.calloc()
			.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO)
			.apiVersion(VK_MAKE_VERSION(obj.getInt(API_MAJOR), obj.getInt(API_MINOR), obj.getInt(API_PATCH)))
			.pEngineName(memUTF8(obj.getString(ENGINE_NAME)))
			.engineVersion(VK_MAKE_VERSION(obj.getInt(ENGINE_MAJOR), obj.getInt(ENGINE_MINOR), obj.getInt(ENGINE_PATCH)))
			.pApplicationName(memUTF8(obj.getString(APPLICATION_NAME)))
			.applicationVersion(VK_MAKE_VERSION(obj.getInt(APP_MAJOR), obj.getInt(APP_MINOR), obj.getInt(APP_PATCH)));
		
		return appInfo;
	}
	
	public static JSONObject createJSONAppInfo() {
		JSONObject obj = new JSONObject();
		
		obj.put(ENGINE_NAME, "Engine");
		obj.put(APPLICATION_NAME, "Application");
		
		obj.put(API_MAJOR, 1);
		obj.put(API_MINOR, 0);
		obj.put(API_PATCH, 1);
		
		obj.put(APP_MAJOR, 0);
		obj.put(APP_MINOR, 1);
		obj.put(APP_PATCH, 1);
		
		obj.put(ENGINE_MAJOR, 0);
		obj.put(ENGINE_MINOR, 1);
		obj.put(ENGINE_PATCH, 1);
		
		return obj;
	}
}
