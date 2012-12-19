package mazeoblig;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import simulator.PositionInMaze;

public interface UpdateListener extends Remote {
	void pushPositions(int[] updatedPositions) throws RemoteException;
}
