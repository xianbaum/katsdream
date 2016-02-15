package com.christianbaum.games.katsdream;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Bullet extends Actor {

	//Whether the bullet will hurt enemies or the player
	private boolean player_bullet;
	
	/** Creates a bullet with two points, 
	 * a speed, whether the bullet is a player bullet
	 * and a texture region
	 * 
	 * @param loc The start location
	 * @param dest The end location
	 * @param speed the bullet's speed
	 * @param player_bullet Whether the bullet came from a player or not
	 * @param texture_region The texture region
	 */
	Bullet(Point loc, Point dest, float speed, 
			boolean player_bullet, TextureRegion[][] texture_region ) {
		super(loc.x, loc.y);
		movement_vector = new Vect( loc, dest, speed );
		this.player_bullet = player_bullet;
		TextureRegion[]	walk_frames = new TextureRegion[4];
		walk_frames[0] = texture_region[0][0];
		walk_frames[1] = texture_region[0][1];
		walk_frames[2] = texture_region[1][0];
		walk_frames[3] = texture_region[1][1];
		anim = new Animation( 0.05f, walk_frames );
		hitbox_size = new Rectangle( 0f, 0f, 0.5f, 0.5f );
		state = State.MOVING;
		drawbox = new Point(0.5f, 0.5f);
	}
	
	/** Updates the bullet
	 * 
	 * @param dt delta time
	 * @param w The world
	 * @param actors_to_add Pointless for this
	 */
	@Override
	public void update( float dt, KatsDream w, ArrayList<Actor> actors_to_add ) {
		anim_timer += dt;
		move( dt );
		if( hitbox().x < 0-(hitbox().width/16) || pos.x > w.tiles_per_cam_width + w.l.getScroll() || 
				hitbox().y < 0-(hitbox().height/16) || pos.y > w.tiles_per_cam_height ) {
				state = State.DEAD;
				return;
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
						state = State.DEAD;
						actor.notifyOfCollision();
				}
	}
	
	//Bullets (probably) don't need to be notified of collision
	@Override
	public void notifyOfCollision() {}
	
	/** Returns the 0th texture
	 * 
	 */
	@Override
	public int getTextureRegion() {
		return 0;
	}

}
