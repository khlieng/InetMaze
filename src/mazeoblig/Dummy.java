package mazeoblig;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Random;

import simulator.*;

public class Dummy {
	private static Random rand = new Random();
	private PlayersInterface players;
	private PositionInMaze[] firstPath;
	private PositionInMaze[] path;
	private int currentTime;
	private int moveTime;
	private int currentPos;
	private boolean first = true;
	private int playerID;
	//private PositionInMaze[] positions;
	
	public Dummy(VirtualUser user, PlayersInterface players, UpdateListener listener) {
		this.players = players;
		firstPath = user.getFirstIterationLoop();
		path = user.getIterationLoop();
		//positions = new PositionInMaze[Maze.CLIENTS + 1];
		
		try {
			playerID = players.join(listener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void update(int time) {
		currentTime += time;		
		if (currentTime >= moveTime) {
			currentTime -= moveTime;
			moveTime = 100 + rand.nextInt(900);
			currentPos++;
			if (!first) {
				if (currentPos >= path.length) {
					currentPos = 0;
				}
				try {
					players.updatePos(playerID, path[currentPos]);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			else {
				if (currentPos >= firstPath.length) {
					currentPos = 0;
					first = false;
				}
				try {
					players.updatePos(playerID, firstPath[currentPos]);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
