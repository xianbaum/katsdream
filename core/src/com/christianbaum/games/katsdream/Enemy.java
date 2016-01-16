package com.christianbaum.games.katsdream;

import java.util.ArrayList;

public abstract class Enemy extends Actor {
	
	Enemy(Point pos) {
		super(pos);
	}

	Enemy(float x, float y) {
		super(x, y);
	}
	
	@Override
	public void update( float dt, KatsDream w,
			ArrayList<Actor> actors_to_add ) {
		super.update(dt, w, actors_to_add);
	}
	
	protected void updatePathFromTarget(float dt, Level level, Point target) {
		if( path.isEmpty() || !path_target.equals( target )) {
			path_target = target;
			findPath(path_target, level);
		}
		else {
			followPath( dt );
		}
	}
	
	@Override
	protected boolean okayToDelete( ) {
		return state == State.DEAD && anim_timer > 1
				|| tile_pos.x() < 0 ;
		
	}
	
	/**
	 * Checks if the actor is outside of the screen or not.
	 * @param scroll
	 * @param tiles_per_cam_height
	 * @return True if out of bounds (does it need to be boolean?)
	 */
	protected void updateEnemy( KatsDream w ) {
		if( state == State.INACTIVE && 
			tile_pos.x() < w.l.getScroll() + w.tiles_per_cam_width ) {
				state = State.MOVING;
		}
		else if( pos.x() < -1 || pos.y() > w.tiles_per_cam_height ||
			pos.y() < -1 ) {
			state = State.DEAD;
		}
		if( isColliding( w.actors.get(0) ) )
			w.actors.get(0).notifyOfCollision();
	}
}
