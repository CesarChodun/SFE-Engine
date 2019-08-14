package core.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class ResourceUtil {
	
	public static final Integer INDENT_FACTOR = 4;
	public static final Integer INDENT = 1;
	
	public static String getCompatibleStringProperty(File file, String property, Collection<? extends String> compatible) throws JSONException, FileNotFoundException {
		
		JSONObject obj = new JSONObject(new JSONTokener(new FileReader(file)));
		JSONArray arr = obj.getJSONArray(property); 
		HashSet<String> map = new HashSet<String>(compatible);
		
		for (int  i = 0; i < arr.length(); i++)
			if (map.contains(arr.getString(i)))
				return arr.getString(i);
		
		return null;
	} 

	public static String getStringProperty(File file, String property) {
		try {
			JSONObject obj = new JSONObject(new JSONTokener(new FileReader(file)));
			
			return obj.getString(property);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
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
