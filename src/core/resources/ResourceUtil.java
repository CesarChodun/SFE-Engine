package core.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * 
 * Class for managing resource containers.
 * 
 * @author Cezary Chodun
 *
 */
public class ResourceUtil {
	
	/** Indent factor for the JSON writer(number of spaces). */
	public static final Integer INDENT_FACTOR = 4;
	/** Initial indent for JSON writer. */
	public static final Integer INDENT = 1;
	
	private static final Level JSON_ERROR_MSG_LVL = Level.FINER;
	private static final Logger logger = Logger.getLogger(ResourceUtil.class.getName());
	
	/** 
	 * Returns the first compatible option from 
	 * the JSON configuration file.
	 * 
	 * @param file			JSON file.
	 * @param property		the property name.
	 * @param compatible	a list of compatible options.
	 * 
	 * @return		The first compatible option.
	 * 
	 * @throws JSONException			
	 * 		If there is a syntax error
	 * 		in the source string or a duplicated key.
	 * @throws FileNotFoundException	
	 * 		if the file does not exist, is a directory
	 * 		rather than a regular file, or for some other
	 * 		reason cannot be opened for reading.
	 */
	public static String getCompatibleStringProperty(File file, String property, Collection<? extends String> compatible) throws JSONException, FileNotFoundException {
		
		JSONObject obj = new JSONObject(new JSONTokener(new FileReader(file)));
		JSONArray arr = obj.getJSONArray(property); 
		HashSet<String> map = new HashSet<String>(compatible);
		
		for (int  i = 0; i < arr.length(); i++)
			if (map.contains(arr.getString(i)))
				return arr.getString(i);
		
		return null;
	} 
	
	/**
	 * 
	 * Converts a meta language length description to
	 * the corresponding value.
	 * 
	 * @param pos		Metalanguage position.
	 * @param maxPx		Maximum position.
	 * 
	 * @return			Converted position in pixels.
	 */
	public static int toPx(String pos, int maxPx) {
		if (pos.startsWith("+"))
			return toPx(pos.substring(1), maxPx);
		
		if (pos.contains("-"))
			return maxPx - toPx(pos.substring(1), maxPx);
		
		if (pos.contains("%")) {
			String[] spl = pos.split("%");
			String prc = spl[0];
			
			int out = (int) (Float.valueOf(prc) * maxPx / 100);
			if (spl.length > 1)
				out += toPx(pos.substring(prc.length() + 1), maxPx - out);
			return  out;
		}
		
		return Integer.valueOf(pos);
	}

	/**
	 * 
	 * Obtains a String property from a JSON file.
	 * 
	 * @param file		The JSON file.
	 * @param property	The property name.
	 * 
	 * @return			Corresponding value.
	 */
	public static String getStringProperty(File file, String property) {
		try {
			JSONObject obj = new JSONObject(new JSONTokener(new FileReader(file)));
			
			return obj.getString(property);
		} catch (Exception e) {
			logger.log(JSON_ERROR_MSG_LVL, "Failed to get JSON property", e);
		}
		
		return null;
	}
	
	/**
	 * 
	 * Obtains default static Integer values from a given class.
	 * 
	 * @param c 						The class to retrieve static values from.
	 * @param valueNames				The names of the static values.
	 * 
	 * @return	A list of the values. In the same order as given by the
	 * 			<code>valueNames</code> list.
	 * 
	 * @throws NoSuchFieldException			if a field with the specified name is not found.
	 * 
	 * @throws SecurityException			 If a security manager, s, is present and the caller's
	 * 	class loader is not the same as or an ancestor of the class loader for the current class
	 * 	and invocation of s.checkPackageAccess() denies access to the package of this class.
	 * 
	 * @throws IllegalArgumentException		if the specified object is not an instance of 
	 * 	the class or interface declaring the underlying field (or a subclass or implementor thereof), 
	 * 	or if the field value cannot be converted to the type int by a widening conversion.
	 * 
	 * @throws IllegalAccessException		if this Field object is enforcing Java language
	 * 		 access control and the underlying field is inaccessible.
	 */
	public static List<Integer> getStaticIntValuesFromClass(Class<?> c, List<String> valueNames) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		List<Integer> out = new ArrayList<Integer>();
		for (int i = 0; i < valueNames.size(); i++) {
			Field f = c.getField(valueNames.get(i));
			
			out.add(f.getInt(null));
		}
		
		return out;
	}
}
