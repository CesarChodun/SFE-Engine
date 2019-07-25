package game.game_loggs;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import game.GameStage;

public class LogStage implements GameStage{

	@Override
	public void initialize() throws InitializationException {
		List<Logger> loggers = new ArrayList<Logger>();
		Logger logger = Logger.getLogger("");
		logger.setLevel(Level.ALL);
		loggers.add(logger);
		
		redirectLoggersToConsole(loggers);
	}

	@Override
	public void present() throws GameStageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	private static void redirectLoggersToConsole(List<Logger> loggers) {
		Handler consoleLog = new ConsoleHandler();
		consoleLog.setLevel(Level.ALL);
		
		for (Logger lg : loggers) 
			lg.addHandler(consoleLog);
	}
	
}
