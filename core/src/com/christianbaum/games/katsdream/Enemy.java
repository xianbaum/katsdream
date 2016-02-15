package com.christianbaum.games.katsdream;

public abstract class Enemy extends Actor {

	/** Initializes the enemy
	 * @param x THe x coordinate
	 * @param y The Y coordinate
	 */
	Enemy(float x, float y) {
		super(x, y);
	}
	
	@Override
	protected boolean okayToDelete( ) {
		return state == State.DEAD && anim_timer > 1
				|| tilePos().x < 0 ;
		
	}
	
	/**
	 * Checks if the actor is outside of the screen or not.
	 * @param scroll
	 * @param tiles_per_cam_height
	 * @return True if out of bounds (does it need to be boolean?)
	 */
	protected void updateEnemy( KatsDream w ) {
		if( state == State.INACTIVE && 
			tilePos().x < w.l.getScroll() + w.tiles_per_cam_width ) {
				state = State.MOVING;
		}
		else if( pos.x < w.l.getScroll() -1 || pos.y > w.tiles_per_cam_height ||
			pos.y < -1 ) {
			state = State.DEAD;
		}
		if( w.state == KatsDream.GameState.PLAYING && 
				state != State.DEAD && 
				w.actors.get(0).hitbox().overlaps( hitbox()) )
			w.actors.get(0).notifyOfCollision();
	}
}
