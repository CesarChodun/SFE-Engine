package core.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


public class Util {

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
