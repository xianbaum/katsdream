package com.christianbaum.games.katsdream;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.christianbaum.games.katsdream.KatsDream.GameState;

public class Plane extends Enemy {

	float velocity;
	int sprite_state;
	
	Plane (float x, float y)
	{
		super (x, y);
		state = State.INACTIVE;
		dead_sprite_set = false;
		sprite_state = 1;
	}
	
	@Override
	public void update( float dt, KatsDream w,
			ArrayList<Actor> actors_to_add ) {
		if( state == State.MOVING && w.state != GameState.DRAWING) {
			pos.setX( pos.x() - 7*dt );
			pos.setY( pos.y() + velocity );
		}
		else
			anim_timer+=dt/2;
		Actor p = w.actors.get(0);
		if( state == State.MOVING ) {
			if( p.pos.y() > pos.y() )
				velocity += 0.2*dt;
			else
				velocity -= 0.2*dt;
		}
		boolean play_sound = false;
		if( state == State.INACTIVE)
			play_sound = true;
		updateEnemy( w );
		if( state == State.MOVING && play_sound)
			w.sfx[4].play();
	}

	
	@Override
	public void draw(Batch batch, KatsDream world) {
		if( state == State.MOVING  ) {
			if ( velocity < 0 && sprite_state != 1 ) {
				TextureRegion[] frame = new TextureRegion[1];
				frame[0] = world.texture_region[6][3][1];
				anim = new Animation(1, frame);
				sprite_state = 1;
			}
			else if ( velocity >= 0 && sprite_state != 2 ) {
				TextureRegion[] frame = new TextureRegion[1];
				frame[0] = world.texture_region[6][3][2];
				anim = new Animation(1, frame);
				sprite_state = 2;
			}
			super.draw(batch, world);
		}
		else if (state == State.DEAD) {
			if( !dead_sprite_set ) {
				TextureRegion[] walk_frames = new TextureRegion[4];
				for(int i=0; i < 3; i++) {
					walk_frames[i] = world.texture_region[6][i][0];
				}
				anim = new Animation( 0.1f, walk_frames);
				dead_sprite_set = true;
			}
			super.draw(batch, world);
		}
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
	public int getTextureRegion() {
		return 6;
	}
	
	@Override
	public boolean okayToDelete() {
		return state == State.DEAD && anim_timer > 0.30f ? true : false;
	}
}