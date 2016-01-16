package com.christianbaum.games.katsdream;
import java.util.ArrayList;
import java.util.Iterator;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.christianbaum.games.katsdream.KatsDream.GameState;

public final class Player extends Actor {
	
	Player(int x, int y, TextureRegion[][] texture, KatsDream world) {
		super(x, y);
		updateWalkFrames(texture);
		health = 3;
	}
	
	@Override
	public boolean okayToDelete() {
		return state == State.DEAD && anim_timer > 0.30f ? true : false;
	}
	
	@Override
	public void draw(Batch batch, KatsDream w) {
		if( updateDirection() || state == State.DEAD && !dead_sprite_set)
			updateWalkFrames( w.texture_region[1] );
		super.draw(batch, w);
		Iterator<GridNode> path_iter = path.iterator();
		while( path_iter.hasNext() ) {
			GridNode node = path_iter.next();
			batch.draw( node.getTexture( w.texture_region[7]),
					node.x*w.cam_width/w.tiles_per_cam_width -
					w.l.getScroll() * w.cam_width/w.tiles_per_cam_width,
					w.cam_height - (node.y + 1) * w.cam_height/w.tiles_per_cam_height,
					w.cam_width/w.tiles_per_cam_width, w.cam_height/w.tiles_per_cam_height);
		}
	}
	
	@Override
	public void update( float dt, KatsDream world,
			ArrayList<Actor> actors_to_add ) {
		if( world.mouse_down ) {
			if(	world.state == GameState.PLAYING &&
				world.click_tile_pos.equals( tilePos() ) ){
				clearPath();
				world.setState( GameState.DRAWING );
				world.advance();				
			}
			else if( world.state == GameState.PLAYING ) {
				if( canShoot( true ) ) {
					world.sfx[0].play();
					actors_to_add.add( new Bullet ( pos(), world.click_pos, 5.5f, 
						true, world.texture_region[0]  ) );
				}
			}
			else if ( world.state == GameState.DRAWING ) {
				if( GridNode.canTraverse( world.click_tile_pos, lastPathPos(), world.l) ) {
					Dir tmp = Dir.NO;
					if(!path.isEmpty() ) {
						path.getLast().setDir(lastPathPos().pointToDir(world.click_tile_pos));
						tmp = path.getLast().dir.opposite();
					}
					path.add( new GridNode( world.click_tile_pos ));
					path.getLast().setLastDir(tmp);
				}
			}
		}
		super.update( dt, world, actors_to_add );
		if( !followPath( dt*2 ) && state != State.DEAD )
			state = State.MOVING;
		else if( state == State.MOVING )
			state = State.STANDING;

		if( pos.x() < world.l.getScroll() -4 ) {
			state = State.DEAD;
			anim_timer = 0;
		}
		if( pos.x() < world.l.getScroll() -4 ) {
			state = State.DEAD;
			anim_timer = 0;
		}
	}
	
	@Override
	public boolean canShoot( boolean bonus_level ) {
		if( bulletTimer() > 0.75f || bonus_level && bulletTimer() >= 0.1f) {
			resetBulletTimer();
			return true;
		}
		return false;
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
		return 1;
	}
}
