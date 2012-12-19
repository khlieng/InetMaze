package mazeoblig;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.Timer;

import simulator.PositionInMaze;
import simulator.VirtualUser;

public class MazeDummy {
	private static BoxMazeInterface bm;
	private static Box[][] maze;
	public static int DIM = 10;
	private static int dim = DIM;
	
	public static PlayersInterface Players;
	
	private static String server_hostname;
	private static int server_portnumber;
	
	private static int nClients = Maze.CLIENTS;
	private static Dummy[] dummies;
	
	public static PositionInMaze[][][] PathPool;
	public static int PoolSize = 4096;
	
	public static void main(String[] args) {
		int size = dim;
		/*
		 ** Kobler opp mot RMIServer, under forutsetning av at disse
		 ** kjører på samme maskin. Hvis ikke må oppkoblingen
		 ** skrives om slik at dette passer med virkeligheten.
		 */
		if (server_hostname == null)
			server_hostname = RMIServer.getHostName();
		if (server_portnumber == 0)
			server_portnumber = RMIServer.getRMIPort();
		try {
			java.rmi.registry.Registry r = java.rmi.registry.LocateRegistry.
			getRegistry(server_hostname,
					server_portnumber);

			UpdateListener client = new UpdateListener() {
				public void pushPositions(int[] updatedPositions) throws RemoteException {					
				}
			};
			UnicastRemoteObject.exportObject(client, 0);
			
			/*
			 ** Henter inn referansen til Labyrinten (ROR)
			 */
			bm = (BoxMazeInterface) r.lookup(RMIServer.MazeName);
			maze = bm.getMaze();
			
			Players = (PlayersInterface)r.lookup("Players");
			
			PathPool = new PositionInMaze[PoolSize][2][256];
			for (int i = 0; i < PoolSize; i++) {
				VirtualUser vu = new VirtualUser(maze);
				PathPool[i][0] = vu.getFirstIterationLoop();
				PathPool[i][1] = vu.getIterationLoop();
			}
			
			dummies = new Dummy[nClients];
			for (int i = 0; i < nClients; i++) {
				//VirtualUser vu = new VirtualUser(maze);
				dummies[i] = new Dummy(client);
			}
			
			while (true) {
				for (Dummy d : dummies) {
					d.update(10);
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
		}
		catch (RemoteException e) {
			System.err.println("Remote Exception: " + e.getMessage());
			System.exit(0);
		}
		catch (NotBoundException f) {
			/*
			 ** En exception her er en indikasjon på at man ved oppslag (lookup())
			 ** ikke finner det objektet som man søker.
			 ** Årsaken til at dette skjer kan være mange, men vær oppmerksom på
			 ** at hvis hostname ikke er OK (RMIServer gir da feilmelding under
			 ** oppstart) kan være en årsak.
			 */
			System.err.println("Not Bound Exception: " + f.getMessage());
			System.exit(0);
		}
	}
}
