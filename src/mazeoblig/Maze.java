package mazeoblig;


import simulator.*;

/**
 *
 * <p>Title: Maze</p>
 *
 * <p>Description: En enkel applet som viser den randomiserte labyrinten</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JComboBox.KeySelectionManager;
import javax.swing.Timer;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.*;
import org.newdawn.slick.command.KeyControl;
/**
 * Tegner opp maze i en applet, basert på definisjon som man finner på RMIServer
 * RMIServer på sin side  henter størrelsen fra definisjonen i Maze
 * @author asd
 *
 */
public class Maze extends BasicGame {

	public static int CLIENTS = 100; // Antall klienter som startes når MazeDummy kjøres
	public static int SRV_UPDATERATE = 50; // Hvor ofte serveren skyver ut oppdateringer i millisekunder
	public static int DUMMY_WAIT_MIN = 100; // Minimum tid det tar før en klient flytter seg
	public static int DUMMY_WAIT_MAX = 1000; // Maksimal tid det tar før en klient flytter seg
	
	private BoxMazeInterface bm;
	private Box[][] maze;
	public static int DIM = 100;

	private PlayersInterface players;
	static int xp;
	static int yp;

	private String server_hostname;
	private int server_portnumber;
	private int playerID;

	private PositionInMaze[] pathIndicator;
	private PositionInMaze[] otherPlayers;
	private byte[][] map;

	long prev;
	
	/**
	 * Henter labyrinten fra RMIServer
	 */
	public Maze() {
		super("Maze");
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
			
			map = new byte[DIM][DIM];

			otherPlayers = new PositionInMaze[Maze.CLIENTS + 1];
			for (int i = 0; i < otherPlayers.length; i++) {
				otherPlayers[i] = new PositionInMaze(0, 0);
			}
			
			UpdateListener client = new UpdateListener() {
				public void pushPositions(int[] updatedPositions) throws RemoteException {
					int known = 0;
					for (int i = 0; i < otherPlayers.length; i++) {
						if (otherPlayers[i].getXpos() != 0 && otherPlayers[i].getYpos() != 0) {
							known++;
						}
					}
					long time = System.currentTimeMillis();					
					System.out.println((time - prev) + " ms, changes: " + updatedPositions.length + ", known: " + known);					
					prev = time;
					
					for (int i = 0; i < updatedPositions.length; i++) {
						int id = (updatedPositions[i] >> 16) & 65535;
						int x = (updatedPositions[i] >> 8) & 255;
						int y = (updatedPositions[i]) & 255;
						if (id != playerID) {
							otherPlayers[id].setXPos(x);
							otherPlayers[id].setYPos(y);
						}
					}
				}
			};
			UnicastRemoteObject.exportObject(client, 0);
						
			/*
			 ** Henter inn referansen til Labyrinten (ROR)
			 */
			bm = (BoxMazeInterface) r.lookup(RMIServer.MazeName);
			maze = bm.getMaze();
						
			players = (PlayersInterface)r.lookup("Players");
			playerID = players.join(client);
			
			System.out.println("ID: " + playerID);

			VirtualUser vu = new VirtualUser(maze);
			pathIndicator = vu.getIterationLoop();
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
	
	public void keyPressed(int key, char c) {
		if (key == Keyboard.KEY_A) {
			xp--;
		}
		else if (key == Keyboard.KEY_D) {
			xp++;
		}
		
		if (key == Keyboard.KEY_W) {
			yp--;
		}
		else if (key == Keyboard.KEY_S) {
			yp++;
		}
		try {
			int id = playerID << 16;
			int x = xp << 8;
			int y = yp;
			int position = id | x | y;
			players.updatePos(position);
		} catch (RemoteException e1) {
			e1.printStackTrace();
		}
	}

	int size = 5;
	public void render(GameContainer container, Graphics g) {
		int x, y;
		
		g.setColor(new Color(64, 128, 64));
		for (int i = 0; i < pathIndicator.length - 1; i++) {
			g.drawLine(pathIndicator[i].getXpos() * size + size / 2, 
					pathIndicator[i].getYpos() * size + size / 2, 
					pathIndicator[i+1].getXpos() * size + size / 2, 
					pathIndicator[i+1].getYpos() * size + size / 2);
		}
		
		clearMap();
		
		g.setColor(Color.yellow);
		for (PositionInMaze p : otherPlayers) {
			// Tegner kun spilleren dersom det ikke allerede har blitt tegnet en annen spliller i samme posisjon
			if (p != null && map[p.getXpos()][p.getYpos()] == 0) {
				map[p.getXpos()][p.getYpos()] = 1;
				g.fillOval(p.getXpos() * size + 1, p.getYpos() * size + 1, 3, 3);
			}
		}
		
		g.setColor(Color.red);
		g.fillOval(xp * size + 1, yp * size + 1, 4, 4);
		
		g.setColor(Color.darkGray);
		for (x = 1; x < (DIM - 1); ++x)
			for (y = 1; y < (DIM - 1); ++y) {
				if (maze[x][y].getUp() == null)
					g.drawLine(x * size, y * size, x * size + size, y * size);
				if (maze[x][y].getDown() == null)
					g.drawLine(x * size, y * size + size, x * size + size, y * size + size);
				if (maze[x][y].getLeft() == null)
					g.drawLine(x * size, y * size, x * size, y * size + size);
				if (maze[x][y].getRight() == null)
					g.drawLine(x * size + size, y * size, x * size + size, y * size + size);
			}
	}
	
	private void clearMap() {
		for (int x = 0; x < DIM; x++) {
			for (int y = 0; y < DIM; y++) {
				map[x][y] = 0;
			}
		}
	}

	@Override
	public void init(GameContainer arg0) throws SlickException {
	}

	@Override
	public void update(GameContainer arg0, int arg1) throws SlickException {
	}
	
	public static void main(String[] args) {
		try {
			AppGameContainer app = new AppGameContainer(new Maze());
			app.setDisplayMode(DIM * 5, DIM * 5, false);
			app.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
}

