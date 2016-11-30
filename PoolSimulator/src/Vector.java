/**
 * Object representing both 2D vectors and points.
 */

public class Vector {
	double x, y;

	/**
	 * 
	 * @param x x-coordinate corresponding to the vector
	 * @param y y-coorindate corresponding to the vector
	 */
	Vector (double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 
	 * @param v vector to be added to this Vector
	 * @return this + val
	 */
	public Vector add (Vector v) {
		return new Vector(x + v.x, y + v.y);
	}

	/**
	 * 
	 * @param v vector to be subtracted from this Vector
	 * @return this - val
	 */
	public Vector subtract (Vector v) {
		return this.add(v.negate());
	}

	/**
	 * 
	 * @param s scalar to multiply this Vector by
	 * @return this * s
	 */
	public Vector multiply (double s) {
		return new Vector(x * s, y * s);
	}

	/**
	 * 
	 * @param s scalar to divide this Vector by
	 * @return this / s
	 */
	public Vector divide (double s) {
		return new Vector(x / s, y / s);
	}

	/** 
	 * 
	 * @return the length of of this Vector
	 */
	public double norm () {
		return Math.sqrt(x * x + y * y);
	}

	/**
	 * Used when this Vector and v are points.
	 * @param v the vector to calculate the distance to this Vector
	 * @return the distance between v and this Vector
	 */
	public double getDistance (Vector v) {
		return new Vector(x - v.x, y - v.y).norm();
	}

	/**
	 * 
	 * @return -this
	 */
	public Vector negate () {
		return new Vector(-x, -y);
	}

	/**
	 * 
	 * @param v the vector to dot with this Vector
	 * @return this * v
	 */
	public double dot (Vector v) {
		return x * v.x + y * v.y;
	}

	/**
	 * 
	 * @param v the vector to append column-wise to this Vector
	 * @return the determinant of the newly-formed matrix.
	 */
	public double det (Vector v) {
		return x * v.y - v.x * y;
	}
	
	/**
	 * 
	 * @return a vector that is the element-wise signum of this Vector
	 */
	public Vector signum () {
		return new Vector(Math.signum(x), Math.signum(y));
	}
	
	/**
	 * 
	 * @return a vector that is the element-wise absolute value of this Vector
	 */
	public Vector abs () {
		return new Vector(Math.abs(x), Math.abs(y));
	}

	/**
	 * This Vector is a point and d is a direction vector of a line that goes through the origin.
	 * @param d direction vector for this Vector to be projected on.
	 * @return point of the projection
	 */
	public Vector proj (Vector d) {
		return d.multiply(this.dot(d) / d.norm() / d.norm());
	}
}
