package core;

public interface EngineTask extends Runnable{

	//TODO: Find a way to indicate which task is more important!
	
	@Override
	public void run() throws AssertionError;
}
