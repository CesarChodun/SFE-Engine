package demos.helloTriangle;

import java.util.concurrent.Semaphore;

import core.Engine;
import core.EngineTask;
import demos.helloTriangle.initialization.InitializeEngine;
import demos.helloTriangle.window.WindowManager;

public class GameLogic implements EngineTask {
	
	private Engine engine;
	
	public GameLogic(Engine engine) {
		this.engine = engine;
	}

	@Override
	public void run() throws AssertionError {
		// Semaphore indicating initialization state
		Semaphore initialized = new Semaphore(0);
		
		// Adding engine initialization task to the engine task queue
		engine.addTask(new InitializeEngine(initialized));
		
		// Creating a thread that will wait until the engine is initialized and then
		// it will create the window.
		Thread waitForConfig = new Thread(new WindowManager(engine, initialized));
		waitForConfig.start();
	}

}
