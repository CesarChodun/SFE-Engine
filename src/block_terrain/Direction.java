package block_terrain;

import org.joml.Vector3f;

public enum Direction {
	
	NORTH(0), EAST(1), SOUTH(2), WEST(3), DOWN(4), UP(5);
	
	public static final int ALL = 6;
	public static final int HORIZONTAL = 4;
	
	int num;
	
	private Direction(int num) {
		this.num = num;
	}
	
	public int getNum() {
		return num;
	}
	
	/**
	 * Left Handed Z+
	 * @return
	 */
	public Vector3f toVec3f() {
		switch(num) {
		case 0:
			return new Vector3f(0, 0, 1);
		case 1:
			return new Vector3f(1, 0, 0);
		case 2:
			return new Vector3f(0, 0, -1);
		case 3:
			return new Vector3f(-1, 0, 0);
		case 4:
			return new Vector3f(0, -1, 0);
		case 5:
			return new Vector3f(0, 1, 0);
		}
		
		return new Vector3f(0, 0, 0);
	}
	
	public static Direction getDir(int num) {
		switch(num) {
		case 0:
			return NORTH;
		case 1:
			return EAST;
		case 2:
			return SOUTH;
		case 3:
			return WEST;
		case 4:
			return DOWN;
		case 5:
			return UP;
		}
		
		return null;
	}
	
	public static Direction horizontalShiftClockwise(Direction dir, int rot) {
		if(dir == UP || dir == DOWN)
			return dir;
		
		rot = rot%4;
		return getDir((dir.num + rot + 8)%4);
	}
}
