package mazeoblig;

import java.rmi.RemoteException;
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
	
	public Dummy(VirtualUser user, PlayersInterface players) {
		this.players = players;
		firstPath = user.getFirstIterationLoop();
		path = user.getIterationLoop();
		try {
			playerID = players.join();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public void update(int time) {
		currentTime += time;
		if (currentTime >= moveTime) {
			currentTime -= moveTime;
			moveTime = 200 + rand.nextInt(2000);
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
