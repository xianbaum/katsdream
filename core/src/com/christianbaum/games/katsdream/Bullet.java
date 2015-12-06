package com.christianbaum.games.katsdream;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Bullet extends Actor {

	private boolean player_bullet;
	
	Bullet(float x1, float y1, float x2, float y2, float speed,
			boolean player_bullet, TextureRegion[][] texture_region) {
		super(x1, y1);
		initializeMovementVector( new Point( x1, y1 ), new Point( x2, y2 ), 
				speed );
		this.player_bullet = player_bullet;
		initializeTexture( texture_region );
	}

	Bullet(Point loc, Point dest, float speed, 
			boolean player_bullet, TextureRegion[][] texture_region ) {
		super(loc);
		initializeMovementVector( loc, dest, speed );
		this.player_bullet = player_bullet;
		initializeTexture( texture_region );
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

	public boolean isColliding( Player player, ArrayList<Actor> other_actors, 
			float scroll, int tiles_per_cam_width, int tiles_per_cam_height ) {
		if( pos().x() < 0 || pos().x() > tiles_per_cam_width + scroll || 
			pos().y() < 0 || pos().y() > tiles_per_cam_height ) {
			setState(State.DEAD);
			return true;
		}
		
		if( hitbox().overlaps( player.hitbox() )) {
			player.notifyOfCollision();
			setState(State.DEAD);
			return true;
		}
		
		for( Actor  other_actor : other_actors )
			if( !player_bullet )
				if ( hitbox().overlaps(other_actor.hitbox() ) ) {
					setState( State.DEAD );
					other_actor.notifyOfCollision();
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
	
	public void draw2(SpriteBatch batch, float left_screen_scroll, 
			int cam_width, int cam_height, int tiles_per_cam_width,
			int tiles_per_cam_height ) {
		batch.draw( anim.getKeyFrame( walk_timer, true),
				pos().x()*cam_height/tiles_per_cam_height -
				left_screen_scroll * 32f,
				cam_height - (pos().y() + 1) * cam_width/tiles_per_cam_width,
				cam_width/tiles_per_cam_width/2, 
				cam_height/tiles_per_cam_height/2);
	}

}
