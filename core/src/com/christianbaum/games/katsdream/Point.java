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
	 * @param x The X value to set
	 * @param y The Y value to set
	 */
	Point(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	/** Copy constructor for Point
	 * 
	 * @param point point to copy
	 */
	Point( Point point ) {
		this( point.x, point.y );
	}
	
	/** Turns a direction to one whole point up, down, left or right
	 * 
	 * @param direction The direction
	 * @return a Point in the direction
	 */
	public Point dirToPoint(Dir direction ) {
		switch (direction) {
		case LEFT:
			return new Point( x-1, y );
		case UP:
			return new Point( x, y-1 );
		case DOWN:
			return  new Point( x, y+1);
		case RIGHT:
			return new Point( x+1, y);
		default:
			return null;
		}
	}
	
	/** Returns a point in the direction of the point
	 * 
	 * @param point The point to turn into a direction
	 * @return The Direction gotten from the point
	 */
	public Dir pointToDir( Point point ) {
		if( point.y > y )
			return Dir.DOWN;
		else if( point.y < y )
			return Dir.UP;
		else if( point.x > x)
			return Dir.RIGHT;
		return Dir.LEFT;
	}

	/** Compares the X and Y values of both the the "this" point and the other 
	 * point to see whether or not they are the same value.
	 * 
	 * @param other_point The point to compare the object to.
	 * @return True if the X and Y values are identical. False if they are not.
	 */
	@Override
	public boolean equals( Object o ) {
	    if (o == null || !(o instanceof Point)) {
	        return false;
	    }
		return ( ((Point)o).x == x && ((Point)o).y == y );
	}
	
	@Override
	public int hashCode() {
		return 59 + 73 + (int)x + (int)y;
	}
	
	public boolean equals( float x, float y ) {
		return ( this.x == x && this.y == y );
	}
	
	public void updateXFromMapChange( int level_width ) {
		x -= level_width;
	}
	

	
}
