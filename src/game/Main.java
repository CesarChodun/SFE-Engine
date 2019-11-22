package game;

import core.Engine;

public class Main {
	
	
	public static void main(String[] args) {
		Engine engine = new Engine();
		
		Game game = new Game(engine);
		engine.addTask(game);
		
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
 