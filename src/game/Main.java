package game;

import java.util.ArrayList;
import java.util.List;

import game.game_logo.GameLogoStage;

public class Main {

	public static void main(String[] args) {
		
		List<GameStage> stages = new ArrayList<GameStage>();
		stages.add(new GameLogoStage());
//		...
//		stages.add(new GameShutDownIcon()); //TODO
		
		if(stages.size() == 0)
			return;
		
		try {
			// Initializes the first stage.
			stages.get(0).initialize();
			
			for (int i = 0; i < stages.size(); i++) {
				// Hides the last stage.
				if (i > 0)
					stages.get(i-1).hide();
				
				// Presents the current stage.
				stages.get(i).present();
				
				// Initializes the next stage.
				if (i+1 < stages.size())
					stages.get(i+1).initialize();
			}
			
			// Hides the last game stage.
			stages.get(stages.size()-1).hide();
		}
		catch (Exception e) {
			unscheduledShutdown(e);
		}
		
		scheduledShutdown();
	}
	
	private static void unscheduledShutdown(Exception e) {
		//TODO: Print the logs and etc.
	}

	private static void scheduledShutdown() {
		//TODO: Consider printing logs and free all resources.
	}
	
}
