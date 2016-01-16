package com.christianbaum.games.katsdream;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Bullet extends Actor {

	private boolean player_bullet;
	
	Bullet(float x1, float y1, float x2, float y2, float speed,
			boolean player_bullet, TextureRegion[][] texture_region) {
		super(x1, y1);
		setMovementVector( new Vect( new Point( x1, y1 ), 
				                     new Point( x2, y2 ), speed ));
		this.player_bullet = player_bullet;
		initializeTexture( texture_region );
		hitbox_size = new Rectangle( 0f, 0f, 0.5f, 0.5f );
		state = State.MOVING;
		drawbox = new Point(0.5f, 0.5f);
	}

	Bullet(Point loc, Point dest, float speed, 
			boolean player_bullet, TextureRegion[][] texture_region ) {
		super(loc);
		setMovementVector( new Vect( loc, dest, speed ) );
		this.player_bullet = player_bullet;
		initializeTexture( texture_region );
		hitbox_size = new Rectangle( 0f, 0f, 0.5f, 0.5f );
		state = State.MOVING;
		drawbox = new Point(0.5f, 0.5f);
	}
	
	private void initializeTexture( TextureRegion[][] texture_region ) {
		TextureRegion[]	walk_frames = new TextureRegion[4];
		walk_frames[0] = texture_region[0][0];
		walk_frames[1] = texture_region[0][1];
		walk_frames[2] = texture_region[1][0];
		walk_frames[3] = texture_region[1][1];
		anim = new Animation( 0.05f, walk_frames );
	}

	public boolean isColliding( KatsDream w ) {
		if( hitbox().x < 0-(hitbox().width/16) || pos().x() > w.tiles_per_cam_width + w.l.getScroll() || 
			hitbox().y < 0-(hitbox().height/16) || pos().y() > w.tiles_per_cam_height ) {
			setState(State.DEAD);
			return true;
		}
		for( Actor  actor : w.actors )
			if ( hitbox().overlaps(actor.hitbox() ) &&
				actor.state != State.DEAD &&
				actor.getClass() != Bullet.class)
				if(  player_bullet && actor.getClass() != Player.class ||
					!player_bullet && actor.getClass() == Player.class) {
					if( actor.getClass() == Cart.class )
						w.sfx[5].play();
					else
						w.sfx[(int) (1+Math.round(Math.random()))].play();
					setState( State.DEAD );
					actor.notifyOfCollision();
					return true;
				}
		return false;
	}
	
	@Override
	public boolean okayToDelete() {
		return state() == State.DEAD;
	}
	
	@Override
	public void update( float dt, KatsDream w, ArrayList<Actor> actors_to_add ) {
		super.update( dt, w, actors_to_add );
		super.move( dt );
		isColliding( w );
	}

	@Override
	public void notifyOfCollision() {
		//do bullets collide with other bullets?
	}

}
