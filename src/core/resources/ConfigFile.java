package core.resources;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class for obtaining configuration 
 * data from file.
 * 
 * @author Cezary Chodun
 * @since 25.09.2019
 */
public class ConfigFile implements Closeable{
	
	/** Default logger for the class. */
	private static final Logger cfgLogging = Logger.getLogger(ConfigFile.class.getName());

	/** Tells whether the JSON object was created. */
	boolean created = false;
	/** The JSON object with configuration data. */
	private JSONObject file;
	/** Parental asset folder. */
	private Asset asset;
	/** The path to the JSON file. */
	private String path;
	
	/**
	 * Creates a new configuration file from the asset,
	 * and given its path within the asset.
	 * 
	 * @param asset		Asset containing the configuration file.
	 * @param path		Path to the configuration file within the asset.
	 * @throws IOException		If an I/O error occurred.
	 */
	public ConfigFile(Asset asset, String path) throws IOException{
		this.asset = asset;
		this.path = path;
		
		try {
			this.file = asset.getJSON(path);
		} catch (IOException e) {
			cfgLogging.log(Level.INFO, "Failed to locate CFG file: " + asset.getAssetLocation().getPath() + "/" + path, e);
		}
		
		if (this.file == null) {
			created = true;
			
			this.file = new JSONObject();//asset.getJSON(path);
		}
	}
	
	/**
	 * Saves JSON data to the file.
	 */
	public void close() {
		if (created) {
			FileWriter writer;
			try {
				writer = new FileWriter(asset.get(path));
				file.write(writer, ResourceUtil.INDENT_FACTOR, ResourceUtil.INDENT);
				writer.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Obtains an array from the JSON object.
	 * 
	 * @param <T>			Type of the objects in the list.
	 * @param key			The key(name) of the value.
	 * @param defaultValue	The default value for the key.
	 * @return		A list with the objects from the JSON file.
	 */
	public <T> List<T> getArray(String key, List<T> defaultValue) {		
		if (file.isNull(key)) {
			file.put(key, defaultValue);
			return defaultValue;
		}

		JSONArray arr = file.getJSONArray(key);
		List<T> out = new ArrayList<T>();
		for (int i = 0; i < arr.length(); i++)
			out.add((T) arr.get(i));
		
		return out;
	}
	
	/**
	 * 
	 * Obtains a String from the JSON file.
	 * 
	 * @param key			Key of the value.
	 * @param defaultValue	Default value.
	 * @return		
	 * 		Value corresponding to the key from the
	 * 		JSON object or the default value.
	 */
	public String getString(String key, String defaultValue) {
		
		if (!file.isNull(key)) 
			return file.getString(key);
		
		file.put(key, defaultValue);
		return defaultValue;
	}
	
	/**
	 * 
	 * Obtains a Integer from the JSON file.
	 * 
	 * @param key			Key of the value.
	 * @param defaultValue	Default value.
	 * @return		
	 * 		Value corresponding to the key from the
	 * 		JSON object or the default value.
	 */
	public Integer getInteger(String key, Integer defaultValue) {
		
		if (!file.isNull(key))
			return file.getInt(key);
		
		file.put(key, (int) defaultValue);
		return defaultValue;
	}

	/**
	 * 
	 * Obtains a Float from the JSON file.
	 * 
	 * @param key			Key of the value.
	 * @param defaultValue	Default value.
	 * @return		
	 * 		Value corresponding to the key from the
	 * 		JSON object or the default value.
	 */
	public Float getFloat(String key, Float defaultValue) {
		
		if (!file.isNull(key))
			return file.getFloat(key);
		
		file.put(key, (float) defaultValue);
		return defaultValue;
	}
	
	/**
	 * 
	 * Obtains a Double from the JSON file.
	 * 
	 * @param key			Key of the value.
	 * @param defaultValue	Default value.
	 * @return		
	 * 		Value corresponding to the key from the
	 * 		JSON object or the default value.
	 */
	public Double getDouble(String key, Double defaultValue) {
		
		if (!file.isNull(key))
			return file.getDouble(key);
		
		file.put(key, (double) defaultValue);
		return defaultValue;
	}
	
	/**
	 * 
	 * Obtains a Boolean from the JSON file.
	 * 
	 * @param key			Key of the value.
	 * @param defaultValue	Default value.
	 * @return		
	 * 		Value corresponding to the key from the
	 * 		JSON object or the default value.
	 */
	public Boolean getBoolean(String key, Boolean defaultValue) {
		
		if (!file.isNull(key))
			return file.getBoolean(key);
		
		file.put(key, (boolean) defaultValue);
		return defaultValue;
	}
}
