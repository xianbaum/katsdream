package com.christianbaum.games.katsdream;

import java.util.ArrayList;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Bomb extends Enemy {
	
	float beep_timer = 0;
	boolean beep_faster = false;
	Bomb(float x, float y) {
		super(x, y);
		health = 15;
		state = State.INACTIVE;
	}

	@Override
	public void notifyOfCollision() {
		health = health -1;
		if( health <= 0 )
			state = State.DEAD;
		anim_timer = 0;
		dead_sprite_set = false;
	}
	
	@Override
	public void update( float dt, KatsDream world, ArrayList<Actor> actors_to_add) {
		if( state != State.INACTIVE ) {
			anim_timer+=dt;
			beep_timer += dt;
		}
		updateEnemy( world );
		
		if( !beep_faster && anim_timer > 3.2f ) {
			beep_faster = true;
			anim_timer = 0;
		}
		else if( beep_faster && anim_timer > 3.2f && state == State.MOVING ) {
			state = State.EXPLODING;
			anim_timer = 0;
		}
		
		if( !beep_faster && beep_timer > 1 ||
				beep_faster && beep_timer > 0.15f && state == State.MOVING ) {
			world.sfx[6].play();
			beep_timer = 0;
		}
	}
	
	@Override
	public void draw( Batch batch, KatsDream world) {
		if( state == State.MOVING && !beep_faster ) {
			if ( !dead_sprite_set ) {
				TextureRegion[] frame = new TextureRegion[1];
				frame[0] = world.texture_region[6][2][1];
				anim = new Animation(1, frame);
				dead_sprite_set = true;
			}
		}
		else if( state == State.MOVING && beep_faster ) {
			if ( dead_sprite_set ) {
				TextureRegion[] frame = new TextureRegion[2];
				frame[0] = world.texture_region[6][2][1];
				frame[1] = world.texture_region[6][2][2];
				anim = new Animation(0.1f, frame);
				dead_sprite_set = false;
			}
		}
		else if( state == State.DEAD ) {
			if( !dead_sprite_set ) {
				TextureRegion[] walk_frames = new TextureRegion[4];
				for(int i=0; i < 4; i++) {
					walk_frames[i] = world.texture_region[6][i][3];
				}
				anim = new Animation( 0.3f, walk_frames);
				dead_sprite_set = true;
			}
		}
		if( state == State.DEAD || state == State.MOVING || state == State.STANDING)
			super.draw(batch, world);
	}
	
	@Override
	public boolean okayToDelete() {
		return state == State.DEAD && anim_timer > 1f;
	}
	
	@Override
	public int getTextureRegion() {
		return 6;
	}

}
