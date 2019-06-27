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

public class JSONParser {
	
	protected static final String CLASS_IDENTIFIER = "Class";
	protected static final String GET_PREF = "get";
	protected static final String SET_PREF = "set";
	public static final String LOGGER_NAME = "core.resources.JSONParser";
	
	
	protected static Logger logger = Logger.getLogger(LOGGER_NAME);
	
	private static Method searchForMethod(Class<?> c, String name, Class<?>... params) {
		Method method = null;
		
		try {
			method = c.getDeclaredMethod(name, params);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		
		return method;
	}
	
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
	
	public static JSONObject readJSONObject(File file) throws FileNotFoundException {
		FileReader reader = new FileReader(file);
		
		return new JSONObject(new JSONTokener(reader));
	}
	
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
//						
//						if (m == null && toAdd.getClass().equals(Integer.class))
//							m = searchForMethod(obj.getClass(), setMethodName, int.class);

						if (m != null)
							m.invoke(obj, toAdd);
					} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
					
				}
			}
	}

	
	public static JSONObject toJSON(Object obj) {
//		JSONObject json = new JSONObject();
//		Method[] methods = obj.getClass().getDeclaredMethods();
//		
//		json.put(CLASS_IDENTIFIER, obj.getClass());
//		for (Method m : methods) {
//			if(m.getName().startsWith(GET_PREF) && m.getParameterCount() == 0) {
//				try {
//					String parameterName = m.getName().substring(GET_PREF.length());
//					parameterName = String.valueOf(parameterName.charAt(0)).toLowerCase() + 
//										parameterName.substring(1);
//					
//					Object next = m.invoke(obj, null);
//					
//					if (m.getReturnType().isPrimitive() || m.getReturnType() == String.class)
//						json.put(parameterName, next);
//					else {
//						if (next != null)
//							json.put(parameterName, toJSON(next));
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		return json;
		return new JSONObject(obj);
	}
	
}
