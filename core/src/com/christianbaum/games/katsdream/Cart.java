package com.christianbaum.games.katsdream;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Cart extends Enemy {

	boolean is_angry;
	boolean has_changed_sprite;
	
	Cart(float x, float y, TextureRegion[][] texture, boolean hard_flag ) {
		super(x, y);
		is_angry = hard_flag;
		updateFrames( texture );
		health = is_angry ? 20: 5;
		float speed = 1.5f;
		if( is_angry )
			speed = 3f;
		setMovementVector(new Vect(new Point(x,y), new Point( -1,y ), speed ));
		setState( State.INACTIVE );
	}
	
	@Override
	public void notifyOfCollision() {
		if(!is_angry) {
			has_changed_sprite = true;
			setMovementVector(new Vect(new Point(pos().x(),pos().y()), new Point( -1,pos().y() ), 3f ));
			is_angry = true;
		}
	}
	
	@Override
	public void draw(Batch batch, TextureRegion[][] texture_region, 
			float left_screen_scroll, int cam_width, int cam_height,
			int tiles_per_cam_width, int tiles_per_cam_height ) {
		if( has_changed_sprite ) {
			updateFrames( texture_region );
			has_changed_sprite = false;
		}
		super.draw(batch, texture_region, left_screen_scroll, cam_width,
				cam_height, tiles_per_cam_width, tiles_per_cam_height);
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
	
	public void update( float dt ) {
		if( state() == State.MOVING ) {
			anim_timer += dt * (is_angry ? 1: 0.5);
			super.move( dt );
		}
	}
	
	public int getTextureRegion() {
		return 6;
	}

	@Override
	public Bullet updateAI(float dt, Player player, Level l,
			TextureRegion[][] region) {
		// TODO Auto-generated method stub
		return null;
	}
}
