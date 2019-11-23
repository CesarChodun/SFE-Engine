package rendering.pipeline;

import core.resources.Destroyable;

public interface Pipeline extends Destroyable{

	public long handle();
	
}
