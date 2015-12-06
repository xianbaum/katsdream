package com.christianbaum.games.katsdream;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/** An Actor is a superclass skeleton
 * 
 * @author Christian
 *
 */
public class Actor {
	/** The actual position of the player */
	private Point pos;
	/** The tile the actor is fixated on */
	private Point tile_pos;
	/** The movement vector **/
	private Vect movement_vector;
	/** The end target for the path */
	private Point path_target;
	/** The path finding points */
	private Deque<GridNode> path;
	/** The LibGDX animation for the walk animation */
	protected Animation anim;
	/** The timer for animations */
	protected float walk_timer;
	/** The hit box of the actor */
	private Rectangle hitbox;
	/** The size of the hitbox for the actor */
	private Rectangle hitbox_size;
	/** The enum for the actor state */
	protected enum State { STANDING, MOVING, DEAD, INACTIVE } ;
	/** The current state of the actor */
	private State state;
	/** The direction the Actor is facing */
	private Dir direction;
	/** The hit points that the Actor has. */
	private int health;
	/** The speed that the actor will move */
	private float speed;
	/** Whether or not the actor has collided or not */
	private boolean notified_of_collision;
	/** The amount of time the actor has to wait to fire another bullet*/
	private float bullet_timer;
	/** The amount of time before the actor can take damage again */
	private float cooldown_timer;
	/** If the actor is out of bounds or not */
	private boolean out_of_bounds = false;
	
	/**
	 * The constructor of the actor. Creates an actor from an X and Y position
	 * @param x the start X position
	 * @param y the start Y position
	 */
	Actor( float x, float y) {
		pos = new Point(x,y);
		tile_pos = new Point(x,y);
		walk_timer = 0;
		direction = Dir.DOWN;
		state = State.STANDING;
		path = new LinkedList<GridNode>();
		speed = 2;
		movement_vector = new Vect( new Point(x,y), new Point(x,y), 0 );
		hitbox_size = new Rectangle( 4, 4, 8, 8 );
		hitbox = new Rectangle( 4, 4, 8, 8 );
	}
	
	Actor( Point point ) {
		this( (int)point.x(), (int)point.y());
	}
	
	//TODO: this
	protected TextureRegion getCurrentFrame( TextureRegion[][] texture_region ){
		if(state == State.STANDING)
			return texture_region[direction.index()][0];
		else if( state == State.MOVING)
			return anim.getKeyFrame( (float) walk_timer, true);
		else if( state == State.DEAD)
			return anim.getKeyFrame( (float) walk_timer, true);
		else
			return texture_region[direction.index()][0];
	}

	//TODO: this
	protected void move( float dt ) {
		movement_vector.move( dt );
		pos = new Point( movement_vector.loc() );
		tile_pos = new Point( movement_vector.loc() );
		hitbox.setPosition( pos.x()+hitbox_size.x, pos.y()+hitbox_size.y );
	}
	
	//TODO: this
	public void draw(SpriteBatch batch, TextureRegion[][] texture_region, 
			float left_screen_scroll, int cam_width, int cam_height,
			int tiles_per_cam_width, int tiles_per_cam_height ) {
		batch.draw(getCurrentFrame( texture_region ), 
				pos.x()*cam_height/tiles_per_cam_height -
				left_screen_scroll * 32f,
				cam_height - (pos.y() + 1) * cam_width/tiles_per_cam_width,
				cam_width/tiles_per_cam_width, cam_height/tiles_per_cam_height);
	}
	
	protected void updateXFromMapChange( int left_map_width ) {
		movement_vector.updateXFromMapChange( left_map_width );
		pos.updateXFromMapChange( left_map_width );
		tile_pos.updateXFromMapChange( left_map_width );
		if( path_target != null )
			path_target.updateXFromMapChange( left_map_width );
		path = GridNode.updateXFromMapChange(path, left_map_width);
		hitbox.setPosition( pos.x()+hitbox_size.x, pos.y()+hitbox_size.y );
	}
	
	protected boolean updateDirection() {
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
		path = GridNode.findPath(pos, end, map);
	}
	
	protected void updateWalkFrames( TextureRegion[][] texture_region ) {
		TextureRegion[] walk_frames = new TextureRegion[4];
		for(int i=0; i < 3; i++)
			walk_frames[i] = texture_region[direction.index()][i];
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
	
	//TODO: possibly less functions
	protected boolean isColliding( Rectangle other_hitbox ) {
		return hitbox.overlaps(other_hitbox);
	}
	
	protected boolean isColliding(ArrayList<Actor> other_actors ) {
		for ( Actor other_actor : other_actors )
			if( other_actor.hitbox().overlaps( hitbox ))
			{
				other_actor.notifyOfCollision();
				return true;
			}
		return false;
	}
	
	protected boolean isColliding( Point other_tile_pos ) {
		return other_tile_pos.equals(tile_pos);
	}
	
	
	public boolean isColliding( Player player ) {
		return player.hitbox().contains( hitbox );
	}
	
	/**
	 * Checks if the actor is outside of the screen or not.
	 * @param scroll
	 * @param tiles_per_cam_height
	 * @return True if out of bounds
	 */
	protected boolean isOutOfBounds( float left_screen_scroll,
			int tiles_per_cam_width, int tiles_per_cam_height ) {
		if( state == State.INACTIVE && tile_pos.x() < 
				left_screen_scroll + tiles_per_cam_width)
			state = State.MOVING;
		if( pos.x() < -1 || pos.x() > tiles_per_cam_height ||
			pos.x() < left_screen_scroll-1 ) {
			state = State.DEAD;
			out_of_bounds = true;
			return true;
		}
		return false;
	}
	
	public Point pos() {
		return pos;
	}
	
	public Point tilePos() {
		return tile_pos;
	}
	
	public Rectangle hitbox() {
		return hitbox;
	}

	protected void update( float dt ) {
		walk_timer += dt;
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
		return state == State.DEAD || out_of_bounds;
	}

	public Vect movementVector() {
		return movement_vector;
	}

//	public void setMovementVector(Vect movement_vector) {
//		this.movement_vector = movement_vector;
//	}
	
	/**
	 * 
	 * @return True if the path is empty.
	 */
	protected boolean followPath( float dt ) {
		if(movement_vector.arrived() && !path.isEmpty() )
			movement_vector = new Vect( pos, path.remove(), speed );
		else if( !movement_vector.arrived() )
			move( dt );
		return path.isEmpty() && movement_vector.arrived();
	}
		
	protected void updatePathFromTarget(float dt, Level level, Actor target) {
		if( path.isEmpty() || path_target != target.tilePos() )
			findPath(pos, level);
		else
			followPath( dt );
	}
	
	public void addToPath( GridNode node ) {
		path.add( node );
	}
	
//	public void setPath( Deque<GridNode> path ) {
//		this.path = path;
//	}
	
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
}
