/**
 * Object representing every circular object used in PoolSimulator. In particular
 * the same object is used for the 16 balls and the 6 pockets. Each ball has a
 * translational velocity vector where right and bottom are the positive directions
 * and a rotational velocity vector where counter clockwise in the x and y axis
 * are the positive directions. The secondary color of a ball is only used when
 * the ball is striped.
 */

import java.awt.Color;
import java.awt.Graphics;

public class Ball {
	Vector pos, vel, omega;
	double mass, inertia;
	double radius;
	boolean isSunk;
	Color primary, secondary;

	/**
	 * 
	 * @param x the x position of the center of the ball
	 * @param y the y position of the center of the ball
	 * @param radius the radius of the ball
	 * @param primary the primary color of the ball
	 * @param secondary the secondary color of the ball
	 */
	Ball (double x, double y, int radius, Color primary, Color secondary) {
		pos = new Vector(x, y);
		vel = new Vector(0, 0);
		omega = new Vector(0, 0);
		isSunk = false;
		this.mass = 1;
		this.inertia = 2.0 / 5.0 * mass * radius * radius;
		this.radius = radius;
		this.primary = primary;
		this.secondary = secondary;
	}

	/**
	 * Used to determine when a ball collides with another ball.
	 * 
	 * @param b the ball to check intersection with
	 * @return true if ball intersects with b
	 */
	public boolean isIntersecting (Ball b) {
		return pos.getDistance(b.pos) < radius + b.radius;
	}

	/**
	 * Used to determine when a ball falls into a pocket.
	 * 
	 * @param b the ball to check overlap with
	 * @return true if ball overlaps with b
	 */
	public boolean overlap (Ball b) {
		return pos.getDistance(b.pos) < Math.max(radius, b.radius);
	}

	/**
	 * Used to determine when a ball collides with a wall of the pool table.
	 * 
	 * @param l the line to check intersection with
	 * @return true if ball intersects with l
	 */
	public boolean isIntersecting (Line l) {
		Vector v = pos.subtract(l.v1);
		Vector d = l.v2.subtract(l.v1);
		Vector proj = v.proj(d);

		proj = proj.add(l.v1);
		if (proj.x < Math.min(l.v1.x, l.v2.x))
			proj.x = Math.min(l.v1.x, l.v2.x);
		else if (proj.x > Math.max(l.v1.x, l.v2.x))
			proj.x = Math.max(l.v1.x, l.v2.x);
		if (proj.y < Math.min(l.v1.y, l.v2.y))
			proj.y = Math.min(l.v1.y, l.v2.y);
		else if (proj.y > Math.max(l.v1.y, l.v2.y))
			proj.y = Math.max(l.v1.y, l.v2.y);
		return pos.getDistance(proj) < radius;
	}

	/**
	 * Draws the ball on a specified graphics with two primary colored stripes on the top
	 * and bottom of the ball
	 * 
	 * @param g the graphics to draw the ball on
	 */
	public void draw (Graphics g) {
		double stripeWidth = 2 * (int) (Math.sqrt(radius * radius - (radius / 2.0) * (radius / 2.0)));
		g.setColor(this.secondary);
		g.fillOval((int) (pos.x - radius), (int) (pos.y - radius), (int)(radius * 2), (int)(radius * 2));

		double offset = radius - stripeWidth / 2;
		g.setColor(this.primary);
		g.fillArc((int) (pos.x - radius + offset), (int) (pos.y - radius), (int)(stripeWidth), (int)(radius), 0, 180);
		g.fillArc((int) (pos.x - radius + offset), (int) (pos.y), (int)(stripeWidth), (int)(radius), 0, -180);

		g.setColor(Color.BLACK);
		g.drawOval((int) (pos.x - radius), (int) (pos.y - radius), (int)(radius * 2), (int)(radius * 2));
	}

