package com.christianbaum.games.katsdream;

import java.util.ArrayList;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** An Actor is a superclass skeleton
 * @author Christian
 */
public abstract class Actor {
	/** The actual position of the player */
	protected Point pos;
	/** The movement vector **/
	protected Vect movement_vector;
	/** The end target for the path */
	protected Point path_target;
	/** The LibGDX animation for the walk animation */
	protected Animation anim;
	/** The timer for animations */
	protected float anim_timer;
	/** The size of the hitbox for the actor */
	protected Rectangle hitbox_size;
	/** The size of the drawing; x=width, y=height */
	protected Point drawbox;
	/** The enum for the actor state */
	protected enum State { STANDING, MOVING, DEAD, EXPLODING, INACTIVE } ;
	/** The current state of the actor */
	protected State state;
	/** The direction the Actor is facing */
	private Dir direction;
	/** The hit points that the Actor has. */
	protected int health;
	/** The speed that the actor will move */
	protected float speed;
	/** Whether or not the death sprite has been set */
	protected boolean dead_sprite_set;
	
	/**
	 * The constructor of the actor. Creates an actor from an X and Y position
	 * @param x the start X position
	 * @param y the start Y position
	 */
	Actor( float x, float y) {
		pos = new Point(x,y);
		anim_timer = 0;
		direction = Dir.DOWN;
		state = State.STANDING;
		speed = 2;
		movement_vector = new Vect( new Point(x,y), new Point(x,y), 0 );
		hitbox_size = new Rectangle( 0f, 0f, 1f, 1f );
		drawbox = new Point(1,1);
	}

	/** Updates the actor
	 * 
	 * @param dt delta time
	 * @param world The world
	 * @param actors_to_add Any actors created by other actors
	 */
	protected abstract void update( float dt, KatsDream world,
			ArrayList<Actor> actors_to_add );
	
	/** draws the sprite
	 * 
	 * @param batch The Batch to draw to
	 * @param w the World
	 */
	public void draw(Batch batch, KatsDream w ) {
		batch.draw(getCurrentFrame( w.texture_region[getTextureRegion()] ), 
				pos.x*w.cam_width/w.tiles_per_cam_width -
				w.l.getScroll() * w.cam_width/w.tiles_per_cam_width,
				w.cam_height - (pos.y + 1) * w.cam_height/w.tiles_per_cam_height,
				(w.cam_width/w.tiles_per_cam_width)*drawbox.x,
				(w.cam_height/w.tiles_per_cam_height)*drawbox.y);
	}
	
	/** Gets the texture region of the actor
	 * 
	 * @return The texture region of whatever actor this is
	 */
	protected abstract int getTextureRegion();
	
	/** Gets the current frame for walking 
	 * 
	 * @param texture_region
	 * @return
	 */
	protected TextureRegion getCurrentFrame( TextureRegion[][] texture_region ){
		if(state == State.STANDING)
			return texture_region[direction.index()][0];
		else if( state == State.MOVING)
			return anim.getKeyFrame( (float) anim_timer, true);
		else if( state == State.DEAD)
			return anim.getKeyFrame( (float) anim_timer, false);
		return texture_region[direction.index()][0];
	}

	/** Updates the movement vector
	 * 
	 * @param dt delta time
	 */
	protected void move( float dt ) {
		movement_vector.move( dt );
		pos = new Point( movement_vector.loc() );
	}
	
	/** Offsets the X position for when the map changes
	 * 
	 * @param left_map_width the amount to offset by
	 */
	protected void updateXFromMapChange( int left_map_width ) {
		movement_vector.updateXFromMapChange( left_map_width );
		pos.updateXFromMapChange( left_map_width );
		if( path_target != null )
			path_target.updateXFromMapChange( left_map_width );
	}
	
	/** Returns true if the direction needs updated
	 * 
	 * @return whether the direction needs updated
	 */
	protected boolean updateDirection() {
		if( state == State.DEAD)
			return false;
		Dir last_dir = direction;
		if( Math.abs( movement_vector.y_spd() ) > 
		Math.abs( movement_vector.x_spd() ) )
			if ( movement_vector.y_spd() > 0)
				this.direction = Dir.DOWN;
			else 
				this.direction = Dir.UP;
		else
			if (movement_vector.x_spd() > 0 )
				this.direction = Dir.RIGHT;
			else
				this.direction = Dir.LEFT;
		return direction == last_dir;
	}
	
	/** Updates the animation frames for a walking actor
	 * 
	 * @param texture_region The texture region
	 */
	protected void updateWalkFrames( TextureRegion[][] texture_region ) {
		TextureRegion[] walk_frames = new TextureRegion[4];
		for(int i=0; i < 3; i++)
			if( state != State.DEAD )
				walk_frames[i] = texture_region[direction.index()][i];
			else
				walk_frames[i] = texture_region[i][3];
		walk_frames[3] = texture_region[direction.index()][1];
		anim = new Animation( 0.1f, walk_frames);
	}
	
	/** Returns the updated hitbox offsetted by the x and y pos
	 * 
	 * @return the updated hitbox offsetted by the x and y pos
	 */
	public Rectangle hitbox() {
		return new Rectangle( pos.x + hitbox_size.x, pos.y 
				+ hitbox_size.y, hitbox_size.width, hitbox_size.height );
	}
	
	public abstract void notifyOfCollision();
	
	/** Returns the tile the Actor is currently on
	 * 
	 * @return the closest tile
	 */
	public Point tilePos() {
		return new Point( Math.round(pos.x),
				Math.round(pos.y) );
	}
	
	/** Whether the actor can be deleted or not.
	 *  Most actors override this.
	 * 
	 * @return true if the enemy can be deleted
	 */
	protected boolean okayToDelete( ) {
		return state == State.DEAD;
	}
	
	public boolean isExploding() {
		return state == State.EXPLODING;
	}
	
	public void kill() {
		state = State.DEAD;
	}
}
