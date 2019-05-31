package block_terrain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.joml.Vector3f;

import space.Position;

public class ConnectedChunk extends Chunk{

	private ConnectedChunk[] connected = new ConnectedChunk[6];
	

	public ConnectedChunk() {
		super();
		
		for(int i = 0; i < 6; i++) 
			connected[i] = null;
	}
	
	public void connectChunk(ConnectedChunk c, Direction dir) {
		connected[dir.getNum()] = c;
	}
	
	public void desconnectChunk(Direction dir) {
		connected[dir.getNum()] = null;
	}
	
	public ConnectedChunk getChunk(Direction dir) {
		return connected[dir.getNum()];
	}
	
	public ConnectedChunk getChunk(int dir) {
		return connected[dir];
	}
	
	
	//STATIC:
	
	public static ArrayList<ConnectedChunk> planeSearch(ConnectedChunk origin, int chunks) {
		HashMap<Position, ConnectedChunk> visited = new HashMap<Position, ConnectedChunk>();
		Position.DistanceComparator cmp = new Position.DistanceComparator(new Vector3f(0, 0, 0));
		PriorityQueue<Position> byDistance = new PriorityQueue<Position>(cmp);
		
		ArrayList<ConnectedChunk> out = new ArrayList<ConnectedChunk>();
		
		Position first = new Position(0, 0, 0);
		visited.put(first, origin);
		byDistance.add(first);
		
		while(chunks > 0 && !byDistance.isEmpty()) {
			Position p = byDistance.poll();
			ConnectedChunk chunk = visited.get(p);
			out.add(chunk);
			chunks--;
			
			for(int i = 0; i < Direction.HORIZONTAL; i++) {
				ConnectedChunk nc = chunk.getChunk(i);
				
				if(nc != null) {
					Position np = new Position(p);
					np.add(Direction.getDir(i).toVec3f());
					
					visited.put(np, nc);
					byDistance.add(np);
				}
			}
		}
		
		return out;
	}
}
