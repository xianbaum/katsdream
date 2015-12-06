package com.christianbaum.games.katsdream;

import java.util.Deque;
import java.util.LinkedList;

/** A structure that contains the position, and whether it can travel
 * up, down, left, or right.
 * @author Christian Baum
 *
 */
public class GridNode extends Point {
	/** The x/y position */
	/** The amount of attempts. Used to determine the next dir. */
	private int attempts;
	/** This function 
	 * Collision type 1 is impassible.
	 * 
	 * @param pos The X and Y position of the node.
	 * @param map The map.
	 */
	GridNode( Point pos ) {
		super( pos );
		attempts = 0;
	}
	
	GridNode( float x, float y ) {
		super( x, y );
		attempts = 0;
	}
	
	/** Gets the next point to traverse in the direction.
	 * Yikes, this is a bloated function.
	 * @param current Current point
	 * @param dest Destination point
	 * @param map Level (to test if a part can be traversed or not)
	 * TODO: unbloat
	 * @return The next point
	 */
	private GridNode next_point ( Point dest, Level map,
			boolean traversed[][]) {
		Dir current_dir = Dir.NO;
		Point next_point = null;
		if( x() > dest.x() ) {
			if( y( )<= dest.y() ) {
			do {
				current_dir = Dir.getDirFromIndex( attempts);
				attempts++;
				next_point=canTraverse(current_dir, map, traversed);
			} while ( next_point== null && attempts < 4);
			}
			else {
				if( y() > dest.y()) {
					do {
						if( attempts == 0)
							current_dir = Dir.LEFT;
						else if( attempts == 1 )
							current_dir = Dir.DOWN;
						else if(attempts == 2 )
							current_dir = Dir.UP;
						else if( attempts == 3 )
							current_dir = Dir.RIGHT;
						attempts++;
						next_point = canTraverse(current_dir, map, traversed);
					} while ( next_point == null && attempts < 4);
				}
			}
		}
		else if( x() == dest.x()) {
			if( y() > dest.y()) {
				do {
					if( attempts == 0)
						current_dir = Dir.DOWN;
					else if( attempts == 1 )
						current_dir = Dir.LEFT;
					else if(attempts == 2 )
						current_dir = Dir.RIGHT;
					else if( attempts == 3 )
						current_dir = Dir.UP;
					attempts++;
					next_point = canTraverse(current_dir, map, traversed);
				} while ( next_point == null && attempts < 4);
			}
			else {
				do {
					if( attempts == 0)
						current_dir = Dir.UP;
					else if( attempts == 1 )
						current_dir = Dir.LEFT;
					else if(attempts == 2 )
						current_dir = Dir.RIGHT;
					else if( attempts == 3 )
						current_dir = Dir.DOWN;
					attempts++;
					next_point = canTraverse(current_dir, map, traversed);
				} while ( next_point == null && attempts < 4);
			}
		}
		else {
			if( y()<dest.y()) {
				if( y()>dest.y()) {
					do {
						if( attempts == 0)
							current_dir = Dir.RIGHT;
						else if( attempts == 1 )
							current_dir = Dir.UP;
						else if(attempts == 2 )
							current_dir = Dir.DOWN;
						else if( attempts == 3 )
							current_dir = Dir.LEFT;
						attempts++;
						next_point = canTraverse(current_dir, map, traversed);
					} while ( next_point == null && attempts < 4);
				}
				else {
					do {
						current_dir = Dir.getDirFromIndex( 3-attempts);
						attempts++;
						next_point = canTraverse(current_dir, map, traversed);
					} while ( next_point == null || attempts < 4 );
				}
			}
		}
		return new GridNode(next_point);
	}
	
	/** Subtracts the left_map_width from the location x and destination x.
	 * 
	 * @param left_screen_width The width of the left map that was replaced.
	 */
	public void updateXFromMapChange( int left_screen_width ) {
		setX( x() - left_screen_width );
	}
	
