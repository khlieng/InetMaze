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

	public static int CLIENTS = 1000;
	public static int SRV_UPDATERATE = 50;
	public static int DUMMY_WAIT_MIN = 200;
	public static int DUMMY_WAIT_MAX = 1000;
	
	private BoxMazeInterface bm;
	private Box[][] maze;
	public static int DIM = 50;
	private int dim = DIM;

	private PlayersInterface players;
	static int xp;
	static int yp;
	static boolean found = false;

	private String server_hostname;
	private int server_portnumber;
	private int playerID;

	private PositionInMaze[] pos;
	private PositionInMaze[] otherPlayers;
	private byte[][] map;

	/**
	 * Henter labyrinten fra RMIServer
	 */
	public Maze() {
		super("Maze");
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
			
			map = new byte[50][50];

			otherPlayers = new PositionInMaze[Maze.CLIENTS + 1];
			for (int i = 0; i < otherPlayers.length; i++) {
				otherPlayers[i] = new PositionInMaze(0, 0);
			}
			
			UpdateListener client = new UpdateListener() {
				public void pushPositions(int[] updatedPositions) throws RemoteException {
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

/*
			addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_A) {
						xp--;
					}
					else if (e.getKeyCode() == KeyEvent.VK_D) {
						xp++;
					}
					
					if (e.getKeyCode() == KeyEvent.VK_W) {
						yp--;
					}
					else if (e.getKeyCode() == KeyEvent.VK_S) {
						yp++;
					}
					try {
						players.updatePos(playerID, new PositionInMaze(xp, yp));
					} catch (RemoteException e1) {
						e1.printStackTrace();
					}
					repaint();
				}

				public void keyReleased(KeyEvent arg0) {}
				public void keyTyped(KeyEvent arg0) {}
			});
			new Timer(100, new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					repaint();
				}
			}).start();*/
			
/*
** Finner løsningene ut av maze - se forøvrig kildekode for VirtualMaze for ytterligere
** kommentarer. Løsningen er implementert med backtracking-algoritme
*/
			VirtualUser vu = new VirtualUser(maze);
			
			pos = vu.getFirstIterationLoop();

			for (int i = 0; i < pos.length; i++)
				System.out.println(pos[i]);
			System.out.println("---");
			pos = vu.getIterationLoop();
			for (int i = 0; i < pos.length; i++)
				System.out.println(pos[i]);
/**/			
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

	int size = 10;
	public void render(GameContainer container, Graphics g) {
		int x, y;
		
		g.setColor(new Color(64, 128, 64));
		for (int i = 0; i < pos.length - 1; i++) {
			g.drawLine(pos[i].getXpos() * size + size / 2, pos[i].getYpos() * size + size / 2, pos[i+1].getXpos() * size + size / 2, pos[i+1].getYpos() * size + size / 2);
		}
		
		for (x = 0; x < DIM; x++) {
			for (y = 0; y < DIM; y++) {
				map[x][y] = 0;
			}
		}
		
		g.setColor(Color.yellow);
		for (PositionInMaze p : otherPlayers) {
			if (p != null && map[p.getXpos()][p.getYpos()] == 0) {
				map[p.getXpos()][p.getYpos()] = 1;
				g.fillOval(p.getXpos() * size + 1, p.getYpos() * size + 1, 3, 3);
			}
		}
		
		g.setColor(Color.red);
		g.fillOval(xp * size + 1, yp * size + 1, 4, 4);
		
		// Tegner baser på box-definisjonene ....
		g.setColor(Color.darkGray);
		for (x = 1; x < (dim - 1); ++x)
			for (y = 1; y < (dim - 1); ++y) {
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
	/**
	 * Viser labyrinten / tegner den i applet
	 * @param g Graphics
	 */
	/*public void paint (Graphics g) {
		int x, y;
		
		g.setColor(Color.GREEN);		
		for (int i = 0; i < pos.length - 1; i++) {
			g.drawLine(pos[i].getXpos() * 10 + 5, pos[i].getYpos() * 10 + 5, pos[i+1].getXpos() * 10 + 5, pos[i+1].getYpos() * 10 + 5);
		}
		
		try {
			PositionInMaze[] otherPlayers = players.getPositions(playerID);
			g.setColor(Color.PINK);
			for (PositionInMaze p : otherPlayers) {
				if (p != null) {
				g.fillOval(p.getXpos() * 10 + 2, p.getYpos() * 10 + 2, 6, 6);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		g.setColor(Color.RED);
		g.fillOval(xp * 10, yp * 10, 10, 10);
		
		// Tegner baser på box-definisjonene ....
		g.setColor(Color.BLACK);
		for (x = 1; x < (dim - 1); ++x)
			for (y = 1; y < (dim - 1); ++y) {
				if (maze[x][y].getUp() == null)
					g.drawLine(x * 10, y * 10, x * 10 + 10, y * 10);
				if (maze[x][y].getDown() == null)
					g.drawLine(x * 10, y * 10 + 10, x * 10 + 10, y * 10 + 10);
				if (maze[x][y].getLeft() == null)
					g.drawLine(x * 10, y * 10, x * 10, y * 10 + 10);
				if (maze[x][y].getRight() == null)
					g.drawLine(x * 10 + 10, y * 10, x * 10 + 10, y * 10 + 10);
			}
	}*/

	@Override
	public void init(GameContainer arg0) throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(GameContainer arg0, int arg1) throws SlickException {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		try {
			AppGameContainer app = new AppGameContainer(new Maze());
			app.setDisplayMode(500, 500, false);
			//app.setShowFPS(false);
			app.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
}

