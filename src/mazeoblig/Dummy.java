package mazeoblig;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Random;
import simulator.*;

public class Dummy {
	private static Random rand = new Random();

	private short playerID;
	private short currentTime;
	private short moveTime;
	private short initialPath;
	private short currentPos;
	private boolean first = true;
	
	public Dummy() {
		initialPath = (short)rand.nextInt(MazeDummy.PoolSize);
		
		try {
			UpdateListener listener = new UpdateListener() {
				public void pushPositions(int[] updatedPositions) throws RemoteException {
					// Tar ikke vare på posisjonene her for å spare minne
				}
			};
			UnicastRemoteObject.exportObject(listener, 0);
			
			playerID = (short)(MazeDummy.Players.join(listener) - (Short.MAX_VALUE + 1));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void update(int time) {
		currentTime += time;		
		if (currentTime >= moveTime) {
			currentTime -= moveTime;
			moveTime = (short)(Maze.DUMMY_WAIT_MIN + rand.nextInt(Maze.DUMMY_WAIT_MAX - Maze.DUMMY_WAIT_MIN));
			currentPos++;
			if (!first) {
				if (currentPos >= MazeDummy.Path.length) {
					currentPos = 0;
				}
				try {
					int id = (playerID + Short.MAX_VALUE + 1) << 16;
					int x = MazeDummy.Path[currentPos].getXpos() << 8;
					int y = MazeDummy.Path[currentPos].getYpos();
					int position = id | x | y;
					MazeDummy.Players.updatePos(position);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			else {
				if (currentPos >= MazeDummy.InitialPathPool[initialPath].length) {
					currentPos = 0;
					first = false;
				}
				try {
					int id = (playerID + Short.MAX_VALUE + 1) << 16;
					int x = MazeDummy.InitialPathPool[initialPath][currentPos].getXpos() << 8;
					int y = MazeDummy.InitialPathPool[initialPath][currentPos].getYpos();
					int position = id | x | y;
					MazeDummy.Players.updatePos(position);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
