package com.christianbaum.games.katsdream;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class Player extends Actor {

	Player(int x, int y, TextureRegion[][] texture) {
		super(x, y);
		// TODO Auto-generated constructor stub
		updateWalkFrames(texture);
		setHealth(3);
		
	}

	public boolean isCollidingWithBullet( Actor other_actors[] ) {
		for( Actor other_actor : other_actors )
			if ( other_actor instanceof Bullet) {
				setHealth( health() - 1);
				return true;
			}
		
		return false;
	}public boolean okayToDelete() {
		return false;
	}
	
	public void draw(SpriteBatch batch, TextureRegion[][] texture_region, 
			float left_screen_scroll, int cam_width, int cam_height,
			int tiles_per_cam_width, int tiles_per_cam_height ) {
		if( updateDirection() )
			updateWalkFrames( texture_region );
		super.draw(batch, texture_region, left_screen_scroll, 
				cam_width, cam_height,tiles_per_cam_width,tiles_per_cam_height);
	}
	
	public void update( float dt ) {
		super.update( dt);
		if( !followPath( dt ) )
			setState( State.MOVING );
		else
			setState( State.STANDING );		
	}
	
	public boolean canShoot( boolean bonus_level ) {
		if( bulletTimer() > 0.75f || bonus_level && bulletTimer() >= 0.1f) {
			resetBulletTimer();
			return true;
		}
		return false;
	}	
}
