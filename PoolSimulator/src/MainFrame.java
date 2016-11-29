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

	private GamePanel g = new GamePanel(this);
	private UserPanel[] players = new UserPanel[] {
		new UserPanel("Player 1", true), 
		new UserPanel("Player 2", false)
	};
	private int currentPlayer = 0;
	private boolean[] keyPressed = new boolean[2];

	MainFrame () throws InterruptedException {
		super("Pool Simulator");
		
		Consumer<String> consumer = (x) -> processInput(x);
		new SerialReader("COM3", consumer);
		
		addKeyListener(this);
		setSize(900, 950);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
	
	public void switchTurns () {
		players[0].toggleIsPlaying();
		players[1].toggleIsPlaying();
		currentPlayer ^= 1;
	}
	
	public void addBall (Ball b, boolean isBreak) {
		players[currentPlayer].addBall(b);
		if (players[currentPlayer].getType() == UserPanel.NONE_ID && !isBreak) {
			players[currentPlayer].setType(b.getType());
			players[currentPlayer ^ 1].setType(b.getType() == UserPanel.STRIPED_ID ? UserPanel.SOLID_ID : UserPanel.STRIPED_ID);
		}
		
		if (!g.hasUnsunkType(players[currentPlayer].getType()))
			players[currentPlayer].setType(UserPanel.BLACK_ID);
		if (!g.hasUnsunkType(players[currentPlayer ^ 1].getType()))
			players[currentPlayer ^ 1].setType(UserPanel.BLACK_ID);
	}
	
	public boolean isScratch (Ball b) {
		int type = 0;
		if (b.isBlack())
			type = UserPanel.BLACK_ID;
		else if (b.isStriped())
			type = UserPanel.STRIPED_ID;
		else if (b.isSolid())
			type = UserPanel.SOLID_ID;
		return players[currentPlayer].getType() != type && players[currentPlayer].getType() != 0;
	}
	
	public void reset () {
		players[0].reset();
		players[1].reset();
		players[0].setIsPlaying(true);
		players[1].setIsPlaying(false);
		g.reset();
	}
	
	private void processInput (String s) {
		StringTokenizer st = new StringTokenizer(s);
		String cmd = st.nextToken();

		double val;
		switch (cmd) {
			case "RESET_GAME":
				reset();
				break;
			case "CHANGE_ANGLE":
				val = Integer.parseInt(st.nextToken());
				val = (val - 2048) / 2048.0;
				if (Math.abs(val) > 0.05)
					g.changeDirectionAngle((val - Math.signum(val) * 0.05) / 50.0);
				break;
			case "SHOT":
				val = Double.parseDouble(st.nextToken());
				g.setVelocity(val);
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
