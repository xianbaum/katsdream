package com.christianbaum.games.katsdream;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.Batch;

public class Slug extends Enemy {
	
	private final int enemy_num;
	private float bullet_timer = 0;
	/** The path finding points */
	private Deque<Point> path;
	
	Slug(float x, float y,  int num ) {
		super(x, y);
		enemy_num = num;
		if( num == 3 || num == 5 )
			speed = 3;
		state = State.INACTIVE;
		path = new LinkedList<Point>();
	}
	
	@Override
	public void update( float dt, KatsDream world,
			ArrayList<Actor> actors_to_add) {
		if( state == State.MOVING ) {
			anim_timer += dt;
			bullet_timer += dt;
		}
		else if (state == State.DEAD)
			anim_timer+=dt/2;
		Actor p = world.actors.get(0);
		if( state == State.MOVING) {
			if( enemy_num == 4 || enemy_num == 5)
				updatePathFromTarget(dt, world.l, randomDest(), world.actors.size());
			else
				updatePathFromTarget(dt, world.l, p.tilePos(), world.actors.size());
			if( enemy_num % 2 == 1  && canShoot( false ) )
				actors_to_add.add( new Bullet( pos, p.pos,
						2, false, world.texture_region[0]));
		}
		updateEnemy( world );
	}
	
	public boolean canShoot( boolean bonus_level ) {
		if( bullet_timer > 0.75f ) {
			bullet_timer = 0;
			return true;
		}
		return false;
	}
	
	@Override
	public void draw(Batch batch, KatsDream world ) {
		if( updateDirection() || state == State.DEAD && !dead_sprite_set)
			updateWalkFrames( world.texture_region[enemy_num] );
		super.draw(batch, world);
	}
	
	@Override
	public int getTextureRegion() {
		return enemy_num;
	}
	

	@Override
	public void notifyOfCollision() {
		health = health -1;
		if(health <= 0 && state != State.DEAD) {
			state = State.DEAD;
			anim_timer = 0;
		}
	}
	
	@Override
	public boolean okayToDelete() {
		return state == State.DEAD && anim_timer > 0.30f ? true : false;
	}
	
	public Point randomDest() {
		return tilePos().dirToPoint(Dir.fromIndex(Math.round((int)(Math.random()*3))));
	}
	/** Follows the path and returns true if the path is empty
	 * 
	 * @return True if the path is empty.
	 */
	protected boolean followPath( float dt ) {
		if(movement_vector.arrived() && !path.isEmpty() ) {
			Point x = path.remove();
			movement_vector = new Vect( pos, x, speed ); 
			}
		else if( !movement_vector.arrived() ) 
			move( dt );
		return path.isEmpty() && movement_vector.arrived();
	}
	
	@SuppressWarnings("unchecked")
	protected void updatePathFromTarget(float dt, Level level, Point target, int length) {
		if( path.isEmpty() || !path_target.equals( target )) {
			path_target = target;
			int max_attempts;
			if( length < 20 )
				max_attempts = 100;
			else if( length < 40 )
				max_attempts = 80;
			else
				max_attempts = 50;
			path = (Deque<Point>) Path.findPath(tilePos(), path_target, level , max_attempts);
		}
		else {
			followPath( dt );
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override 
	public void updateXFromMapChange(int left_map_width) {
		super.updateXFromMapChange(left_map_width);
		path = (Deque<Point>)Path.updateXFromMapChange(path, left_map_width);
	}
}
