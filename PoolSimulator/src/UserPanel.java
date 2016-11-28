import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;

import javax.swing.JPanel;


public class UserPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8005126132273764537L;
	public static final int WIDTH = 400;
	public static final int HEIGHT = 400;
	public static final Ball STRIPED = new Ball(0, 0, 0, Color.RED, Color.WHITE);
	public static final Ball SOLID = new Ball(0, 0, 0, Color.RED, Color.RED);
	public static final int STRIPED_ID = 1;
	public static final int SOLID_ID = -1;

	private String name;
	private int type;
	private boolean isPlaying;
	
	UserPanel (String name, boolean isPlaying) {
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.name = name;
		this.isPlaying = isPlaying;
	}

	@Override
	protected void paintComponent (Graphics g) {
		super.paintComponent(g);
		Font f = new Font("Arial", Font.BOLD, 48);
		Graphics2D g2d = (Graphics2D)g.create();
		g2d.setFont(f);
		FontMetrics fm = g2d.getFontMetrics();
		int x = (getWidth() - fm.stringWidth(this.name)) / 2;
		int y = (fm.getHeight()) / 2 + fm.getAscent();
		
		g2d.setColor(Color.BLACK);
		g2d.drawString(this.name, x, y);
		
		if (type == STRIPED_ID) {
			STRIPED.radius = fm.getHeight() / 2;
			STRIPED.pos.x = getWidth() - STRIPED.radius;
			STRIPED.pos.y = y - fm.getAscent() + STRIPED.radius;
			STRIPED.draw(g);
		} else {
			SOLID.radius = fm.getHeight() / 2;
			SOLID.pos.x = getWidth() - SOLID.radius;
			SOLID.pos.y = y - fm.getAscent() + SOLID.radius;
			SOLID.draw(g);
		}
		
		if (isPlaying) {
			g.setColor(Color.GREEN);
			g.fillPolygon(new Polygon(new int[]{25, 25, 60}, new int[]{30, 80, 55}, 3));
		}
		
		g2d.dispose();
	}
	
	public void setType (int type) {
		this.type = type;
	}
	
	public void toggleIsPlaying () {
		this.isPlaying = !this.isPlaying;
	}
}
