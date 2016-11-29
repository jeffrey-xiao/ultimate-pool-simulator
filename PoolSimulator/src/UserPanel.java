import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;

import javax.swing.JPanel;


public class UserPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8005126132273764537L;
	
	public static final int WIDTH = 400;
	public static final int HEIGHT = 400;
	public static final int NONE_ID = 0;
	public static final int STRIPED_ID = 1;
	public static final int SOLID_ID = 2;
	public static final int BLACK_ID = 3;

	private String name;
	private Ball ballType;
	private int type;
	private boolean isPlaying;
	private ArrayList<Ball> sunkBalls;
	
	UserPanel (String name, boolean isPlaying) {
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.name = name;
		this.isPlaying = isPlaying;
		this.sunkBalls = new ArrayList<Ball>();
		this.ballType = new Ball(0, 0, 0, Color.BLACK, Color.BLACK);
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
		
		ballType.radius = fm.getHeight() / 2;
		ballType.pos.x = getWidth() - ballType.radius;
		ballType.pos.y = y - fm.getAscent() + ballType.radius;
		
		if (type == STRIPED_ID) {
			ballType.primary = Color.RED;
			ballType.secondary = Color.WHITE;
			ballType.draw(g);
		} else if (type == SOLID_ID) {
			ballType.primary = Color.RED;
			ballType.secondary = Color.RED;
			ballType.draw(g);
		} else if (type == BLACK_ID) {
			ballType.primary = Color.BLACK;
			ballType.secondary = Color.BLACK;
			ballType.draw(g);
		}
		
		for (int i = 0; i < sunkBalls.size(); i++) {
			sunkBalls.get(i).radius = 15;
			sunkBalls.get(i).pos = new Vector(120 + (i % 6) * 35, 115 + (i / 6) * 35);
			sunkBalls.get(i).draw(g);
		}
		
		if (isPlaying) {
			g.setColor(Color.GREEN);
			g.fillPolygon(new Polygon(new int[]{25, 25, 60}, new int[]{30, 80, 55}, 3));
		}
		
		g2d.dispose();
	}
	
	public void reset () {
		type = 0;
		sunkBalls.clear();
	}
	
	public void setType (int type) {
		this.type = type;
	}
	
	public void toggleIsPlaying () {
		this.isPlaying = !this.isPlaying;
	}
	
	public void setIsPlaying (boolean isPlaying) {
		this.isPlaying = isPlaying;
	}
	
	public void addBall (Ball b) {
		this.sunkBalls.add(b);
	}
	
	public int getType () {
		return this.type;
	}
}
