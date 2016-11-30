import java.awt.Color;
import java.awt.Graphics;

public class Ball {
	Vector pos, vel, omega;
	double mass, inertia;
	double radius, stripeWidth;
	boolean isSunk;
	Color primary, secondary;

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

	public boolean isIntersecting (Ball b) {
		return pos.dist(b.pos) < radius + b.radius;
	}

	public boolean overlap (Ball b) {
		return pos.dist(b.pos) < Math.max(radius, b.radius);
	}

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
		return pos.dist(proj) < radius;
	}

	public void draw (Graphics g) {
		this.stripeWidth = 2 * (int) (Math.sqrt(radius * radius - (radius / 2.0) * (radius / 2.0)));
		g.setColor(this.secondary);
		g.fillOval((int) (pos.x - radius), (int) (pos.y - radius), (int)(radius * 2), (int)(radius * 2));

		double offset = radius - stripeWidth / 2;
		g.setColor(this.primary);
		g.fillArc((int) (pos.x - radius + offset), (int) (pos.y - radius), (int)(stripeWidth), (int)(radius), 0, 180);
		g.fillArc((int) (pos.x - radius + offset), (int) (pos.y), (int)(stripeWidth), (int)(radius), 0, -180);

		g.setColor(Color.BLACK);
		g.drawOval((int) (pos.x - radius), (int) (pos.y - radius), (int)(radius * 2), (int)(radius * 2));
	}

	public void update () {
		if (vel.abs().norm() < GamePanel.EPS) {
			omega = new Vector(0, 0);
			return;
		}
		
		pos = pos.add(vel);
		Vector v = omega.multiply(radius);
		Vector totalV = v.add(vel);
		Vector absTotalV = totalV.abs();
		double theta = Math.atan2(vel.y, vel.x);
		
		Vector kineticFriction = new Vector(Math.abs(Math.cos(theta) * GamePanel.FRICTION), Math.abs(Math.sin(theta) * GamePanel.FRICTION));
		Vector rollingFriction = new Vector(Math.abs(Math.cos(theta) * GamePanel.ROLLING_FRICTION), Math.abs(Math.sin(theta) * GamePanel.ROLLING_FRICTION));
		
		
		boolean velXGreater = vel.abs().x > omega.abs().x * radius;
		boolean velYGreater = vel.abs().y > omega.abs().y * radius;
		
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
		} else {
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
		} else {
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
	
	public int getType () {
		if (isSolid())
			return UserPanel.SOLID_ID;
		if (isStriped())
			return UserPanel.STRIPED_ID;
		if (isBlack())
			return UserPanel.BLACK_ID;
		return UserPanel.NONE_ID;
	}
	
	public boolean isSolid () {
		return primary == secondary && !isCue() && !isBlack();
	}
	
	public boolean isStriped () {
		return primary != secondary && !isCue() && !isBlack();
	}
	
	public boolean isCue () {
		return primary == Color.WHITE && secondary == Color.WHITE;
	}
	
	public boolean isBlack () {
		return primary == Color.BLACK && secondary == Color.BLACK;
	}
}
