package com.christianbaum.games.katsdream;


/** A structure with an X and Y value.
 * 
 * @author Christian Baum
 * 
 */
public class Point {
	/** THe x point */
	protected float x;
	/** The y point */
	protected float y;
	
	/** Creates a point with an X and Y value.
	 * 
	 * @param _x The X value to set
	 * @param _y The Y value to set
	 */
	Point(float _x, float _y) {
		this.x = _x;
		this.y = _y;
	}
	
	Point( Point point ) {
		this.x = point.x();
		this.y = point.y();
	}
	
	public Point dirToPoint(Dir direction ) {
		switch (direction) {
		case LEFT:
			return new Point( x()-1, y() );
		case UP:
			return new Point( x(), y()-1 );
		case DOWN:
			return  new Point( x(), y()+1);
		case RIGHT:
			return new Point( x()+1, y());
		default:
			return null;
		}
	}
		
	/** Gets the X value
	 * 
	 * @return The instance's x value
	 */
	public float x() {
		return x;
	}
	
	/** Gets the Y value.
	 * 
	 * @return THe instance's y value
	 */
	public float y() {
		return y;
	}
	
	/** Sets the instance's x value
	 * 
	 * @param _x	The X value to set to
	 */
	public void setX( float _x ) {
		this.x = _x;
	}
	/** Sets the instance's y value
	 * 
	 * @param _y The Y value to set to/
	 */
	public void setY( float _y) {
		this.y = _y;
	}
	
	/** Compares the X and Y values of both the the "this" point and the other 
	 * point to see whether or not they are the same value.
	 * 
	 * @param other_point The point to compare the object to.
	 * @return True if the X and Y values are identical. False if they are not.
	 */
	public boolean equals( Point other_point ) {
		return ( other_point.x() == x() && other_point.y() == y() );
	}
	
	public boolean equals( float x, float y ) {
		return ( this.x == x && this.y == y );
	}
	
	public void updateXFromMapChange( int level_width ) {
		x -= level_width;
	}
}
