import java.awt.Color;
import java.awt.Graphics;

public class Ball {
	Vector pos, vel;
	int radius, stripeWidth;
	boolean isSunk;
	Color primary, secondary;

	Ball (double x, double y, int radius, Color primary, Color secondary) {
		pos = new Vector(x, y);
		vel = new Vector(0, 0);
		isSunk = false;
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
		g.fillOval((int) (pos.x - radius), (int) (pos.y - radius), radius * 2, radius * 2);

		double offset = radius - stripeWidth / 2;
		g.setColor(this.primary);
		g.fillArc((int) (pos.x - radius + offset), (int) (pos.y - radius), stripeWidth, radius, 0, 180);
		g.fillArc((int) (pos.x - radius + offset), (int) (pos.y), stripeWidth, radius, 0, -180);

		g.setColor(Color.BLACK);
		g.drawOval((int) (pos.x - radius), (int) (pos.y - radius), radius * 2, radius * 2);
	}

	public void update () {
		pos = pos.add(vel);
		if (vel.norm() < GamePanel.FRICTION + GamePanel.EPS)
			vel = new Vector(0, 0);
		else {
			Vector sub = vel.multiply(GamePanel.FRICTION / vel.norm());
			vel = vel.subtract(sub);
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
