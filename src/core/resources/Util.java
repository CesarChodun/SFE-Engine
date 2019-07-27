package core.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class Util {
	
	
	
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
}