	/**
	 * Updates the position, translational velocity, and angular velocity of the ball.
	 */
	public void update () {
		if (vel.abs().norm() < GamePanel.EPS) {
			omega = new Vector(0, 0);
			return;
		}
		
		pos = pos.add(vel);
		Vector v = omega.multiply(radius);
		Vector totalV = v.add(vel);
		Vector absTotalV = totalV.abs();
		
		// the angle that friction is acting in -- opposes the direction of movement
		double theta = Math.atan2(vel.y, vel.x);
		
		// Used when the ball is slipping
		Vector kineticFriction = new Vector(Math.abs(Math.cos(theta) * GamePanel.FRICTION), Math.abs(Math.sin(theta) * GamePanel.FRICTION));
		
		// Used with the ball is rolling without slipping
		Vector rollingFriction = new Vector(Math.abs(Math.cos(theta) * GamePanel.ROLLING_FRICTION), Math.abs(Math.sin(theta) * GamePanel.ROLLING_FRICTION));
		
		// Check to see if translation and angular velocity are "oscillating". If they are,
		// then set v = wr to set the ball into perfect rolling
		boolean velXGreater = vel.abs().x > omega.abs().x * radius;
		boolean velYGreater = vel.abs().y > omega.abs().y * radius;
		
		// Rolling perfectly in the x direction
		if (absTotalV.x < GamePanel.EPS) {
			double sub = Math.signum(vel.x) * rollingFriction.x;
			if (Math.abs(vel.x) < Math.abs(sub))
				vel.x = 0;
			else
				vel.x -= sub;
			if (Math.abs(omega.x) < Math.abs(sub / radius))
				omega.x = 0;
			else
				omega.x += sub / radius;
		} 
		// Slipping in the x direction
		else {
			double sub = Math.signum(totalV.x) * kineticFriction.x;
			if (Math.abs(vel.x) < Math.abs(sub) && Math.signum(sub) == Math.signum(vel.x))
				vel.x = 0;
			else {
				vel.x -= sub;
			}
			
			if (Math.abs(omega.x) < Math.abs(sub / radius) && Math.signum(sub / radius) == Math.signum(omega.x))
				omega.x = 0;
			else {
				omega.x -= sub / radius;
			}
		}

		// Perfectly rolling the in y direction
		if (absTotalV.y < GamePanel.EPS) {
			double sub = Math.signum(vel.y) * rollingFriction.y;
			if (Math.abs(vel.y) < Math.abs(sub))
				vel.y = 0;
			else
				vel.y -= sub;
			if (Math.abs(omega.y) < Math.abs(sub / radius))
				omega.y = 0;
			else
				omega.y += sub / radius;
		} 
		// Slipping in the y direction
		else {
			double sub = Math.signum(totalV.y) * kineticFriction.y;
			if (Math.abs(vel.y) < Math.abs(sub) && Math.signum(sub) == Math.signum(vel.y))
				vel.y = 0;
			else
				vel.y -= sub;
			if (Math.abs(omega.y) < Math.abs(sub / radius) && Math.signum(sub / radius) == Math.signum(omega.y))
				omega.y = 0;
			else
				omega.y -= sub / radius;
		}

		// Setting the ball into perfect rolling if translational and angular
		// velocities are "oscillating"
		if (velXGreater != (vel.abs().x > omega.abs().x * radius)) {
			double abs = Math.abs(vel.abs().x + omega.abs().x * radius) / 2;
			if (vel.x >= 0) {
				vel.x = abs;
				omega.x = -abs / radius;
			} else {
				vel.x = -abs;
				omega.x = abs/radius;
			}
		}
		if (velYGreater != (vel.abs().y > omega.abs().y * radius)) {
			double abs = Math.abs(vel.abs().y + omega.abs().y * radius) / 2;
			if (vel.y >= 0) {
				vel.y = abs;
				omega.y = -abs / radius;
			} else {
				vel.y = -abs;
				omega.y = abs/radius;
			}
		}
	}
	
	/**
	 * 
	 * @return the type of the ball
	 */
	public int getType () {
		if (isSolid())
			return UserPanel.SOLID_ID;
		if (isStriped())
			return UserPanel.STRIPED_ID;
		if (isEight())
			return UserPanel.EIGHT_ID;
		return UserPanel.NONE_ID;
	}
	
	/**
	 * 
	 * @return true is the ball is solid
	 */
	public boolean isSolid () {
		return primary == secondary && !isCue() && !isEight();
	}
	
	/**
	 * 
	 * @return true if the ball is striped
	 */
	public boolean isStriped () {
		return primary != secondary && !isCue() && !isEight();
	}
	
	/**
	 * 
	 * @return true if the ball is the cue ball
	 */
	public boolean isCue () {
		return primary == Color.WHITE && secondary == Color.WHITE;
	}
	
	/**
	 * 
	 * @return true if the ball is the eight ball
	 */
	public boolean isEight () {
		return primary == Color.BLACK && secondary == Color.BLACK;
	}
}
