package com.christianbaum.games.katsdream;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Cart extends Enemy {

	boolean is_angry;
	boolean has_changed_sprite;
	final int texture_num = 6;
	
	Cart(float x, float y, TextureRegion[][] texture, boolean hard_flag ) {
		super(x, y);
		is_angry = hard_flag;
		updateFrames( texture );
		health = is_angry ? 20: 5;
		float speed = 1.5f;
		if( is_angry )
			speed = 3f;
		movement_vector = new Vect(new Point(x,y), new Point( -1,y ), speed );
		state = State.INACTIVE;
	}
	
	@Override
	public void notifyOfCollision() {
		if(!is_angry) {
			has_changed_sprite = true;
			movement_vector = new Vect(new Point(pos.x,pos.y), new Point( -1,pos.y ), 3f );
			is_angry = true;
		}
	}
	
	@Override
	public void draw(Batch batch, KatsDream world) {
		if( has_changed_sprite ) {
			updateFrames( world.texture_region[6] );
			has_changed_sprite = false;
		}
		super.draw(batch, world );
	}
	
	private void updateFrames( TextureRegion[][] texture_region ) {
		int frame_row = 1;
		if( is_angry )
			frame_row = 0;
		TextureRegion walk_frames[] = new TextureRegion[2];
		walk_frames[0] = texture_region[frame_row][1];
		walk_frames[1] = texture_region[frame_row][2];
		anim = new Animation( 0.1f, walk_frames);
	}
	
	@Override
	public void update( float dt , KatsDream world, ArrayList<Actor> actors_to_add ) {
		if( state == State.MOVING ) {
			anim_timer += dt * (is_angry ? 1: 0.5);
			move( dt );
		}
		updateEnemy( world );
	}
	
	public int getTextureRegion() {
		return 6;
	}
}
