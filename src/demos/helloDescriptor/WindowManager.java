package demos.helloDescriptor;

import java.util.concurrent.Semaphore;

import core.Application;
import core.Engine;
import core.resources.Asset;
import demos.helloDescriptor.rendering.*;
import resources.memory.MemoryBin;
import util.window.CFrame;

/**
 * Creates a window and initializes the rendering layer for it.
 * 
 * @author Cezary Chodun
 * @since 10.01.2020
 */
public class WindowManager implements Runnable{
	
	private Engine engine;
	private Semaphore wait;
	
	public WindowManager(Engine engine, Semaphore wait) {
		this.engine = engine;
		this.wait = wait;
	}
	
	@Override
	public void run() {
		MemoryBin toDestroy = new MemoryBin();
		
		Semaphore windowCreated = new Semaphore(0);
		try {
			wait.acquire();
		} catch (InterruptedException e) {
			System.out.println("Window wait for semaphore interupted.");
			e.printStackTrace();
		}
		
		// Obtaining the asset folder for the window.
		Asset windowAsset = Application.getConfigAssets().getSubAsset("window");
		
		// Creating a task that will create the window.
		CFrame frame = new CFrame(engine, windowAsset, false, windowCreated, toDestroy);
//		TriangleWindow cwt = new TriangleWindow(engine, windowAsset, false, windowCreated, toDestroy);
//		engine.addTask(cwt);
		
		try {
			// Waiting for the window to be created.
			windowCreated.acquire();
		} catch (InterruptedException e) {
			System.out.println("Wait for window creation interupted.");
			e.printStackTrace();
		}
		
		// Creating the rendering task.
		InitializeRendering rendTask = new InitializeRendering(engine, frame.getWindow());
		toDestroy.add(rendTask);
		engine.addTask(rendTask);
	}

}
