package mazeoblig;

import java.rmi.*;
import simulator.*;

public interface PlayersInterface extends Remote {
	int join(UpdateListener listener) throws RemoteException;
	void updatePos(int playerID, PositionInMaze pos) throws RemoteException;
	PositionInMaze[] getPositions(int playerID) throws RemoteException; 
}
