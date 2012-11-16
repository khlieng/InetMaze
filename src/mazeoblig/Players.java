package mazeoblig;

import java.rmi.RemoteException;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import simulator.PositionInMaze;

public class Players extends UnicastRemoteObject implements PlayersInterface {
	private static PositionInMaze[] positions;
	private static UpdateListener[] listeners;
	private static ConcurrentHashMap<Integer, PositionInMaze> updatedPositions;
	private static int count;
	private static int elapsed;
	private ArrayList<UpdateListener> contains = new ArrayList<UpdateListener>();
		
	protected Players() throws RemoteException {
		positions = new PositionInMaze[Maze.CLIENTS + 1];
		listeners = new UpdateListener[Maze.CLIENTS + 1];
		updatedPositions = new ConcurrentHashMap<Integer, PositionInMaze>();
		
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					elapsed += 10;
					if (elapsed >= 500) {
						elapsed -= 500;
						for (int i = 0; i < listeners.length; i++) {
							if (listeners[i] != null) {
								try {
									listeners[i].pushPositions(updatedPositions);
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
						}
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

	public int join(UpdateListener listener) throws RemoteException {
		int i = 0;
		while (positions[i] != null) {
			i++;
		}
		positions[i] = new PositionInMaze(0, 0);
		if (!contains.contains(listener)) {
			listeners[i] = listener;
			contains.add(listener);
			System.out.println(contains.size());
		}
		count++;
		return i;
	}

	public void updatePos(int playerID, PositionInMaze pos) throws RemoteException {
		positions[playerID] = pos;
		updatedPositions.put(playerID, pos);
	}

	public PositionInMaze[] getPositions(int playerID) throws RemoteException {
		PositionInMaze[] result = new PositionInMaze[count];
		int c = 0;
		for (int i = 0; i < positions.length; i++) {
			if (positions[i] != null && i != playerID) {
				result[c] = positions[i];
				c++;
			}
		}
		return result;
	}
}
