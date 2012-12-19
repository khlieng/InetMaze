package mazeoblig;

import java.rmi.RemoteException;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import simulator.PositionInMaze;

public class Players extends UnicastRemoteObject implements PlayersInterface {
	private static UpdateListener[] listeners;
	private static HashMap<Integer, PositionInMaze> updatedPositions;
	private static int elapsed;
	private static Lock l = new ReentrantLock();
	
	protected Players() throws RemoteException {
		listeners = new UpdateListener[Maze.CLIENTS + 1];
		updatedPositions = new HashMap<Integer, PositionInMaze>();
		
		// Tråden der oppdateringer skyves ut
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					long t = System.currentTimeMillis();
					if (elapsed >= Maze.SRV_UPDATERATE) {
						elapsed -= Maze.SRV_UPDATERATE;
						try {
							Set<Entry<Integer, PositionInMaze>> entries = null;
							l.lock();
							try {
								HashMap<Integer, PositionInMaze> copy = new HashMap<Integer, PositionInMaze>(updatedPositions);
								entries = copy.entrySet();
							} finally {
								l.unlock();
							}
							if (entries != null) {
								int n = entries.size(), i = 0;
								if (n > 0) {
									int[] positions = new int[n];
									for (Entry<Integer, PositionInMaze> entry : entries) {
										int id = entry.getKey() << 16;
										int x = entry.getValue().getXpos() << 8;
										int y = entry.getValue().getYpos();
										positions[i] = id | x | y;
										i++;
									}
									try {
										for (int j = 0; j < listeners.length; j++) {
											if (listeners[j] != null)
												listeners[j].pushPositions(positions);
										}
									} catch (RemoteException e) {
										e.printStackTrace();
									}
									updatedPositions.clear();
								}
							}
						} catch(Exception e) {
							System.out.println(e.getMessage());
						}
					}
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}					

					elapsed += System.currentTimeMillis() - t;
				}
			}
		}).start();
	}

	int c = -1;
	public int join(UpdateListener listener) throws RemoteException {
		c++;
		listeners[c] = listener;
		return c;
	}

	public void updatePos(int position) throws RemoteException {
		int id = (position >> 16) & 65535;
		int x = (position >> 8) & 255;
		int y = position & 255;
		
		l.lock();
		try {
			updatedPositions.put(id, new PositionInMaze(x, y));
		} finally {
			l.unlock();
		}
	}
}
