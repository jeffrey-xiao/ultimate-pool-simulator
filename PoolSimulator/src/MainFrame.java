import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

public class MainFrame extends JFrame implements KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2269971701250845501L;
	private static final double FRAMES_PER_SEC = 500;
	private static final double SECS_PER_FRAME = 1000 / FRAMES_PER_SEC;

	private GamePanel g = new GamePanel();
	private UserPanel player1 = new UserPanel("Player 1", true), player2 = new UserPanel("Player 2", false);
	private boolean[] keyPressed = new boolean[2];

	MainFrame () throws InterruptedException {
		super("Pool Simulator");
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
		contentPane.add(player1, c);
		c.gridx = 1;
		c.gridy = 1;
		contentPane.add(player2, c);

		player1.setType(UserPanel.STRIPED_ID);
		player2.setType(UserPanel.SOLID_ID);
		
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

	@Override
	public void keyPressed (KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT)
			keyPressed[0] = true;
		if (keyCode == KeyEvent.VK_RIGHT)
			keyPressed[1] = true;

		if (keyPressed[0]) {
			g.angle -= 0.02;
			g.r = 1 << 30;
		}
		if (keyPressed[1]) {
			g.angle += 0.02;
			g.r = 1 << 30;
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
		int keyCode = e.getKeyCode();
		System.out.println(keyCode);
	}
}
