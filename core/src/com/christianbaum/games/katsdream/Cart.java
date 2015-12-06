package com.christianbaum.games.katsdream;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Cart extends Actor {

	boolean is_angry;
	boolean has_changed_sprite;
	boolean is_out_of_bounds;
	
	Cart(float x, float y, TextureRegion[][] texture, boolean hard_flag ) {
		super(x, y);
		is_angry = hard_flag;
		updateFrames( texture );
		float speed = 1.5f;
		if( is_angry )
			speed = 2.5f;
		setMovementVector(new Vect(new Point(x,y), new Point( x-1,y ), speed ));
		setState( State.INACTIVE );
	}
	
	public void notifyOFCollision() {
		if(!is_angry)
			has_changed_sprite = true;
		is_angry = true;
	}

	public void draw(SpriteBatch batch, TextureRegion[][] texture_region, 
			float left_screen_scroll, int cam_width, int cam_height,
			int tiles_per_cam_width, int tiles_per_cam_height ) {
		if( has_changed_sprite ) {
			updateFrames( texture_region );
			has_changed_sprite = false;
		}
	}
	
	private void updateFrames( TextureRegion[][] texture_region ) {
		int frame_row = 1;
		if( is_angry )
			frame_row = 0;
		TextureRegion walk_frames[] = new TextureRegion[2];
		walk_frames[0] = texture_region[1][frame_row];
		walk_frames[1] = texture_region[2][frame_row];
	}
	
	public void update( float dt ) {
		if( state() == State.MOVING ) {
			super.update(dt);
			super.move( dt );
		}
	}
}
