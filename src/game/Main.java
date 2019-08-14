package game;

import core.Engine;

public class Main {

	
	
	public static void main(String[] args) {
		
		Engine gameEngine = new Engine();
		
		Game game = new Game(gameEngine);
		Thread gameThread = new Thread(game);
		gameThread.start();
		
		gameEngine.run();
		
	}
}
