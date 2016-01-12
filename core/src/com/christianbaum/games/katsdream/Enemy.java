package com.christianbaum.games.katsdream;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Enemy extends Actor {
	
	Enemy(Point pos) {
		super(pos);
	}

	Enemy(float x, float y) {
		super(x, y);
	}
	
	public abstract Bullet updateAI(float dt, Player player, Level l,
			TextureRegion[][] region );
	
	protected void updatePathFromTarget(float dt, Level level, Point target) {
		if( path.isEmpty() || !path_target.equals( target )) {
			path_target = target;
			findPath(path_target, level);
		}
		else {
			followPath( dt );
		}
	}
	
	protected boolean okayToDelete( ) {
		return state == State.DEAD && anim_timer > 1
				|| tile_pos.x() < 0 ;
		
	}
}
