package com.christianbaum.games.katsdream;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Plane extends Enemy {

	float velocity;
	int sprite_state;
	
	Plane (float x, float y)
	{
		super (x, y);
		state = State.INACTIVE;
		dead_sprite_set = false;
	}
	
	@Override
	public Bullet updateAI(float dt, Player player, Level l, TextureRegion[][] region) {
		if( state == State.MOVING ) {
			if( player.pos.y() > pos.y() )
				velocity += 0.2*dt;
			else
				velocity -= 0.2*dt;
		}
		return null;
	}
	
	@Override
	public void update( float dt ) {
		if( state == State.MOVING) {
			pos.setX( pos.x() - 7*dt );
			pos.setY( pos.y() + velocity );
		}
		else
			anim_timer+=dt/2;
	}

	
	@Override
	public void draw(Batch batch, TextureRegion[][] texture_region, 
			float left_screen_scroll, int cam_width, int cam_height,
			int tiles_per_cam_width, int tiles_per_cam_height ) {
		if( state == State.MOVING  ) {
			if ( velocity < 0 && sprite_state != 1 ) {
				sprite_state = 1;
			}
			else if ( velocity > 0 && sprite_state != 2 ) {
				sprite_state = 2;
			}
			batch.draw( texture_region[3][sprite_state], 
				pos.x()*cam_width/tiles_per_cam_width -
				left_screen_scroll * cam_width/tiles_per_cam_width,
				cam_height - (pos.y() + 1) * cam_height/tiles_per_cam_height,
				cam_width/tiles_per_cam_width, cam_height/tiles_per_cam_height);
		}
		else if (state == State.DEAD) {
			if( !dead_sprite_set ) {
				TextureRegion[] walk_frames = new TextureRegion[4];
				for(int i=0; i < 3; i++) {
					walk_frames[i] = texture_region[i][0];
				}
				anim = new Animation( 0.1f, walk_frames);
				dead_sprite_set = true;
			}
			super.draw(batch, texture_region,
					left_screen_scroll, cam_width, 
					cam_height, tiles_per_cam_width,
					tiles_per_cam_height);
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