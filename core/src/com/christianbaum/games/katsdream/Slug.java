package com.christianbaum.games.katsdream;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Batch;

public class Slug extends Enemy {
	
	int enemy_num;
	
	Slug(float x, float y,  int num ) {
		super(x, y);
		enemy_num = num;
		if( num == 3 || num == 5 )
			speed = 3;
		setState( State.INACTIVE );
	}
	
	@Override
	public void update( float dt, KatsDream world,
			ArrayList<Actor> actors_to_add) {
		if( state == State.MOVING )
			super.update( dt, world, actors_to_add );
		else if (state == State.DEAD)
			anim_timer+=dt/2;
		Actor p = world.actors.get(0);
		if( state == State.MOVING) {
			if( enemy_num == 4 || enemy_num == 5)
				updatePathFromTarget(dt, world.l, randomDest());
			else
				updatePathFromTarget(dt, world.l, p.tilePos());
			if( enemy_num % 2 == 1  && canShoot( false ) )
				actors_to_add.add( new Bullet( pos, p.pos(),
						2, false, world.texture_region[0]));
		}
		updateEnemy( world );
	}
	
	@Override
	public boolean canShoot( boolean bonus_level ) {
		if( bulletTimer() > 0.75f ) {
			resetBulletTimer();
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
}
