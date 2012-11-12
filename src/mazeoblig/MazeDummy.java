package mazeoblig;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.Timer;

import simulator.PositionInMaze;
import simulator.VirtualUser;

public class MazeDummy {
	private static BoxMazeInterface bm;
	private static Box[][] maze;
	public static int DIM = 10;
	private static int dim = DIM;
	
	private static PlayersInterface players;
	
	private static String server_hostname;
	private static int server_portnumber;
	
	private static int nClients = 15000;
	private static Dummy[] dummies;
	
	public static void main(String[] args) {
		int size = dim;
		/*
		 ** Kobler opp mot RMIServer, under forutsetning av at disse
		 ** kj�rer p� samme maskin. Hvis ikke m� oppkoblingen
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

			/*
			 ** Henter inn referansen til Labyrinten (ROR)
			 */
			bm = (BoxMazeInterface) r.lookup(RMIServer.MazeName);
			maze = bm.getMaze();
			
			players = (PlayersInterface)r.lookup("Players");
			
			dummies = new Dummy[nClients];
			for (int i = 0; i < nClients; i++) {
				VirtualUser vu = new VirtualUser(maze);
				dummies[i] = new Dummy(vu, players);
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
			 ** En exception her er en indikasjon p� at man ved oppslag (lookup())
			 ** ikke finner det objektet som man s�ker.
			 ** �rsaken til at dette skjer kan v�re mange, men v�r oppmerksom p�
			 ** at hvis hostname ikke er OK (RMIServer gir da feilmelding under
			 ** oppstart) kan v�re en �rsak.
			 */
			System.err.println("Not Bound Exception: " + f.getMessage());
			System.exit(0);
		}
	}
}