package mazeoblig;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Random;

import simulator.*;

public class Dummy {
	private static Random rand = new Random();
	//private PlayersInterface players;
	//private PositionInMaze[] firstPath;
	//private PositionInMaze[] path;
	private short currentTime;
	private short moveTime;
	private short path;
	private short currentPos;
	private boolean first = true;
	private short playerID;
	//private PositionInMaze[] positions;
	
	public Dummy(UpdateListener listener) {
		//this.players = players;
		//firstPath = user.getFirstIterationLoop();
		//path = user.getIterationLoop();
		path = (short)rand.nextInt(MazeDummy.PoolSize);
		
		//positions = new PositionInMaze[Maze.CLIENTS + 1];
		
		try {
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
				if (currentPos >= MazeDummy.PathPool[path][1].length) {
					currentPos = 0;
				}
				try {
					int id = (playerID + Short.MAX_VALUE + 1) << 16;
					int x = MazeDummy.PathPool[path][1][currentPos].getXpos() << 8;
					int y = MazeDummy.PathPool[path][1][currentPos].getYpos();
					int position = id | x | y;
					MazeDummy.Players.updatePos(position);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			else {
				if (currentPos >= MazeDummy.PathPool[path][0].length) {
					currentPos = 0;
					first = false;
				}
				try {
					int id = (playerID + Short.MAX_VALUE + 1) << 16;
					int x = MazeDummy.PathPool[path][0][currentPos].getXpos() << 8;
					int y = MazeDummy.PathPool[path][0][currentPos].getYpos();
					int position = id | x | y;
					MazeDummy.Players.updatePos(position);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
