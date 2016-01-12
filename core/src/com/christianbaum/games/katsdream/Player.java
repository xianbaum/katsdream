package com.christianbaum.games.katsdream;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class Player extends Actor {

	Player(int x, int y, TextureRegion[][] texture) {
		super(x, y);
		// TODO Auto-generated constructor stub
		updateWalkFrames(texture);
		health = 3;
	}

	public boolean isCollidingWithBullet( Actor other_actors[] ) {
		for( Actor other_actor : other_actors )
			if ( other_actor instanceof Bullet) {
				setHealth( health() - 1);
				return true;
			}
		return false;
	}
	
	public boolean okayToDelete() {
		return state == State.DEAD && anim_timer > 0.30f ? true : false;
	}
	
	@Override
	public void draw(Batch batch, TextureRegion[][] texture_region, 
			float left_screen_scroll, int cam_width, int cam_height,
			int tiles_per_cam_width, int tiles_per_cam_height ) {
		if( updateDirection() || state == State.DEAD && !dead_sprite_set)
			updateWalkFrames( texture_region );
		super.draw(batch, texture_region, left_screen_scroll, 
				cam_width, cam_height,tiles_per_cam_width,tiles_per_cam_height);
	}
	
	@Override
	public void update( float dt ) {
		super.update( dt);
		if( !followPath( dt*2 ) && state != State.DEAD )
			state = State.MOVING;
		else if( state == State.MOVING )
			state = State.STANDING;
		if(notified_of_collision)
			health -= 1;
		if(health <= 0 )
			state = State.DEAD;
	}
	
	
	
	public boolean canShoot( boolean bonus_level ) {
		if( bulletTimer() > 0.75f || bonus_level && bulletTimer() >= 0.1f) {
			resetBulletTimer();
			return true;
		}
		return false;
	}
	
	@Override
	public void notifyOfCollision() {
		health = health -1;
		if(health <= 0 && state != State.DEAD) {
			state = State.DEAD;
			anim_timer = 0;
		}
	}
	
	public boolean isOutOfBounds( float left_screen_scroll ) {
		if( pos.x() < left_screen_scroll -4 ) {
			state = State.DEAD;
			return true;
		}
		return false;
	}
}
