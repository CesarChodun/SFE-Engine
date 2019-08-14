package game.rendering;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;

import core.EngineTask;

public class GLFWTask implements EngineTask{

	@Override
	public void run() throws AssertionError {
		
		glfwPollEvents();
	}

}
