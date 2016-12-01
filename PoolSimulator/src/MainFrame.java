/*
 * Wrapper for GamePanel and UserPanel
 */

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.StringTokenizer;
import java.util.function.Consumer;

import javax.swing.*;

public class MainFrame extends JFrame implements KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2269971701250845501L;
	private static final double FRAMES_PER_SEC = 500;
	private static final double SECS_PER_FRAME = 1000 / FRAMES_PER_SEC;

	private GamePanel g;
	private UserPanel[] players = new UserPanel[] {
		new UserPanel("Player 1", true), 
		new UserPanel("Player 2", false)
	};
	private boolean[] keyPressed = new boolean[2];
	
	public SerialCommunicator sc;

	MainFrame () throws InterruptedException {
		super("Pool Simulator");
		
		// initializing new serial communicator that invokes function
		// processInput whenever new information is passed from tiva
		Consumer<String> consumer = (x) -> processInput(x);
		sc = new SerialCommunicator("COM3", consumer);
		g = new GamePanel(this);
		
		addKeyListener(this);
		
		setSize(900, 950);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Setting the layout using GridBagLayout
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 2;
		c.fill = GridBagConstraints.VERTICAL;
		contentPane.add(g, c);
		c.gridx = 1;
		c.gridy = 0;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		contentPane.add(players[0], c);
		c.gridx = 1;
		c.gridy = 1;
		contentPane.add(players[1], c);
		
		setVisible(true);

		// Main game loop that handles each game tick
		int time = 0;
		while (true) {
			if (time > SECS_PER_FRAME) {
				g.update();
				g.revalidate();
				repaint();
				time = 0;
			} else {
				time++;
			}
			Thread.sleep(1);
		}
	}
	
	/**
	 * 
	 * @return current player
	 */
	public int getCurrentPlayer () {
		return players[0].isPlaying() ? 0 : 1;
	}
	
	/**
	 * 
	 * @return current player object
	 */
	public UserPanel getCurrentPlayerObject () {
		return players[0].isPlaying() ? players[0] : players[1];
	}
	
	/**
	 * Switches the active player.
	 */
	public void switchTurns () {
		players[0].toggleIsPlaying();
		players[1].toggleIsPlaying();
	}
	
	/**
	 * Adds a ball to a user's sunken balls.
	 * @param b ball to add
	 * @param isBreak true if the shot was a break
	 */
	public void addBall (Ball b, boolean isBreak) {
		players[getCurrentPlayer()].addBall(b);
		if (players[getCurrentPlayer()].getType() == UserPanel.NONE_ID && !isBreak) {
			players[getCurrentPlayer()].setType(b.getType());
			players[getCurrentPlayer() ^ 1].setType(b.getType() == UserPanel.STRIPED_ID ? UserPanel.SOLID_ID : UserPanel.STRIPED_ID);
		}
		
		if (!g.hasUnsunkType(players[getCurrentPlayer()].getType()))
			players[getCurrentPlayer()].setType(UserPanel.EIGHT_ID);
		if (!g.hasUnsunkType(players[getCurrentPlayer() ^ 1].getType()))
			players[getCurrentPlayer() ^ 1].setType(UserPanel.EIGHT_ID);
	}
	
	/**
	 * 
	 * @param b ball to check if scratch
	 * @return true if hitting Ball b results in a scratch for the active player
	 */
	public boolean isScratch (Ball b) {
		if (b.isEight() && players[getCurrentPlayer()].getType() == UserPanel.NONE_ID)
			return true;
		int type = 0;
		if (b.isEight())
			type = UserPanel.EIGHT_ID;
		else if (b.isStriped())
			type = UserPanel.STRIPED_ID;
		else if (b.isSolid())
			type = UserPanel.SOLID_ID;
		return players[getCurrentPlayer()].getType() != type && players[getCurrentPlayer()].getType() != 0;
	}
	
	/**
	 * Resets the game and user information.
	 */
	public void reset () {
		players[0].reset();
		players[1].reset();
		players[0].setIsPlaying(true);
		players[1].setIsPlaying(false);
		g.reset();
	}
	
	/**
	 * Processes the string passed by the Tiva and executes the appropriate action.
	 * @param s string passed by Tiva
	 */
	private void processInput (String s) {
		StringTokenizer st = new StringTokenizer(s);
		String cmd = st.nextToken();

		double val;
		switch (cmd) {
			case "RESET_GAME":
				reset();
				break;
			case "CHANGE_ANGLE":
				val = Double.parseDouble(st.nextToken());
				val = (val - 2048) / 2048.0;
				if (Math.abs(val) > 0.05)
					g.changeDirectionAngle((val - Math.signum(val) * 0.05) / 50.0);
				break;
			case "SHOT":
				// Can only shoot when the game state is ready for a shot
				if (g.getState() != GamePanel.GameState.PLAY)
					break;
				val = Double.parseDouble(st.nextToken());
				g.setVelocity(val);
				break;
			case "CHANGE_POSITION":
				// Can only change the position of the cue ball when placing the ball after a scratch
				if (g.getState() != GamePanel.GameState.PLACING_BALL)
					break;
				g.changeCuePosition(Double.parseDouble(st.nextToken()), Double.parseDouble(st.nextToken()));
				break;
			case "DROP":
				// Can only drop the cue ball when placing the ball after a scratch
				if (g.getState() != GamePanel.GameState.PLACING_BALL)
					break;
				if (g.isCuePositionOccupied()) {
					sc.println("<BALL_IN_HAND");
					break; 
				}
					
				g.setGameState(GamePanel.GameState.PLAY);
				break;
			case "CHANGE_POCKET":
				int pocket = Integer.parseInt(st.nextToken());
				if (getCurrentPlayerObject().getType() != UserPanel.EIGHT_ID)
					break;
				g.setCalledPocketId(pocket);
				break;
			case "SET_POCKET":
				g.setIsPocketCalled(true);
				break;
		}
	}
	
	@Override
	public void keyPressed (KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT)
			keyPressed[0] = true;
		if (keyCode == KeyEvent.VK_RIGHT)
			keyPressed[1] = true;

		if (keyPressed[0]) {
			g.changeDirectionAngle(-0.02);
		}
		if (keyPressed[1]) {
			g.changeDirectionAngle(+0.02);
		}
	}

	@Override
	public void keyReleased (KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT)
			keyPressed[0] = false;
		if (keyCode == KeyEvent.VK_RIGHT)
			keyPressed[1] = false;
		if (keyCode == KeyEvent.VK_SPACE)
			g.setVelocity(10);
	}

	@Override
	public void keyTyped (KeyEvent e) {
	}
}
