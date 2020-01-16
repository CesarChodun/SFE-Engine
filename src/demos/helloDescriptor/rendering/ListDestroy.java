package demos.helloDescriptor.rendering;

import java.util.ArrayList;
import java.util.List;

import core.resources.Destroyable;

/**
 * A class for grouping destroyable objects.
 */
public class ListDestroy implements Destroyable {

	// Objects to destroy
	private List<Destroyable> toDestroy = new ArrayList<>();
	
	// Ands a new destroyable object.
	public void add(Destroyable... descriptorSets) {
		for (Destroyable d : descriptorSets)
			toDestroy.add(d);
	}
	
	@Override
	public void destroy() {
		for (Destroyable d : toDestroy)
			d.destroy();
	}
	
}