	/** Uses a silly depth-first search to find a path to the player.
	 * 
	 * 
	 * @param start The point to start from
	 * @param end The destination of the path
	 * @param map The map
	 * @return
	 */
	
	//TODO: take traversed into account
	private Point canTraverse(Dir direction, Level map, boolean traversed[][]) {
		switch (direction) {
		case LEFT:
			return map.getCollisionType( (int)x() - 1, (int)y() ) 
			== 1 && traversed[(int) ( x()-1)][(int) y()] &&
			 x() == 0 ? null : new Point( x()-1, y() );
		case UP:
			return map.getCollisionType( (int) x(), (int) y()-1) 
			== 1 && traversed[(int) x()][(int) ( y()-1)] &&
			 y() == 0 ? null : new Point( x(), y()-1 );
		case DOWN:
			return map.getCollisionType( (int) x(), (int) y()+1) 
			== 1 && traversed[(int) x()][(int) ( y()+1)] &&
			 y() != 15 ? null : new Point( x(), y()+1);
		case RIGHT:
			return map.getCollisionType( (int) x()+1, (int) y()) 
			!= 1 && !traversed[(int) ( x()+1)][(int) y()] &&
			 x() != map.levelWidth()?
					new Point( x()+1, y()) : null;
		default:
			return null;
		}
	}
	
	public static boolean canTraverse( 
			Point point, Point player_pos, Level map, boolean traversed[][]) {
		if( player_pos.equals( point.x(), point.y() - 1 ) ||
			player_pos.equals( point.x(), point.y() + 1 ) || 
			player_pos.equals( point.x() - 1, point.y() ) ||
			player_pos.equals( point.x() + 1, point.y() ) )
			return map.getCollisionType( point ) != 1 && 
			!traversed[ (int)point.x() ][ (int) point.y() ];
		return false;
	}

	//TODO: This
	public static Deque<GridNode> findPath( Point start, Point end, Level map) {
		boolean traversed[][] = new boolean[ map.levelWidth() ][ 15 ];
		Deque<GridNode> path = new LinkedList<GridNode>();
		GridNode current_gridnode = new GridNode( start );
		GridNode next_gridnode;
		traversed[ (int)current_gridnode.x() ]
				[(int)current_gridnode.y()] = true;
		int passes=0;
		do{
			traversed[ (int)current_gridnode.x() ]
					[(int)current_gridnode.y()] = true;
			next_gridnode = current_gridnode.next_point( end, map, traversed);
			if( next_gridnode != null) {
				path.addFirst( current_gridnode );
				current_gridnode = next_gridnode;
			}
			else {
				path.remove(current_gridnode);
				if( !path.isEmpty())
					current_gridnode = path.removeLast();
			}
			passes++;
		} while (!path.isEmpty() && passes < 15);
		return path;
	}
	
	public static Deque<GridNode> addToPath(Deque<GridNode> path, 
			Point click_tile_pos ) {
		
		Point last_point = path.getLast();
		if( click_tile_pos.equals( last_point.x()-1, last_point.y() ) ) {
			path.add( new GridNode( click_tile_pos ) );
		}
		else if( click_tile_pos.equals( last_point.x()+1, last_point.y() ) ) {
			path.add( new GridNode( click_tile_pos ) );
		}
		else if( click_tile_pos.equals( last_point.x(), last_point.y()-1 ) ) {
			path.add( new GridNode( click_tile_pos ) );
		}
		else if( click_tile_pos.equals( last_point.x(), last_point.y()+1 ) ) {
			path.add( new GridNode( click_tile_pos ) );
		}
		
		return path;
	}
	
	public static Deque<GridNode> updateXFromMapChange( Deque<GridNode> path,
			int left_map_width ) {
		Deque<GridNode> new_path = new LinkedList<GridNode>();
		while( !path.isEmpty() ) {
			GridNode node = path.remove() ;
			node.updateXFromMapChange( left_map_width );
			new_path.add( node );
		}
		return new_path;
	}
	
}
