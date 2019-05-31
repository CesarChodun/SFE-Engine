package space;

import java.util.Comparator;

import org.joml.Vector3f;

public class Position extends Vector3f{

	public static class DistanceComparator implements Comparator<Position>{
		Vector3f origin;
		
		public DistanceComparator(Vector3f origin) {
			this.origin = origin;
		}

		@Override
		public int compare(Position o1, Position o2) {
			return Float.compare(o1.distance(origin), o2.distance(origin));
		}
	}
	
	public Position(Position pos) {
		super(pos.x, pos.y, pos.z);
	}
	
	public Position(int x, int y, int z) {
		super(x, y, z);
	}
}
