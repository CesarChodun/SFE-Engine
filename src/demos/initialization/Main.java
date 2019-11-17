package demos.initialization;

import core.Engine;
import core.EngineTask;

public class Main {
	
private static class ExampleClass implements Runnable, EngineTask {
		
		/**
		 * Our engine object. We need it to shut it down when we finish
		 * our work.
		 */
		private Engine engine;
		
		public ExampleClass(Engine engine) {
			this.engine = engine;
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
			
			System.out.println("Hello Vulkan Game Engine!");
			
			engine.stop();
		}
		
		
	}
	
	/**
	 * An example main method that starts the engine.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		Engine engine = new Engine();
		
		ExampleClass example = new ExampleClass(engine);
		engine.addTask(example);
		
		try {
			engine.run();
		}
		catch (Exception e) {
			System.err.println("Engine is shut down due to a problem:");
			e.printStackTrace();
		}
		finally {
			System.out.println("Engine successfully shut down.");
		}
		
	}
}
