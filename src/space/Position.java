package space;

import java.util.Comparator;

import org.joml.Vector3f;

/**
 * Class representing 3D float based position.
 * 
 * @author Cezary Chodun
 *
 */
public class Position extends Vector3f{

	/**
	 * Class for comparing distance of two Positions
	 * in reference to the given point.
	 * 
	 * @author Cezary Chodun
	 *
	 */
	public static class DistanceComparator implements Comparator<Position>{
		/**
		 * The reference point.
		 */
		private final Vector3f origin = new Vector3f();
		
		/**
		 * Creates a new DistanceComparator with the
		 * given reference point.
		 * 
		 * @param origin	the reference point 
		 */
		public DistanceComparator(Vector3f origin) {
			this.origin.set(origin);
		}

		@Override
		public int compare(Position o1, Position o2) {
			return Float.compare(o1.distance(origin), o2.distance(origin));
		}
	}
	
	/**
	 * Creates a new position with the same 
	 * coordinates as <b>pos</b>.
	 * 
	 * @param pos		The position from which the 
	 * 					coordinates will be copied
	 */
	public Position(Position pos) {
		super(pos.x, pos.y, pos.z);
	}
	
	/**
	 * Creates a new position with given coordinates.
	 * @param x		X coordinate
	 * @param y		Y coordinate
	 * @param z		Z coordinate
	 */
	public Position(int x, int y, int z) {
		super(x, y, z);
	}
}
