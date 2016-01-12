package com.christianbaum.games.katsdream;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

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
	public void update( float dt ) {
		if( state == State.MOVING )
			super.update( dt );
		else if (state == State.DEAD)
			anim_timer+=dt/2;
	}
	
	@Override
	public Bullet updateAI(float dt, Player player, Level level, 
			TextureRegion[][] region ) {
		if( state == State.MOVING) {
			if( enemy_num == 4 || enemy_num == 5)
				updatePathFromTarget(dt, level, randomDest());
			else
				updatePathFromTarget(dt, level, player.tilePos());
			if( enemy_num % 2 == 1  && canShoot( false ) )
				return new Bullet( pos, player.tilePos(), 2, false, region);
		}
		return null;
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
	public void draw(Batch batch, TextureRegion[][] texture_region, 
			float left_screen_scroll, int cam_width, int cam_height,
			int tiles_per_cam_width, int tiles_per_cam_height ) {
		if( updateDirection() || state == State.DEAD && !dead_sprite_set)
			updateWalkFrames( texture_region );
		super.draw(batch, texture_region, left_screen_scroll, 
				cam_width, cam_height,tiles_per_cam_width,tiles_per_cam_height);
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
