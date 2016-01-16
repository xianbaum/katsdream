package com.christianbaum.games.katsdream;

/** Direction enum.
 * 
 * @author Christian Baum
 *
 */
public enum Dir {
	LEFT(0),
	UP(1),
	DOWN(2),
	RIGHT(3),
	NO(4);

	/** Corrospondant integer for the enum. Useful for sprite sheet. */
	private int index;
	
	/** Constructor for Dir. All it does is sets an index.
	 * @param index The index number.
	 */
	private Dir( int index ) {
		this.index = index;
	}
	
	/** Gets Dir.Index. Useful for sprite sheet. 
	 * 
	 * @return Dir.index
	 */
	public int index() {
		return index;
	}
	
	/** Gets a dir based on a Vect normalization (specifically for Vect speed)
	 * 
	 * @param vect The Vect to get the dir from.
	 * @return A dir based on the vect that is the most.
	 */
	public static Dir getDirFromVectNormalization( float x_spd, float y_spd ) {
		if( Math.abs(x_spd) > Math.abs(y_spd) ) {
			if(x_spd > 0) 
				return RIGHT;
			//Else, it'd be left
			return LEFT;
		}
		else {
			if(y_spd > 0)
				return UP;
		}
		//If nothing else, it'd be down.
		return DOWN;
	}
	
	public Dir opposite() {
		switch( this ){
		case UP:
			return DOWN;
		case DOWN:
			return UP;
		case LEFT:
			return RIGHT;
		case RIGHT:
			return LEFT;
		default:
			return NO;
		}
	}
	/** Converts an index number to a dir
	 * 
	 * @param index The index number. See the index numbers above.
	 * @return The Dir gotten from the index numbers
	 */
	public static Dir fromIndex( int index ) {
	switch (index) {
	case 0:
		return LEFT;
	case 1:
		return UP;
	case 2:
		return DOWN;
	case 3:
		return RIGHT;
	default:
		return NO;
		}
	}
}
