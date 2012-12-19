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
	
	public static PlayersInterface Players;
	
	private static String server_hostname;
	private static int server_portnumber;
	
	private static int nClients = Maze.CLIENTS;
	private static Dummy[] dummies;
	
	public static PositionInMaze[][] InitialPathPool;
	public static PositionInMaze[] Path;
	public static int PoolSize = 1024;
	
	public static void main(String[] args) {
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
			getRegistry(server_hostname, server_portnumber);
			
			/*
			 ** Henter inn referansen til Labyrinten (ROR)
			 */
			bm = (BoxMazeInterface) r.lookup(RMIServer.MazeName);
			maze = bm.getMaze();
			
			Players = (PlayersInterface)r.lookup("Players");
			
			// Prekalkulerer her et visst antall veier ut labyrinten for å få ned minnebruk per klient
			InitialPathPool = new PositionInMaze[PoolSize][0];
			for (int i = 0; i < PoolSize; i++) {
				VirtualUser vu = new VirtualUser(maze);
				InitialPathPool[i] = vu.getFirstIterationLoop();
			}
			Path = new VirtualUser(maze).getIterationLoop();
			
			dummies = new Dummy[nClients];
			for (int i = 0; i < nClients; i++) {
				dummies[i] = new Dummy();
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
