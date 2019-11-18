package demos.hardwareinit;

import java.util.concurrent.Semaphore;
import core.Engine;

public class ShutDownEngine implements Runnable{

	private Semaphore shouldShutDown;
	private Engine engine;
	
	public ShutDownEngine(Engine engine, Semaphore shouldShutDown) {
		this.shouldShutDown = shouldShutDown;
		this.engine = engine;
	}
	
	@Override
	public void run() {
		try {
			shouldShutDown.acquire();
		} catch (InterruptedException e) {
			System.err.println("Engine shutdown thread was interrupted.");
			e.printStackTrace();
		}
		finally {
			engine.stop();
		}
	}

}
