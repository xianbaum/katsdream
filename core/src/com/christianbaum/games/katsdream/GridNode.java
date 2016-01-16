package com.christianbaum.games.katsdream;

import java.util.Deque;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** A structure that contains the position, and whether it can travel
 * up, down, left, or right.
 * @author Christian Baum
 *
 */
public class GridNode extends Point {
	public Dir lastdir;
	public  Dir dir;
	
	/** This function 
	 * Collision type 1 is impassible.
	 * 
	 * @param pos The X and Y position of the node.
	 * @param map The map.
	 */
	GridNode( Point pos ) {
		super( pos );
		lastdir = Dir.NO;
		dir = Dir.NO;
	}
	
	GridNode( float x, float y ) {
		super( x, y );
		lastdir = Dir.NO;
		dir = Dir.NO;
	}
	
	/** Gets the next point to traverse in the direction.
	 * @param current Current point
	 * @param dest Destination point
	 * @param map Level (to test if a part can be traversed or not)
	 * @return The next point
	current_dir */
	private GridNode next_point ( Point dest, Level map ) {
		if( dest.y() > y &&
			canTraverse( dirToPoint( Dir.DOWN ), this, map))
			return new GridNode(dirToPoint( Dir.DOWN));
		else if( dest.y < y &&
				canTraverse( dirToPoint( Dir.UP ), this, map))
			return new GridNode( dirToPoint(Dir.UP));
		else if( dest.x < x &&
				canTraverse( dirToPoint( Dir.LEFT ), this, map))
			return new GridNode( dirToPoint(Dir.LEFT));
		else if( dest.x > x && 
				canTraverse( dirToPoint( Dir.RIGHT), this, map))
			return new GridNode( dirToPoint(Dir.RIGHT));
		return null;
	}
	
	/** Subtracts the left_map_width from the location x and destination x.
	 * 
	 * @param left_screen_width The width of the left map that was replaced.
	 */
	public void updateXFromMapChange( int left_screen_width ) {
		setX( x() - left_screen_width );
	}
	
	/** Checks if the terrain is blocked or not
	 * 
	 * @param start The point to start from
	 * @param end The destination of the path
	 * @param map The map
	 * @return
	 */
	public static boolean canTraverse( 
			Point point, Point player_pos, Level map ) {
		if( player_pos.equals( point.x(), point.y() - 1 ) ||
			player_pos.equals( point.x(), point.y() + 1 ) || 
			player_pos.equals( point.x() + 1, point.y() ) ||
			player_pos.equals( point.x() - 1, point.y() ) )
			return map.getCollisionType( point ) != 1; 
		return false;
	}
	
	public TextureRegion getTexture( TextureRegion texture[][]) {
		int indexV = lastdir.index();
		int indexH = dir.index();
		if( indexH == 0 && indexV == 1 ||
			indexH == 1 && indexV == 3 ||
			indexH == 2 && indexV == 0 ||
			indexH == 3 && indexV == 2) {
			int temp = indexH;
			indexH = indexV;
			indexV = temp;
		}
		else if( indexV == 4 ) {
			switch( indexH ) {
			case 0:
				indexH = 2;
				indexV = 0;
				break;
			case 1:
				indexH = 1;
				indexV = 3;
				break;
			case 2:
				indexH = 0;
				indexV = 1;
				break;
			case 3:
				indexH = 3;
				indexV = 2;
				break;
			default:
				indexH = 3;
				indexV = 2;
			}
		}
		else if( indexH == 4 ) {
			indexV = indexH =  3 - indexV;
		}
		return texture[indexV][indexH];
	}

	public static Deque<GridNode> findPath( Point start, Point end, Level map) {
		Deque<GridNode> path      = new LinkedList<GridNode>();
		GridNode current_gridnode = new GridNode( start );
		GridNode next_gridnode;
		int passes=0;
		do{
			if( current_gridnode.x() < 0 ||
				current_gridnode.y() < 0 ||
				current_gridnode.x() > map.levelWidth()  ||
				current_gridnode.y() > map.levelHeight() ||
				current_gridnode.equals( end ))
				return path;
			next_gridnode = current_gridnode.next_point( end, map );
			if( next_gridnode != null ) {
				path.add( next_gridnode );
				//current_gridnode.attempts = 0;
				current_gridnode = new GridNode(next_gridnode);
			}
			else {
				//current_gridnode.attempts++;
				if( !path.isEmpty())
					current_gridnode = path.removeLast();
			}
			passes++;
		} while (!path.isEmpty() && passes < 10);
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
	
	public void setLastDir( Dir lastdir) {
		this.lastdir = lastdir;
	}
	
	public void setDir( Dir dir) {
		this.dir = dir;
	}
	
}
 