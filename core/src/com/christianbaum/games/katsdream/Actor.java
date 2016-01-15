package com.christianbaum.games.katsdream;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import com.badlogic.gdx.audio.Sound;
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
	/** The tile the actor is fixated on */
	protected Point tile_pos;
	/** The movement vector **/
	protected Vect movement_vector;
	/** The end target for the path */
	protected Point path_target;
	/** The path finding points */
	protected Deque<GridNode> path;
	/** The LibGDX animation for the walk animation */
	protected Animation anim;
	/** The timer for animations */
	protected float anim_timer;
	/** The size of the hitbox for the actor */
	protected Rectangle hitbox_size;
	/** The enum for the actor state */
	protected enum State { STANDING, MOVING, DEAD, INACTIVE } ;
	/** The current state of the actor */
	protected State state;
	/** The direction the Actor is facing */
	private Dir direction;
	/** The hit points that the Actor has. */
	protected int health;
	/** The speed that the actor will move */
	protected float speed;
	/** Whether or not the actor has collided or not */
	protected boolean notified_of_collision;
	/** The amount of time the actor has to wait to fire another bullet*/
	private float bullet_timer;
	/** The amount of time before the actor can take damage again */
	private float cooldown_timer;
	/** Whether or not the death sprite has been set */
	protected boolean dead_sprite_set;
	
	/**
	 * The constructor of the actor. Creates an actor from an X and Y position
	 * @param x the start X position
	 * @param y the start Y position
	 */
	Actor( float x, float y) {
		pos = new Point(x,y);
		tile_pos = new Point(x,y);
		anim_timer = 0;
		direction = Dir.DOWN;
		state = State.STANDING;
		path = new LinkedList<GridNode>();
		speed = 2;
		movement_vector = new Vect( new Point(x,y), new Point(x,y), 0 );
		hitbox_size = new Rectangle( 0f, 0f, 1f, 1f );
	}
	
	Actor( Point point ) {
		this( (int)point.x(), (int)point.y());
	}
	
	protected TextureRegion getCurrentFrame( TextureRegion[][] texture_region ){
		if(state == State.STANDING)
			return texture_region[direction.index()][0];
		else if( state == State.MOVING)
			return anim.getKeyFrame( (float) anim_timer, true);
		else if( state == State.DEAD)
			return anim.getKeyFrame( (float) anim_timer, true);
		return texture_region[direction.index()][0];
	}

	protected void move( float dt ) {
		movement_vector.move( dt );
		pos = new Point( movement_vector.loc() );
		tile_pos = new Point( movement_vector.loc() );
	}
	
	public void draw(Batch batch, TextureRegion[][] texture_region, 
			float left_screen_scroll, int cam_width, int cam_height,
			int tiles_per_cam_width, int tiles_per_cam_height ) {
		batch.draw(getCurrentFrame( texture_region ), 
				pos.x()*cam_width/tiles_per_cam_width -
				left_screen_scroll * cam_width/tiles_per_cam_width,
				cam_height - (pos.y() + 1) * cam_height/tiles_per_cam_height,
				cam_width/tiles_per_cam_width, cam_height/tiles_per_cam_height);
	}
	
	protected void updateXFromMapChange( int left_map_width ) {
		movement_vector.updateXFromMapChange( left_map_width );
		pos.updateXFromMapChange( left_map_width );
		tile_pos.updateXFromMapChange( left_map_width );
		if( path_target != null )
			path_target.updateXFromMapChange( left_map_width );
		path = GridNode.updateXFromMapChange(path, left_map_width);
	}
	
	protected boolean updateDirection() {
		if( state == State.DEAD)
			return false;
		Dir last_dir = direction;
		if( Math.abs( movementVector().y_spd() ) > 
		Math.abs( movementVector().x_spd() ) )
			if ( movementVector().y_spd() > 0)
				this.direction = Dir.DOWN;
			else 
				this.direction = Dir.UP;
		else
			if (movementVector().x_spd() > 0 )
				this.direction = Dir.RIGHT;
			else
				this.direction = Dir.LEFT;
		return direction == last_dir;
	}
	
	protected void findPath( Point end, Level map ) {
		path = GridNode.findPath(tilePos(), end, map);
	}
	
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

	protected void updateDeadFrames( TextureRegion[][] texture_region ) {
		TextureRegion[] walk_frames = new TextureRegion[4];
		for(int i = 0; i < 4; i++)
			walk_frames[0] = texture_region[i][3];
		anim = new Animation( 0.2f, walk_frames);
	}
	
	protected int health() {
		return health;
	}
	
	protected void setHealth(int health) {
		this.health = health;
	}
	
	protected void subtractHealth() {
		if( cooldown_timer < 1f )
			health--;
		cooldown_timer = 0;
	}
	
	protected boolean isColliding( Rectangle other_hitbox ) {
		
		return hitbox().overlaps(other_hitbox);
	}
	
	public Rectangle hitbox() {
		return new Rectangle( pos.x() + hitbox_size.x, pos.y() 
				+ hitbox_size.y, hitbox_size.width, hitbox_size.height );
	}

	protected boolean isColliding(ArrayList<Actor> other_actors ) {
		for ( Actor other_actor : other_actors )
			if( !(state == State.DEAD) &&
				 other_actor.hitbox().overlaps( hitbox() ))
			{
				other_actor.notifyOfCollision();
				return true;
			}
		return false;
	}
	
	protected boolean isColliding( Point other_tile_pos ) {
		return !(state == State.DEAD) && other_tile_pos.equals(tile_pos);
	}
	
	
	public boolean isColliding( Player player ) {
		return !(state == State.DEAD) && player.hitbox().overlaps( hitbox() );
	}
	
	/**
	 * Checks if the actor is outside of the screen or not.
	 * @param scroll
	 * @param tiles_per_cam_height
	 * @return True if out of bounds (does it need to be boolean?)
	 */
	protected boolean isOutOfBounds( float left_screen_scroll,
			int tiles_per_cam_width, int tiles_per_cam_height,
			Sound[] sfx) {
		if( state == State.INACTIVE)
			if( tile_pos.x() < 
				left_screen_scroll ) {
				state = State.MOVING;
				if( getClass() == Plane.class )
					sfx[4].play();
			}
		else if( pos.x() < -1 || pos.y() > tiles_per_cam_height ||
			pos.y() < -1 ) {
			state = State.DEAD;
			return true;
		}
		return false;
	}
	
	public Point pos() {
		return pos;
	}
	
	public Point tilePos() {
		return new Point( Math.round(tile_pos.x()),
				Math.round(tile_pos.y()) );
	}

	protected void update( float dt ) {
		anim_timer += dt;
		bullet_timer += dt;
		notified_of_collision = false;
	}

	public State state() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
	
	/** Skeleton method for okayToDelete. */
	protected boolean okayToDelete( ) {
		return state == State.DEAD;
	}

	public Vect movementVector() {
		return movement_vector;
	}
	
	/**
	 * 
	 * @return True if the path is empty.
	 */
	protected boolean followPath( float dt ) {
		if(movement_vector.arrived() && !path.isEmpty() ) {
			GridNode x = path.remove();
			movement_vector = new Vect( pos, x, speed ); 
			}
		else if( !movement_vector.arrived() ) 
			move( dt );
		return path.isEmpty() && movement_vector.arrived();
	}
	
	public void addToPath( GridNode node ) {
		path.add( node );
	}
	
	protected void setMovementVector( Vect new_mv )  {
		movement_vector = new_mv;
	}
	
	public Point lastPathPos() {
		if( !path.isEmpty() )
			return path.getLast();
		else
			return movement_vector.dest();
	}
	
	public void clearPath() {
		path.clear();
	}
	
	protected float bulletTimer() {
		return bullet_timer;
	}
	
	protected void resetBulletTimer() {
		bullet_timer = 0;
	}
	
	public void notifyOfCollision() {
		notified_of_collision = true;
	}
	
	public boolean  notifiedOfCollision() {
		return notified_of_collision;
	}
	
	public boolean canShoot( boolean cool_level ) {
		return false;
	}
	
	public int getTextureRegion() {
		return 0;
	}
	
	public void kill() {
		state = State.DEAD;
		anim_timer = 0;
	}
}
