package mazeoblig;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.applet.*;

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

import javax.swing.JComboBox.KeySelectionManager;
import javax.swing.Timer;
/**
 * Tegner opp maze i en applet, basert på definisjon som man finner på RMIServer
 * RMIServer på sin side  henter størrelsen fra definisjonen i Maze
 * @author asd
 *
 */
public class Maze extends Applet {

	private BoxMazeInterface bm;
	private Box[][] maze;
	public static int DIM = 80;
	private int dim = DIM;

	private PlayersInterface players;
	static int xp;
	static int yp;
	static boolean found = false;

	private String server_hostname;
	private int server_portnumber;
	private int playerID;

	private PositionInMaze[] pos;

	/**
	 * Henter labyrinten fra RMIServer
	 */
	public void init() {
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

			/*
			 ** Henter inn referansen til Labyrinten (ROR)
			 */
			bm = (BoxMazeInterface) r.lookup(RMIServer.MazeName);
			maze = bm.getMaze();
			
			players = (PlayersInterface)r.lookup("Players");
			playerID = players.join();
			
			System.out.println("ID: " + playerID);
			
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
			}).start();
			
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

	//Get a parameter value
	public String getParameter(String key, String def) {
		return getParameter(key) != null ? getParameter(key) : def;
	}
	//Get Applet information
	public String getAppletInfo() {
		return "Applet Information";
	}

	//Get parameter info
	public String[][] getParameterInfo() {
		java.lang.String[][] pinfo = { {"Size", "int", ""},
		};
		return pinfo;
	}

	/**
	 * Viser labyrinten / tegner den i applet
	 * @param g Graphics
	 */
	public void paint (Graphics g) {
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
	}
}

