package demos.hardwareinit;

import java.util.concurrent.Semaphore;

import core.Engine;
import core.EngineTask;

public class GameLogic implements EngineTask{

	/**
	 * Our engine object. We need it to shut it down when we finish
	 * our work.
	 */
	private Engine engine;
	
	public GameLogic(Engine engine) {
		this.engine = engine;
	}
	
	@Override
	public void run() throws AssertionError {
		System.out.println("The engine is running.");
		
		// Semaphores for monitoring the progress.
		Semaphore initialized = new Semaphore(0);
		Semaphore reported = new Semaphore(0);
		
		// Engine tasks that we want to perform.
		InitializeEngine init = new InitializeEngine(initialized);
		EngineReport report = new EngineReport(reported);
		
		// A thread that will monitor if the engine should be shut down.
		Thread shutDownMonitor = new Thread(new ShutDownEngine(engine, reported));
		
		// Adding engine tasks to the engine.
		// They will be invoked on the main thread.
		// In the same order as here.
		engine.addTask(init);
		engine.addTask(report);
		
		// Starting a thread that will monitor if the engine should
		// be shut down. In the real application this should be monitored 
		// by a separate logic thread.
		shutDownMonitor.start();
	}

}
