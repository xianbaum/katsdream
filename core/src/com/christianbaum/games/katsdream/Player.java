package com.christianbaum.games.katsdream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.christianbaum.games.katsdream.DrawnPath.DrawnPoint;
import com.christianbaum.games.katsdream.KatsDream.GameState;

public final class Player extends Actor {
		
	private float bullet_timer;
	final private Rectangle click_box;
	private Deque<DrawnPath.DrawnPoint> path;
	Player(int x, int y, TextureRegion[][] texture, KatsDream world) {
		super(x, y);
		updateWalkFrames(texture);
		health = 3;
		bullet_timer = 10;
		path = new LinkedList<DrawnPoint>();
		click_box = new Rectangle( -0.25f, -1.25f, 1.5f, 1.5f);
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
		DrawnPath.draw( batch, path, w );
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void update( float dt, KatsDream world,
			ArrayList<Actor> actors_to_add ) {
		bullet_timer += dt;
		anim_timer += dt;
		if( world.mouse_down ) {
			if(	world.state == GameState.PLAYING &&
				clickTileBox().contains( world.click_pos.x, world.click_pos.y) ){
				path.clear();
				world.setState( GameState.DRAWING );
				world.advance();				
			}
			else if( world.state == GameState.PLAYING ) {
				if( canShoot( true ) ) {
					world.sfx[0].play();
					actors_to_add.add( new Bullet ( new Point( pos.x+0.25f, pos.y ), world.click_pos, 5.5f, 
						true, world.texture_region[0]  ) );
				}
			}
			else if ( world.state == GameState.DRAWING ) {				
				if( !lastPathPos().equals( world.click_tile_pos ) && 
						Math.abs( lastPathPos().x - world.click_tile_pos.x )
						+ Math.abs(lastPathPos().y - world.click_tile_pos.y) < 5 )
					if( path.isEmpty()){
						path = (Deque<DrawnPoint>) DrawnPath.findDrawnPath( tilePos(), world.click_tile_pos, world.l, 10 );
					}
					else {
						Deque<DrawnPath.DrawnPoint> tmp = (Deque<DrawnPoint>) DrawnPath.findDrawnPath( lastPathPos(), world.click_tile_pos, world.l, 10);
						DrawnPath.append( path, tmp);
					}
			}
		}
		if( world.state == GameState.PLAYING ) {
			if( !followPath( dt*2 ) && state != State.DEAD )
				state = State.MOVING;
			else if( state == State.MOVING )
				state = State.STANDING;
		}
		if( pos.x < world.l.getScroll() -4 ) {
			state = State.DEAD;
			anim_timer = 0;
		}
	}
	
	public boolean canShoot( boolean bonus_level ) {
		if( bullet_timer > 0.75f || bonus_level && bullet_timer >= 0.1f) {
			bullet_timer = 0;
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
	
	public boolean isDead() {
		return state == State.DEAD;
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
	@Override 
	public void updateXFromMapChange(int left_map_width) {
		super.updateXFromMapChange(left_map_width);
		path = (Deque<DrawnPath.DrawnPoint>)Path.updateXFromMapChange(path, left_map_width);
	}
	
	/** The end path position, or the start point
	 * 
	 * @return the end path position, or the start point
	 */
	/**/
	private Point lastPathPos() {
		return !path.isEmpty() ? path.getLast() : movement_vector.dest();
	}
	
	private Rectangle clickTileBox() {
		return new Rectangle( pos.x+click_box.x,
				pos.y+click_box.y,
				click_box.width,
				click_box.height);
	}
}
