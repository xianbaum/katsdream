package com.christianbaum.games.katsdream;

import java.util.ArrayList;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Bullet extends Actor {

	private boolean player_bullet;
	
	Bullet(float x1, float y1, float x2, float y2, float speed,
			boolean player_bullet, TextureRegion[][] texture_region) {
		super(x1, y1);
		initializeMovementVector( new Point( x1, y1 ), new Point( x2, y2 ), 
				speed );
		this.player_bullet = player_bullet;
		initializeTexture( texture_region );
		hitbox_size = new Rectangle( 0f, 0f, 0.5f, 0.5f );
	}

	Bullet(Point loc, Point dest, float speed, 
			boolean player_bullet, TextureRegion[][] texture_region ) {
		super(loc);
		initializeMovementVector( loc, dest, speed );
		this.player_bullet = player_bullet;
		initializeTexture( texture_region );
		hitbox_size = new Rectangle( 0f, 0f, 0.5f, 0.5f );
	}
	
	private void initializeMovementVector(Point loc, Point dest, float speed ) {
		setMovementVector( new Vect( loc, dest, speed ) );
	}
	
	private void initializeTexture( TextureRegion[][] texture_region ) {
		TextureRegion[]	walk_frames = new TextureRegion[4];
		walk_frames[0] = texture_region[0][0];
		walk_frames[1] = texture_region[0][1];
		walk_frames[2] = texture_region[1][0];
		walk_frames[3] = texture_region[1][1];
		anim = new Animation( 0.05f, walk_frames );
	}

	public boolean isColliding( Player player, ArrayList<Enemy> enemies, 
			float scroll, int tiles_per_cam_width, int tiles_per_cam_height, Sound [] sfx) {
		if( hitbox().x < 0-(hitbox().width/16) || pos().x() > tiles_per_cam_width + scroll || 
			hitbox().y < 0-(hitbox().height/16) || pos().y() > tiles_per_cam_height ) {
			setState(State.DEAD);
			return true;
		}
		
		if( hitbox().overlaps( player.hitbox() ) && !player_bullet ) {
			player.notifyOfCollision();
			setState(State.DEAD);
			return true;
		}
		
		for( Actor  enemy : enemies )
			if( player_bullet )
				if ( hitbox().overlaps(enemy.hitbox() ) &&
						enemy.state != State.DEAD) {
					if( enemy.getClass() == Cart.class )
						sfx[5].play();
					else
						sfx[(int) (1+Math.round(Math.random()))].play();
					setState( State.DEAD );
					enemy.notifyOfCollision();
					return true;
				}
		return false;
	}
	
	public boolean okayToDelete() {
		return state() == State.DEAD;
	}
	
	public void update( float dt ) {
		super.update( dt );
		super.move( dt );
	}
	
	public void draw(Batch batch, float left_screen_scroll, 
			int cam_width, int cam_height, int tiles_per_cam_width,
			int tiles_per_cam_height ) {
		batch.draw( anim.getKeyFrame( anim_timer, true),
				pos.x()*cam_width/tiles_per_cam_width -
				left_screen_scroll * cam_width/tiles_per_cam_width,
				cam_height - (pos.y() + 1) * cam_height/tiles_per_cam_height,
				cam_width/tiles_per_cam_width/2, cam_height/tiles_per_cam_height/2);

	}

	@Override
	public void notifyOfCollision() {
		//do bullets collide with other bullets?
	}

}
