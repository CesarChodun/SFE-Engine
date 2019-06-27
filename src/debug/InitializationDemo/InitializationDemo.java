package debug.InitializationDemo;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

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
		try {
			hardware = new HardwareManager();
		} catch (VulkanException e) {
			e.printStackTrace();
		}
		
//		JSONObject p = new JSONObject("");
		Test test = new Test();
		test.setID(1);
		test.setConsoleLog(new Test());
		test.setName("Kolejny");
		
		JSONObject json = JSONParser.toJSON(test);
		System.out.print(json.toString(2));
		
		JSONObject json2 = new JSONObject(s);
		Test test2 = new Test();
		Test child = new Test();
		test2.setConsoleLog(child);
		JSONParser.populateData(test2, json2);
		
		JSONObject json3 = JSONParser.toJSON(test2);
		System.out.print(json3.toString(2));
		
		
	}
	
}