package game;

public interface GameStage {

	/**
	 * Error indicating fatal problem with the stage initialization.
	 * 
	 * @author Cezary Chodun
	 *
	 */
	public static class InitializationException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3633713520211791773L;
		
		private String message = "InitializationException";
		
		public InitializationException() {}
		
		public InitializationException(String message) {
			this.message = message;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
	}
	
	/**
	 * Error indicating fatal problem with the stage.
	 * 
	 * @author Cezary Chodun
	 *
	 */
	public static class GameStageException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4477033343909068519L;

		private String message = "GameStageException";
		
		public GameStageException() {}
		
		public GameStageException(String message) {
			this.message = message;
		}
		
		@Override
		public String getMessage() {
			return message;
		}
	}
	
	/**
	 * Initializes resources for the stage.
	 * 
	 * @throws InitializationError	when stage initialization failed.
	 */
	public void initialize() throws InitializationException;
	
	/**
	 * Presents stage(eg. window) to the user.
	 * 
	 * @throws GameStageError		when there was an error during stage lifetime.
	 */
	public void present() throws GameStageException;
	
	/**
	 * Hides the game stage(eg. Window).
	 */
	public void hide();
}
