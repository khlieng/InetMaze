package mazeoblig;

import java.rmi.RemoteException;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import simulator.PositionInMaze;

public class Players extends UnicastRemoteObject implements PlayersInterface {
	//private static PositionInMaze[] positions;
	private static UpdateListener[] listeners;
	private static ConcurrentHashMap<Integer, PositionInMaze> updatedPositions;
	//private static int count;
	private static int elapsed;
	private ArrayList<UpdateListener> contains = new ArrayList<UpdateListener>();
	private boolean locked = false;
		
	protected Players() throws RemoteException {
		//positions = new PositionInMaze[Maze.CLIENTS + 1];
		listeners = new UpdateListener[2];
		updatedPositions = new ConcurrentHashMap<Integer, PositionInMaze>();
		
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					elapsed += 10;
					if (elapsed >= Maze.SRV_UPDATERATE) {
						elapsed -= Maze.SRV_UPDATERATE;
						/*for (int i = 0; i < listeners.length; i++) {
							if (listeners[i] != null) {
								try {
									listeners[i].pushPositions(updatedPositions);
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
						}*/	
						
						//locked = true;
						//Thread.sleep(1000);
						//Set<Integer> keys = updatedPositions.keySet();
						//Collection<PositionInMaze> vals = updatedPositions.values();
						try {
						int n = updatedPositions.size(), i = 0;
						int[] positions = new int[n];
							for (int key : updatedPositions.keySet()) {
								int id = key << 16;
								int x = updatedPositions.get(key).getXpos() << 8;
								int y = updatedPositions.get(key).getYpos();
								positions[i] = id | x | y;
								i++;
							}
							try {
								if (listeners[0] != null)
									listeners[0].pushPositions(positions);
								if (listeners[1] != null)
									listeners[1].pushPositions(positions);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						} catch(Exception e) {
							
						}
						//locked = false;
						
						
						updatedPositions.clear();
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	int c = -1;
	public int join(UpdateListener listener) throws RemoteException {
		c++;
		//positions[c] = new PositionInMaze(0, 0);
		if (!contains.contains(listener)) {
			listeners[c] = listener;
			contains.add(listener);
			//System.out.println(contains.size());
		}
		
		//count++;
		return c;
	}

	public void updatePos(int position) throws RemoteException {
		//positions[playerID] = pos;

		int id = (position >> 16) & 65535;
		int x = (position >> 8) & 255;
		int y = position & 255;
		
		while (locked) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		updatedPositions.put(id, new PositionInMaze(x, y));
	}

	/*public PositionInMaze[] getPositions(int playerID) throws RemoteException {
		PositionInMaze[] result = new PositionInMaze[count];
		int c = 0;
		for (int i = 0; i < positions.length; i++) {
			if (positions[i] != null && i != playerID) {
				result[c] = positions[i];
				c++;
			}
		}
		return result;
	}*/
}
