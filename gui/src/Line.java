/**
 * Object representing a 2D line.
 */

import java.util.ArrayList;

public class Line {
	
	Vector v1, v2;

	/**
	 * 
	 * @param x1 x-coordinate of the start of the line
	 * @param y1 y-coordinate of the start of the line
	 * @param x2 x-coordinate of the end of the line
	 * @param y2 y-coordinate of the end of the line
	 */
	Line (double x1, double y1, double x2, double y2) {
		this.v1 = new Vector(x1, y1);
		this.v2 = new Vector(x2, y2);
	}

	/**
	 *
	 * @param b ball to check intersection with
	 * @return a list of the intersection points between this Line and Ball b
	 */
	public ArrayList<Vector> getIntersectionPoints (Ball b) {
		ArrayList<Vector> ret = new ArrayList<Vector>();
		if (!b.isIntersecting(this))
			return ret;
		Vector start = this.v1.subtract(b.pos);
		Vector end = this.v2.subtract(b.pos);

		double dx = end.x - start.x;
		double dy = end.y - start.y;
		double dist = start.getDistance(end);
		double det = start.det(end);

		double delta = Math.sqrt(b.radius * b.radius * dist * dist - det * det);
		if (delta < GamePanel.EPS) {
			ret.add(new Vector((det * dy + Math.signum(dy) * dx * delta) / dist / dist, (-det * dx + Math.abs(dy) * delta) / dist / dist));
		} else {
			ret.add(new Vector((det * dy + Math.signum(dy) * dx * delta) / dist / dist, (-det * dx + Math.abs(dy) * delta) / dist / dist));
			ret.add(new Vector((det * dy - Math.signum(dy) * dx * delta) / dist / dist, (-det * dx - Math.abs(dy) * delta) / dist / dist));
		}
		for (int i = 0; i < ret.size(); i++)
			ret.set(i, ret.get(i).add(b.pos));
		return ret;
	}

	/**
	 * 
	 * @param l line to check the intersection point with
	 * @return the intersection point between this Line and Line l. null lines are coincident or parallel.
	 */
	public Vector getIntersectionPoint (Line l) {
		double A1 = this.v1.y - this.v2.y;
		double B1 = this.v2.x - this.v1.x;
		double C1 = -A1 * this.v1.x - B1 * this.v1.y;

		double A2 = l.v1.y - l.v2.y;
		double B2 = l.v2.x - l.v1.x;
		double C2 = -A2 * l.v1.x - B2 * l.v1.y;

		double det = A1 * B2 - A2 * B1;

		if (Math.abs(det) < GamePanel.EPS)
			return null;
		return new Vector((B1 * C2 - B2 * C1) / det, (A2 * C1 - A1 * C2) / det);
	}

	/**
	 * 
	 * @param v point to check
	 * @return true if this Line contains Vector v as a point
	 */
	boolean contains (Vector v) {
		if (Math.min(v1.x, v2.x) <= v.x + GamePanel.EPS && v.x - GamePanel.EPS <= Math.max(v1.x, v2.x) && Math.min(v1.y, v2.y) <= v.y + GamePanel.EPS && v.y - GamePanel.EPS <= Math.max(v1.y, v2.y))
			return true;
		return false;
	}

	/**
	 * 
	 * @return the length of the line segment
	 */
	double getDistance () {
		return v1.getDistance(v2);
	}
}
