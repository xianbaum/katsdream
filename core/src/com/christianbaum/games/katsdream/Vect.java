package com.christianbaum.games.katsdream;

/** A 2 dimensional vector. It is able to move at a variable speed.
 * It is created with two points.
 * 
 * @author Christian Baum
 *
 */
public class Vect {
	/** The current location of the Vect*/
	private Point loc;
	/** The destination location for the point*/
	final private Point dest;
	/** The X speed of the point; proportional based on loc/dest/speed*/
	final private float x_spd;
	/** The Y speed of the point; proportional based on loc/dest/speed*/
	final private float y_spd;
	/** Whether the vector has arrived or not. */
	private boolean arrived;
	/** Constructor for Vect. Creates a Vect from two sets of x/y points
	 * 
	 * @param _x The start X point
	 * @param _y The start Y point
	 * @param _dx The destination X point
	 * @param _dy THe destination Y point
	 * @param _speed The speed at which to travel to the point
	 */
	Vect(float x, float y, float dx, float dy, float speed) {
		loc = new Point(x, y);
		dest = new Point (dx, dy);
		if( y != dy || x != dx ) {
			x_spd = (float) (( speed * ( dx - x ))/
					Math.sqrt( sqrn( x - dx ) + sqrn( y - dy )));
			y_spd = (float) (( speed * ( dy - y ))/
					Math.sqrt( sqrn( x - dx ) + sqrn( y - dy )));
			arrived = false;
		}
		else {
			x_spd = 0;
			y_spd = 0;
			arrived = true;
		}
	}
	
	Vect( Point start, Point dest, float speed ) {
		this( start.x(), start.y(), dest.x(), dest.y(), speed );
	}
	
	private double sqrn( double num ) {
		return Math.pow( Math.abs( num ), 2);
	}
	
	/** Constructor for Vect. Creates a Vect from two Points
	 * 
	 * @param _start The start point.
	 * @param _dest The destination point
	 * @param _speed The speed of which to travel to the point
	 */
	
	/** Gets the current x value
	 * 
	 * @return The instance of Vect.loc.x
	 */
	public float x(){
		return loc.x();
	}
	
	/** Gets the current y value
	 * 
	 * @return Vect.loc.y from the instance
	 */
	public float y() {
		return loc.y();
	}
	
	/** Gets the current Point
	 * 
	 * @return Vect.loc from the instance
	 */
	public Point loc() {
		return loc;
	}
	
	public Point dest() {
		return dest;
	}
	
	/** Gets whether the vector has arrived at the destination or not.
	 * 
	 * @return Vect.arrived from the current instance.
	 */
	public boolean arrived() {
		return arrived;
	}
	
	/** Moves the vector toward the destination.
	 * If it is within the destination, it will set Vect.arrived to true.
	 * 
	 * @param dt Delta time
	 */
	public void move( float dt ) {
		loc.setX( loc.x() + x_spd*dt);
		loc.setY( loc.y() + y_spd*dt);
		if( !arrived ) {
			if( x_spd > 0 && loc.x() >= dest.x() ||
					y_spd > 0 && loc.y() >= dest.y() ||
					x_spd < 0 && loc.x() <= dest.x() ||
					y_spd < 0 && loc.y() <= dest.y() ||
					y_spd == 0 && x_spd == 0 ||
					loc.equals( dest )) {
				arrived = true;
				loc = new Point( dest );
			}
		}
	}
	
	/** Subtracts the left_map_width from the location x and destination x.
	 * 
	 * @param temp_scroll The width of the left map that was replaced.
	 */
	public void updateXFromMapChange( int level_width ) {
		loc.updateXFromMapChange( level_width );
		dest.updateXFromMapChange( level_width );
	}
	
	/** Gets the X speed
	 * 
	 * @return Vect.x_spd
	 */
	public float x_spd() {
		return x_spd;
	}
	
	/** Gets the Y speed
	 * 
	 * @return Vect.y_spd
	 */
	public float y_spd() {
		return y_spd;
	}
}
