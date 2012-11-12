package mazeoblig;

import java.rmi.RemoteException;
import java.rmi.server.*;

import simulator.PositionInMaze;

public class Players extends UnicastRemoteObject implements PlayersInterface {
	private static PositionInMaze[] positions;
	private static int count;
	
	protected Players() throws RemoteException {
		positions = new PositionInMaze[150001];
	}

	public int join() throws RemoteException {
		int i = 0;
		while (positions[i] != null) {
			i++;
		}
		positions[i] = new PositionInMaze(0, 0);
		count++;
		return i;
	}

	public void updatePos(int playerID, PositionInMaze pos) throws RemoteException {
		positions[playerID] = pos;
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
