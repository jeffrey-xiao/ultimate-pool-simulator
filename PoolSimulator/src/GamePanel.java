/**
 * Object representing the main game panel (the pool table).
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class GamePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1552746400473185110L;

	/**
	 * Enumeration describing all the different game states:
	 *  - PLAY: the current player can hit the ball.
	 *  - NO_SCRATCH_PLAYED: the current player hit the ball and did not scratch
	 *  - PLAYED: the current player hit the hit
	 *  - BALL_IN_HAND: the current player has scratched or hit the cue ball in
	 *  - PLACING_BALL: the current player is placing the ball after a scratch
	 *  - GAME_OVER: the game has ended
	 */
	public enum GameState {
		PLAY, NO_SCRATCH_PLAYED, CONTINUE_PLAYED, PLAYED, BALL_IN_HAND, PLACING_BALL, GAME_OVER
	}

	private static final Object lock = new Object();

	public static final double METER_TO_PIXEL = (800 / 2.84);
	public static final double POCKET_WIDTH_RATIO = 1.9;
	public static final int TABLE_WIDTH = (int) (1.624 * METER_TO_PIXEL);
	public static final int TABLE_HEIGHT = (int) (3.048 * METER_TO_PIXEL);
	public static final int PLAY_WIDTH = (int) (1.42 * METER_TO_PIXEL);
	public static final int PLAY_HEIGHT = (int) (2.84 * METER_TO_PIXEL);
	public static final int BALL_RADIUS = (int) (0.04615 * METER_TO_PIXEL);
	
	public static final double FRICTION = 0.0075;
	public static final double ROLLING_FRICTION = 0.005;
	public static final double BALL_RESISTITION = 1.0;
	public static final double WALL_RESISTITION = 0.5;
	
	public static final double EPS = 1e-6;

	// labeled from left to right, top to bottom
	private Ball[] p = new Ball[6];

	// 0 = cue ball
	Ball[] b = new Ball[16];

	// borders labeled from top to bottom, left to right
	private Line[] borders = new Line[6];

	private MainFrame parent;
	private GameState state;

	// angle of the direction vector
	private double angle = Math.PI / 2 * 3;
	// length of the direction vector
	private double r = 1 << 30;
	// if the current shot is a break
	private boolean isBreak;

	GamePanel (MainFrame parent) {
		setPreferredSize(new Dimension(TABLE_WIDTH, TABLE_HEIGHT));
		initialize();
		this.parent = parent;
		this.state = GameState.PLAY;
	}

	/**
	 * Resets the game and notifies tiva of the current player using serial port.
	 */
	public void reset () {
		initialize();
		parent.sc.println("<CURRENT_PLAYER " + parent.getCurrentPlayer());
	}

	/**
	 * Initializes the game.
	 */
	public void initialize () {
		isBreak = true;
		state = GameState.PLAY;
		double widthGap = TABLE_WIDTH - PLAY_WIDTH;
		double heightGap = TABLE_HEIGHT - PLAY_HEIGHT;

		int radius = (int) (widthGap / POCKET_WIDTH_RATIO);
		double dx = widthGap / 6 + radius;
		double dy = heightGap / 6 + radius;
		double offset = BALL_RADIUS;
		
		// Initializing the pockets
		p[0] = new Ball(dx, dy, radius, Color.BLACK, Color.BLACK);
		p[1] = new Ball(TABLE_WIDTH - dx, dy, radius, Color.BLACK, Color.BLACK);
		p[2] = new Ball(dx - offset, TABLE_HEIGHT / 2, radius, Color.BLACK, Color.BLACK);
		p[3] = new Ball(TABLE_WIDTH - dx + offset, TABLE_HEIGHT / 2, radius, Color.BLACK, Color.BLACK);
		p[4] = new Ball(dx, TABLE_HEIGHT - dy, radius, Color.BLACK, Color.BLACK);
		p[5] = new Ball(TABLE_WIDTH - dx, TABLE_HEIGHT - dy, radius, Color.BLACK, Color.BLACK);

		// initializing the borders
		borders[0] = new Line(p[0].pos.x + radius, heightGap / 2, p[1].pos.x - radius, heightGap / 2);
		borders[1] = new Line(p[0].pos.x, p[0].pos.y + radius, p[2].pos.x + offset, p[2].pos.y - radius * 0.85);
		borders[2] = new Line(p[1].pos.x, p[1].pos.y + radius, p[3].pos.x - offset, p[3].pos.y - radius * 0.85);
		borders[3] = new Line(p[2].pos.x + offset, p[2].pos.y + radius * 0.85, p[4].pos.x, p[4].pos.y - radius);
		borders[4] = new Line(p[3].pos.x - offset, p[3].pos.y + radius * 0.85, p[5].pos.x, p[5].pos.y - radius);
		borders[5] = new Line(p[4].pos.x + radius, TABLE_HEIGHT - heightGap / 2, p[1].pos.x - radius, TABLE_HEIGHT - heightGap / 2);

		// initializing the cue ball
		double centerX = widthGap / 2 + PLAY_WIDTH / 2;
		double centerY = heightGap / 2 + PLAY_HEIGHT / 2;
		b[0] = new Ball(centerX, centerY + PLAY_HEIGHT / 4, BALL_RADIUS, Color.WHITE, Color.WHITE);

		double initialPosX = centerX;
		double initialPosY = centerY - PLAY_HEIGHT / 4;

		dx = Math.sin(30.0 / 180.0 * Math.PI) * BALL_RADIUS * 2;
		dy = Math.cos(30.0 / 180.0 * Math.PI) * BALL_RADIUS * 2;

		// initializing all the balls
		b[1] = new Ball(initialPosX, initialPosY, BALL_RADIUS, Color.YELLOW, Color.YELLOW);

		b[2] = new Ball(initialPosX - dx, initialPosY - dy, BALL_RADIUS, Color.BLUE, Color.BLUE);
		b[3] = new Ball(initialPosX + dx, initialPosY - dy, BALL_RADIUS, Color.RED, Color.RED);

		b[4] = new Ball(initialPosX - 2 * dx, initialPosY - 2 * dy, BALL_RADIUS, Color.PINK, Color.PINK);
		b[8] = new Ball(initialPosX, initialPosY - 2 * dy, BALL_RADIUS, Color.BLACK, Color.BLACK);
		b[5] = new Ball(initialPosX + 2 * dx, initialPosY - 2 * dy, BALL_RADIUS, Color.ORANGE, Color.ORANGE);

		b[6] = new Ball(initialPosX - 3 * dx, initialPosY - 3 * dy, BALL_RADIUS, Color.GREEN, Color.GREEN);
		b[7] = new Ball(initialPosX - dx, initialPosY - 3 * dy, BALL_RADIUS, Color.LIGHT_GRAY, Color.LIGHT_GRAY);
		b[9] = new Ball(initialPosX + dx, initialPosY - 3 * dy, BALL_RADIUS, Color.YELLOW, Color.WHITE);
		b[10] = new Ball(initialPosX + 3 * dx, initialPosY - 3 * dy, BALL_RADIUS, Color.BLUE, Color.WHITE);

		b[11] = new Ball(initialPosX - 4 * dx, initialPosY - 4 * dy, BALL_RADIUS, Color.RED, Color.WHITE);
		b[12] = new Ball(initialPosX - 2 * dx, initialPosY - 4 * dy, BALL_RADIUS, Color.PINK, Color.WHITE);
		b[13] = new Ball(initialPosX, initialPosY - 4 * dy, BALL_RADIUS, Color.ORANGE, Color.WHITE);
		b[14] = new Ball(initialPosX + 2 * dx, initialPosY - 4 * dy, BALL_RADIUS, Color.GREEN, Color.WHITE);
		b[15] = new Ball(initialPosX + 4 * dx, initialPosY - 4 * dy, BALL_RADIUS, Color.LIGHT_GRAY, Color.WHITE);

	}

	@Override
	public void paintComponent (Graphics g) {
		super.paintComponent(g);
		this.setBorder(BorderFactory.createLineBorder(Color.black));

		// painting pockets
		for (Ball pocket : p) {
			pocket.draw(g);
		}

		// painting border
		for (Line border : borders) {
			g.setColor(Color.BLACK);
			g.drawLine((int) border.v1.x, (int) border.v1.y, (int) border.v2.x, (int) border.v2.y);
		}

		// painting balls
		for (int i = b.length - 1; i >= 0; i--) {
			Ball ball = b[i];
			if (ball.isSunk) 
				continue;
			ball.draw(g);
		}

		// painting direction
		if (isStaticSystem() && state != GameState.PLACING_BALL) {
			updateDirectionIndicator();
			g.setColor(Color.BLACK);
			g.drawLine((int) b[0].pos.x, (int) b[0].pos.y, (int) (b[0].pos.x + Math.cos(angle) * r), (int) (b[0].pos.y + r * Math.sin(angle)));
		}
	}

	/**
	 * Updating all the balls by one tick and checking and resolving collisions.
	 */
	public void update () {
		synchronized (lock) {
			main : for (int i = 0; i < b.length; i++) {
				if (b[i].isSunk)
					continue;
				if (i == 0 && state == GameState.PLACING_BALL)
					continue;
				
				// updating by one tick
				b[i].update();
				
				// ball has entered a pocket
				for (int j = 0; j < p.length; j++) {
					if (b[i].overlap(p[j])) {
						b[i].isSunk = true;
						b[i].vel = new Vector(0, 0);
						b[i].omega = new Vector(0, 0);

						if (i == 0)
							state = GameState.BALL_IN_HAND;
						else if (i == 8) {
							state = GameState.GAME_OVER;
							if (!parent.isScratch(b[i]))
								parent.sc.println("<WINNER " + parent.getCurrentPlayer());
							else
								parent.sc.println("<WINNER " + ((parent.getCurrentPlayer() + 1) % 2));
						} else {
							parent.addBall(b[i], isBreak);
							if (!parent.isScratch(b[i]) && (state == GameState.PLAYED || state == GameState.NO_SCRATCH_PLAYED))
								state = GameState.CONTINUE_PLAYED;
						}
						
						continue main;
					}
				}
				
				// ball has hit a wall
				for (int j = 0; j < borders.length; j++) {
					if (b[i].isIntersecting(borders[j])) {
						// 0 and 5 are vertical flips
						if (j == 0 || j == 5) {
							b[i].vel = new Vector(b[i].vel.x, -b[i].vel.y * WALL_RESISTITION);
							if (b[i].pos.y < borders[j].v1.y)
								b[i].pos.y = borders[j].v1.y - b[i].radius;
							if (b[i].pos.y > borders[j].v1.y)
								b[i].pos.y = borders[j].v1.y + b[i].radius;
						} else {
							b[i].vel = new Vector(-b[i].vel.x * WALL_RESISTITION, b[i].vel.y);
							if (b[i].pos.x < borders[j].v1.x)
								b[i].pos.x = borders[j].v1.x - b[i].radius;
							if (b[i].pos.x > borders[j].v1.x)
								b[i].pos.x = borders[j].v1.x + b[i].radius;
						}
					}
				}
				
				// ball has hit another ball
				for (int j = i + 1; j < b.length; j++) {
					if (b[j].isSunk)
						continue;
					if (b[i].isIntersecting(b[j])) {
						handleCollision(b[i], b[j]);
						if (i == 0) {
							if (parent.isScratch(b[j]) && state == GameState.PLAYED) {
								state = GameState.BALL_IN_HAND;
							} else if ((!parent.isScratch(b[j]) && state == GameState.PLAYED) || (isBreak)) {
								state = GameState.NO_SCRATCH_PLAYED;
							}
						}
					}
				}
			}

			// determining the next game state if it is a static system
			if (isStaticSystem() && state != GameState.PLAY && state != GameState.PLACING_BALL) {
				if (state == GameState.PLAYED)
					state = GameState.BALL_IN_HAND;
	
				if (state == GameState.NO_SCRATCH_PLAYED) {
					parent.switchTurns();
					parent.sc.println("<CURRENT_PLAYER " + parent.getCurrentPlayer());
				} else if (state == GameState.BALL_IN_HAND) {
					parent.switchTurns();
					b[0].isSunk = false;
					b[0].pos = new Vector(TABLE_WIDTH / 2, TABLE_HEIGHT / 2 + PLAY_HEIGHT / 4);
					state = GameState.PLACING_BALL;
					parent.sc.println("<CURRENT_PLAYER " + parent.getCurrentPlayer());
					parent.sc.println("<BALL_IN_HAND");
					return;
				} else if (state == GameState.CONTINUE_PLAYED) {
					parent.sc.println("<CURRENT_PLAYER " + parent.getCurrentPlayer());
				}
				
				state = GameState.PLAY;
				isBreak = false;
			}
		}
	}

	/**
	 * 
	 * @param state state to set
	 */
	public void setGameState (GameState state) {
		this.state = state;
	}
	
	/**
	 * 
	 * @return state
	 */
	public GameState getState () {
		return state;
	}
	
	/**
	 * 
	 * @param val value to change direction angle by
	 */
	public void changeDirectionAngle (double val) {
		angle += val;
	}

	/**
	 * 
	 * @param dx x difference to change x-coordinate of cue ball by
	 * @param dy y difference to change y-coordinate of cue ball by
	 */
	public void changeCuePosition (double dx, double dy) {
		b[0].pos = b[0].pos.add(new Vector(dx, dy));
	}
	
	/**
	 * 
	 * @return true if cue ball interested with another ball
	 */
	public boolean isCuePositionOccupied () {
		for (int i = 1; i < b.length; i++)
			if (b[0].isIntersecting(b[i]))
				return true;
		return false;
	}
	
	/**
	 * 
	 * @param v velocity to set the cue ball to
	 */
	public void setVelocity (double v) {
		b[0].vel = new Vector(v * Math.cos(angle), v * Math.sin(angle));
		state = GameState.PLAYED;
	}

	/**
	 * 
	 * @return true if no ball has any translational or angular velocity
	 */
	public boolean isStaticSystem () {
		boolean ret = true;
		for (int i = 0; i < b.length; i++)
			ret &= b[i].vel.norm() < EPS && b[i].omega.norm() < EPS;
		return ret;
	}

	/**
	 * 
	 * @param type type of ball to check
	 * @return true if there is ball that has not yet been sunk with type
	 */
	public boolean hasUnsunkType (int type) {
		for (int i = 0; i < b.length; i++)
			if (!b[i].isSunk && b[i].getType() == type)
				return true;
		return false;
	}

	/**
	 * Updates the direction vector by stopping at the closest intersection points.
	 */
	private void updateDirectionIndicator () {
		r = 1 << 30;
		Line v = new Line(b[0].pos.x, b[0].pos.y, b[0].pos.x + Math.cos(angle) * r, b[0].pos.y + r * Math.sin(angle));
		for (int i = 0; i < borders.length; i++) {
			Line border = borders[i];
			Vector pt = v.getIntersectionPoint(border);
			if (pt != null && v.contains(pt) && border.contains(pt) && pt.getDistance(v.v1) <= v.getDistance())
				v.v2 = pt;
		}

		for (int i = 1; i < b.length; i++) {
			if (b[i].isSunk)
				continue;
			ArrayList<Vector> pts = v.getIntersectionPoints(b[i]);
			for (Vector pt : pts)
				if (pt != null && v.contains(pt) && pt.getDistance(v.v1) <= v.getDistance())
					v.v2 = pt;
		}

		for (int i = 0; i < p.length; i++) {
			ArrayList<Vector> pts = v.getIntersectionPoints(p[i]);
			for (Vector pt : pts)
				if (pt != null && v.contains(pt) && pt.getDistance(v.v1) <= v.getDistance())
					v.v2 = pt;
		}

		r = v.getDistance();
	}

	/**
	 * Handles the collision between two balls.
	 * @param b1 ball one
	 * @param b2 ball two
	 */
	private void handleCollision (Ball b1, Ball b2) {
		translate(b1, b2);
		double nxs = b2.pos.x - b1.pos.x;
		double nys = b2.pos.y - b1.pos.y;
		double unxs = nxs / Math.sqrt((nxs * nxs + nys * nys));
		double unys = nys / Math.sqrt((nxs * nxs + nys * nys));
		double utxs = -unys;
		double utys = unxs;
		double n1 = unxs * b1.vel.x + unys * b1.vel.y;
		double nt1 = utxs * b1.vel.x + utys * b1.vel.y;
		double n2 = unxs * b2.vel.x + unys * b2.vel.y;
		double nt2 = utxs * b2.vel.x + utys * b2.vel.y;

		double nn1 = ((n2 - n1) * BALL_RESISTITION + n2 + n1) / 2;
		double nn2 = ((n1 - n2) * BALL_RESISTITION + n2 + n1) / 2;
		b1.vel.x = nn1 * unxs + nt1 * utxs;
		b1.vel.y = nn1 * unys + nt1 * utys;

		b2.vel.x = nn2 * unxs + nt2 * utxs;
		b2.vel.y = nn2 * unys + nt2 * utys;

	}

	/**
	 * Translates two balls so they do not overlap.
	 * @param b1 ball one
	 * @param b2 ball two
	 */
	private void translate (Ball b1, Ball b2) {
		double dx = (b1.pos.x - b2.pos.x);
		double dy = (b1.pos.y - b2.pos.y);
		double d = Math.sqrt(dx * dx + dy * dy);
		dx *= (b1.radius + b2.radius - d) / d;
		dy *= (b1.radius + b2.radius - d) / d;
		double im1 = 1;
		double im2 = 1;
		b1.pos.x += dx * im1 / (im1 + im2);
		b1.pos.y += dy * im1 / (im1 + im2);
		b2.pos.x -= dx * im2 / (im1 + im2);
		b2.pos.y -= dy * im2 / (im1 + im2);
		repaint();
	}
}
