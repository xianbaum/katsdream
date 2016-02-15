package com.christianbaum.games.katsdream;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class DrawnPath extends Path {

	public static class DrawnPoint extends Point {
		private Dir lastdir;
		private Dir dir;
		
		DrawnPoint (Point pos) {
			super(pos.x, pos.y);
			lastdir = Dir.NO;
			dir = Dir.NO;
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
					indexH = 2; indexV = 0; break;
				case 1:
					indexH = 1; indexV = 3; break;
				case 2:
					indexH = 0; indexV = 1; break;
				case 3:
					indexH = 3; indexV = 2; break;
				default:
					indexH = 3; indexV = 2; break;
				}
			}
			else if( indexH == 4 ) {
				indexV = indexH =  3 - indexV;
			}
			return texture[indexV][indexH];
		}
		public void setLastDir( Dir lastdir) {
			this.lastdir = lastdir;
		}
		
		public void setDir( Dir dir) {
			this.dir = dir;
		}
	}
	
	public static Deque<?> findDrawnPath( Point start, Point end, Level map, int max_attempts) {
		GridNode x = AStarPath( start, end, map, 100);
		if( x == null ) {
			return new LinkedList<DrawnPoint>();
		}
		return reconstructDrawnPath( x );
	}
	
	public static void draw( Batch batch, Deque<DrawnPoint> path, KatsDream w ) {
		Iterator<DrawnPoint> path_iter = path.iterator();
		while( path_iter.hasNext() ) {
			DrawnPoint node = path_iter.next();
			batch.draw( node.getTexture( w.texture_region[7]),
				node.x*w.cam_width/w.tiles_per_cam_width -
				w.l.getScroll() * w.cam_width/w.tiles_per_cam_width,
				w.cam_height - (node.y + 1) * w.cam_height/w.tiles_per_cam_height,
				w.cam_width/w.tiles_per_cam_width, w.cam_height/w.tiles_per_cam_height);
		}
	}
	
	public static Deque<?> reconstructDrawnPath(GridNode node) {
		Deque<DrawnPoint> x = new LinkedList<DrawnPoint>();
		Dir nextdir = Dir.NO;
		DrawnPoint dp = null;
		while( node.parent != null ) {
			dp = new DrawnPoint(node);
			if( nextdir != Dir.NO ) {
				dp.dir = nextdir.opposite();
			}
			if( node.parent != null )
				dp.lastdir = dp.pointToDir( node.parent);
			nextdir = dp.lastdir;
			node = node.parent;
			x.addFirst( dp);
		}
		if( !x.isEmpty())
			x.peekFirst().lastdir = Dir.NO;
		return x;
	}

	public static void append( Deque<DrawnPoint> p1, Deque<DrawnPoint> p2 ) {
		if( !p1.isEmpty() && !p2.isEmpty() ) {
			p1.peekLast().dir = p1.peekLast().pointToDir( p2.peekFirst() );
			p2.peekFirst().lastdir = p1.peekLast().dir.opposite();
			p1.addAll( p2 );
		}
	}
}
