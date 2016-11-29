public class Vector {
	double x, y;

	Vector (double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Vector add (Vector v) {
		return new Vector(x + v.x, y + v.y);
	}

	public Vector subtract (Vector v) {
		return this.add(v.negate());
	}

	public Vector multiply (double s) {
		return new Vector(x * s, y * s);
	}

	public Vector divide (double s) {
		return new Vector(x / s, y / s);
	}

	public double norm () {
		return Math.sqrt(x * x + y * y);
	}

	public double dist (Vector v1) {
		return new Vector(x - v1.x, y - v1.y).norm();
	}

	public Vector negate () {
		return new Vector(-x, -y);
	}

	public double dot (Vector v) {
		return x * v.x + y * v.y;
	}

	public double det (Vector v) {
		return x * v.y - v.x * y;
	}
	
	public Vector signum () {
		return new Vector(Math.signum(x), Math.signum(y));
	}
	
	public Vector abs () {
		return new Vector(Math.abs(x), Math.abs(y));
	}

	// point projected on vector d which goes through the origin
	public Vector proj (Vector d) {
		return d.multiply(this.dot(d) / d.norm() / d.norm());
	}

	public String toString () {
		return String.format("(%.2f, %.2f)", x, y);
	}
}
