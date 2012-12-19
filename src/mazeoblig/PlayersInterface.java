package mazeoblig;

import java.rmi.*;
import simulator.*;

public interface PlayersInterface extends Remote {
	int join(UpdateListener listener) throws RemoteException;
	void updatePos(int position) throws RemoteException;
	//PositionInMaze[] getPositions(int playerID) throws RemoteException; 
}
