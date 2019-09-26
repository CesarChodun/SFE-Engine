package debug.InitializationDemo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import core.Application;
import core.HardwareManager;
import core.resources.JSONParser;
import core.result.VulkanException;

public class InitializationDemo {
	
	public static class Test{
		int ID = 100;
		String name = "Test";
		Test consoleLog = null;
		String[] layers = {"layer1", "layer2"};
		
		public int getID() {
			return ID;
		}
		public void setID(int iD) {
			ID = iD;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public Test getConsoleLog() {
			return consoleLog;
		}
		public void setConsoleLog(Test consoleLog) {
			this.consoleLog = consoleLog;
		}
		public String[] getLayers() {
			return layers;
		}
		public void setLayers(String[] layers) {
			this.layers = layers;
		}	
		
	}

	public static void main(String[] args) {
		Handler consoleLog = new ConsoleHandler();
		consoleLog.setLevel(Level.ALL);
		Logger core = Logger.getLogger("core");
		core.addHandler(consoleLog);
		core.setLevel(Level.ALL);
		
		HardwareManager hardware = null;
		hardware = new HardwareManager();
		
		try {
			Application.init(new File("resources/InitializationDemo"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}