package core.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.json.JSONTokener;

import core.Application;

/**
 * Class for transferring information
 * from a JSON file to a class.
 * 
 * @author Cezary Chodun
 * @since 26.09.2019
 */
public class JSONParser {
	
	/** */
	protected static final String 
			CLASS_IDENTIFIER = "Class",
			GET_PREF = "get",
			SET_PREF = "set",
			LOGGER_NAME = "core.resources.JSONParser";
	
	/** Default class logger. */
	protected static Logger logger = Logger.getLogger(LOGGER_NAME);
	
	/**
	 * Searches for a method in a class.
	 * 	
	 * @param c			The class.
	 * @param name		Name of the method.
	 * @param params	Parameter types.
	 * 
	 * @return		
	 * 		If the method exists it is returned, 
	 * 		otherwise null is returned. 
	 */
	private static Method searchForMethod(Class<?> c, String name, Class<?>... params) {
		Method method = null;
		
		try {
			method = c.getDeclaredMethod(name, params);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		
		return method;
	}
	
	/**
	 * Compares two classes, and checks
	 * whether they are interchangeable.
	 * 
	 * @param c1	The first class.
	 * @param c2	The second class.
	 * @return		True if classes are  interchangeable.
	 */
	private static boolean compareClass(Class<?> c1, Class<?> c2) {
		if(c1.equals(c2))
			return true;
		
		if(c1.equals(Integer.class))
			return int.class.equals(c2);
		if(c1.equals(Boolean.class))
			return boolean.class.equals(c2);
		if(c1.equals(Float.class))
			return float.class.equals(c2);
		if(c1.equals(Long.class))
			return long.class.equals(c2);
		
		if(c2.equals(Integer.class))
			return int.class.equals(c1);
		if(c2.equals(Boolean.class))
			return boolean.class.equals(c1);
		if(c2.equals(Float.class))
			return float.class.equals(c1);
		if(c2.equals(Long.class))
			return long.class.equals(c1);
		
		return false;
	}
	
	/**
	 * Searches for similar method in a class.
	 * 
	 * @param c			The class.
	 * @param name		Name of the method.
	 * @param params	Parameter types.
	 * @return		
	 * 		If a method with interchangeable parameters
	 * 		exists and has the same name it is returned.
	 * 		Otherwise null is returned.
	 */
	private static Method searchForSimilarMethod(Class<?> c, String name, Class<?>... params) {
		for (Method m : c.getMethods()) 
			if (m.getName().equals(name)) {
				Class<?>[] original = m.getParameterTypes();
				
				if (original.length == params.length) {
					for (int i = 0; i < params.length; i++)
						if (!compareClass(original[i], params[i]))
							return null;
					return m;
				}
			}
		
		return null;
	}
	
	/**
	 * Reads JSON data from a JSON file.
	 * 
	 * @param file		The JSON file.
	 * @return		A JSON object created from the file.
	 * @throws FileNotFoundException	
	 * 		If the file does not exist,
	 * 		is a directory rather than a regular file,
	 * 		or for some other reason cannot be opened for
	 * 		reading.
	 */
	public static JSONObject readJSONObject(File file) throws FileNotFoundException {
		FileReader reader = new FileReader(file);
		
		return new JSONObject(new JSONTokener(reader));
	}
	
	/**
	 * Populates object values with JSON data.
	 * 
	 * @param obj	The object.
	 * @param json	The JSON object.
	 */
	public static void populateData(Object obj, JSONObject json) {
		if (obj ==  null) {
			if(Application.DEBUG)
				logger.log(Level.WARNING, "Failed to load data from JSON file! "
						+ "The targeted object does not exist!");
			return;
		}
		if (!json.get(CLASS_IDENTIFIER).equals(obj.getClass().toString())) {
			if(Application.DEBUG)
				logger.log(Level.WARNING, "Failed to load data from JSON file! "
						+ "The Class names does not match. JSON class name: "
						+ json.get(CLASS_IDENTIFIER) + ", Object class name: "
						+ obj.getClass().toString());
			return;
		}
		
		for (String s : json.keySet()) 
			if (!s.equals(CLASS_IDENTIFIER)) {
				String setMethodName = SET_PREF + s.substring(0, 1).toUpperCase() + s.substring(1);
				
				Object toAdd = json.get(s);
				if (JSONObject.class.isInstance(toAdd)) {
					String getMethodName = GET_PREF + s.substring(0, 1).toUpperCase() + s.substring(1);
					Method getMethod = null;
					Object sub = null;
					
					try {
						getMethod = searchForMethod(obj.getClass(), getMethodName, (Class<?>[]) null);
						
						if(getMethod != null)
							sub = getMethod.invoke(obj, (Object[]) null);
						
						populateData(sub, (JSONObject) toAdd);
					}
					catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
				}
				else {
					Method m = null;
					try {
						m = searchForSimilarMethod(obj.getClass(), setMethodName, toAdd.getClass());

						if (m != null)
							m.invoke(obj, toAdd);
					} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
					
				}
			}
	}
	
}
