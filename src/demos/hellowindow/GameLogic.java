package demos.hellowindow;

import java.util.concurrent.Semaphore;

import core.Application;
import core.Engine;
import core.EngineTask;

public class GameLogic implements EngineTask {
	
	private static class WaitForConfig implements Runnable {

		private Semaphore wait;
		private Engine engine;
		
		public WaitForConfig(Engine engine, Semaphore wait) {
			this.engine = engine;
			this.wait = wait;
		}
		
		@Override
		public void run() {
			try {
				wait.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			engine.addTask(new WindowManager(engine, Application.getConfigAssets()));
		}
		
	}

	private Engine engine;
	
	public GameLogic(Engine engine) {
		this.engine = engine;
	}

	@Override
	public void run() throws AssertionError {
		Semaphore initialized = new Semaphore(0);
		
		engine.addTask(new InitializeEngine(initialized));
		
		Thread waitForConfig = new Thread(new WaitForConfig(engine, initialized));
		waitForConfig.start();
	}

}
