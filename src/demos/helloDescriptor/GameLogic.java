package demos.helloDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import core.Engine;
import core.EngineTask;
import demos.helloDescriptor.initialization.AssetConversion;
import demos.helloDescriptor.initialization.InitializeEngine;
import demos.helloDescriptor.window.WindowManager;

public class GameLogic implements EngineTask {
	
	private Engine engine;
	
	public GameLogic(Engine engine) {
		this.engine = engine;
	}

	@Override
	public void run() throws AssertionError {
		redirectLoggers();
		
		// Semaphore indicating initialization state
		Semaphore initialized = new Semaphore(0);
		
		// Adding engine initialization task to the engine task queue
		engine.addTask(new InitializeEngine(initialized));
		
		// Convert resources
		AssetConversion.convertData();
		
		// Creating a thread that will wait until the engine is initialized and then
		// it will create the window.
		Thread waitForConfig = new Thread(new WindowManager(engine, initialized));
		waitForConfig.start();
	}

	private static void redirectLoggersToConsole(List<Logger> loggers) {
		Handler consoleLog = new ConsoleHandler();
		consoleLog.setLevel(Level.ALL);
		
		for (Logger lg : loggers) 
			lg.addHandler(consoleLog);
	}
	
	private void redirectLoggers() {
		List<Logger> loggers = new ArrayList<Logger>();
		Logger logger = Logger.getLogger("");
		logger.setLevel(Level.ALL);
		loggers.add(logger);
		
		redirectLoggersToConsole(loggers);
	}
	
}
