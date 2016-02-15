package com.christianbaum.games.katsdream;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

/** A structure that contains the position, and whether it can travel
 * up, down, left, or right.
 * @author Christian Baum
 *
 */
public class Path {
	protected static class GridNode extends Point{
		public float g;
		public float h;
		public GridNode parent;

		/** Creates a GridNode from an X and Y value
		 * 
		 * @param x GridNode's x value
		 * @param y GridNOde's y value
		 */
		GridNode( float x, float y ) {
			super( x, y );
			g=0;
			h=0;
			parent = null;
		}

		/** Creates a GridNode from a Point/GridNode
		 * @param pos The X and Y position of the node.
		 * @param map The map.
		 */
		GridNode( Point pos ) {
			this( pos.x, pos.y );
		}
	}
	
	public static Deque<?> findPath( Point start, Point end, Level map, int max_attempts) {
		GridNode x = AStarPath( start, end, map, max_attempts);
		if( x == null )
			return new LinkedList<Point>();
		return reconstructPath( x );
	}
	
	public static GridNode AStarPath( Point start, Point end, Level map, int max_attempts) { 
		ArrayList<GridNode> closed_set = new ArrayList<GridNode>();
		PriorityQueue<GridNode> open_set = new PriorityQueue<GridNode>( 8, new AStarComparator());
		HashMap<Point, Float> open_set_lookup = new HashMap<Point, Float>();
		open_set.add( new GridNode(start) );
		int count = 0;
		while( !open_set.isEmpty() ) {
			  GridNode current = open_set.remove();
			  if( current.equals( end ) ){
				  //Must be reconstructed
				  return current;
			  }
			  else if( count >= max_attempts ) {
				  return null;
			  }
			  closed_set.add( current );
			  for( int i = 0; i < 4; i++ ) {
				  GridNode neighbor = new GridNode( current.dirToPoint(Dir.fromIndex(i)));
				  if( map.getCollisionType( neighbor ) != 1 ) {
					  neighbor.g = current.g + 1;
					  neighbor.h = Math.abs( neighbor.x - end.x ) + Math.abs( neighbor.y - end.y );
					  neighbor.parent = current;
					  if( closed_set.contains( neighbor ) ) {
						  continue;
					  }
					  if( !open_set_lookup.containsKey( neighbor ) ) {
						  open_set.add( neighbor );
						  open_set_lookup.put( neighbor, neighbor.g+neighbor.h);
					  }
					  else if( open_set_lookup.get(neighbor) > neighbor.g + neighbor.h ) {
						  open_set.remove( neighbor );
						  open_set_lookup.remove( neighbor);
						  open_set.add( neighbor);
						  open_set_lookup.put( neighbor, neighbor.g+neighbor.h);
					  }
				  }
			  }
			  count++;
		}
		//failure
		return null;
	}	
	
	public static Deque<?> reconstructPath(GridNode node) {
		Deque<Point> x = new LinkedList<Point>();
		while( node.parent != null ) {
			x.addFirst( node );
			node = node.parent;
		}
		return x;
	}

	private static class AStarComparator implements Comparator<GridNode> {
		@Override 
		public int compare( GridNode g1, GridNode g2 ) {
			return g1.g + g1.h > g2.g + g2.h ? 1 : -1;
		}
	}
	
	public static Deque<?> updateXFromMapChange( Deque<?> path,
			int left_map_width ) {
		Deque<Point> new_path = new LinkedList<Point>();
		while( !path.isEmpty() ) {
			Point node = (Point) path.remove() ;
			node.updateXFromMapChange( left_map_width );
			new_path.add( node );
		}
		return new_path;
	}
}
