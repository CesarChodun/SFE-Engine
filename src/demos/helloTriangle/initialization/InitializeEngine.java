package demos.helloTriangle.initialization;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import core.Application;
import core.EngineTask;
import core.HardwareManager;
import core.result.VulkanException;

public class InitializeEngine implements EngineTask {
	
	final static String CONFIG_FILE = "demos/hellotriangle";
	private Semaphore workDone;
	
	public InitializeEngine(Semaphore workDone) {
		this.workDone = workDone;
	}

	@Override
	public void run() {
		/*
		 * 	This method("run()") will be invoked first.
		 *	It is guaranteed that it will be run on the first thread.
		 *	And this is the place where you want to start programming your game.
		 *	You can think about it like a 'main' method.
		 * 
		 */
		
		try {
			// Initializing application
			Application.init(CONFIG_FILE);
			
			System.out.println("Application data succesfully initialized!");
			
		} catch (FileNotFoundException e) {
			
			System.err.println("Failed to find the configuration file(\"" + CONFIG_FILE + "\")");
			e.printStackTrace();
		}
		
		try {
			// Initializing hardware
			HardwareManager.init(Application.getApplicationInfo(), Application.getConfigAssets());
			System.out.println("Hardware succesfully initialized!");			
		} catch (VulkanException e) {
			
			System.err.println("Failed to initialize hardware due to a vulkan problem.");
			e.printStackTrace();
		} catch (IOException e) {
			
			System.err.println("Failed to initialize hardware due to an input(or output) error.");
			e.printStackTrace();
		} 
		
		workDone.release();
	}
}