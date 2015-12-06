package com.christianbaum.games.katsdream;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** The main LibGDX method for the game.
 * 
 * @author Christian
 *
 */
public class KatsDream extends ApplicationAdapter {
	private SpriteBatch batch;
	private Level l;
	private int cam_width;
	private int cam_height;
	private int tiles_per_cam_width;
	private int tiles_per_cam_height;
	private Player p;
	private Stage stage;
	private boolean arrows[];
	private boolean mouse_down;
	private Point click_pos;
	private Point click_tile_pos;
	private enum GameState { MENU, PLAYING, GAMEOVER, DRAWING };
	private GameState state;
	private FitViewport viewport;
	private OrthographicCamera sprite_camera;
	private ArrayList<Bullet> bullets;;
	private ArrayList<Actor> enemies;
	private boolean traversed[][];
	private TextureRegion[] traversed_frames;
	private Animation traversed_anim;
	private float traversed_timer;
	/** The entire texture region of all of the actors */
	protected TextureRegion[][][] texture_region;
	
	@Override
	public void create () {
		
		//Initializing input
		arrows = new boolean[4];
		mouse_down = false;
		click_pos = new Point( 0, 0 );
		click_tile_pos = new Point( 0, 0 );

		//Initializing screen size and aspect ratio stuff
		tiles_per_cam_width = 20;
		tiles_per_cam_height = 15;
		cam_width=Gdx.graphics.getWidth();
		cam_height=Gdx.graphics.getHeight();
		sprite_camera = new OrthographicCamera();
		viewport = new FitViewport(tiles_per_cam_width, tiles_per_cam_height,
				sprite_camera);
		stage = new Stage( viewport );
		//Initializing textures and sprites
		batch = new SpriteBatch();
		Texture[] images = new Texture[10];
		for(int i = 0; i < 10; i++ )
			images[i] =
			new Texture(Gdx.files.internal("assets/gfx/actors"+i+".png"));
		
		texture_region = new TextureRegion[10][][];
		for(int i = 0; i < 7; i++)
			texture_region[i] = TextureRegion.split(images[i], 16, 16);
		texture_region[7] = TextureRegion.split(images[7], 8, 8);
		texture_region[8] = TextureRegion.split(images[7], 64, 64);
		texture_region[9] = TextureRegion.split(images[9], 32, 32);
		
		//Initializing game variables
		l = new Level( tiles_per_cam_width, tiles_per_cam_height );
		p = new Player(1,1, texture_region[1]);
		bullets = new ArrayList<Bullet>();
		enemies = new ArrayList<Actor>();
		state = GameState.PLAYING;
		
		//Initializing drawing variables
		traversed = new boolean[ l.levelWidth() ][ l.levelHeight() ];
		TextureRegion player_frames[][] = TextureRegion.split(images[1],16, 16);
		traversed_frames = new TextureRegion[2];
		for(int i = 0; i < 2; i++ )
			traversed_frames[i] = player_frames[i+2][3];
		traversed_timer = 0;
		traversed_anim = new Animation( 0.4f, traversed_frames );
	}
	
	@Override
	public void render () {
		float dt = Gdx.graphics.getDeltaTime();
		//Updating
		if( dt > 1 )
			dt = 1;
		
		
		//Updating input
		arrows[0] = Gdx.input.isKeyJustPressed(Keys.LEFT);
		arrows[1] = Gdx.input.isKeyJustPressed(Keys.UP);
		arrows[2] = Gdx.input.isKeyJustPressed(Keys.DOWN);
		arrows[3] = Gdx.input.isKeyJustPressed(Keys.RIGHT);
		
		//Left and right mouse buttons, and touchscreen are gotten.
		mouse_down = Gdx.input.isButtonPressed(Buttons.LEFT) ||
				Gdx.input.isButtonPressed(Buttons.RIGHT) || 
				Gdx.input.isTouched();
		click_pos = new Point ( Gdx.input.getX() , Gdx.input.getY() );
		//yuck
		Vector2 projection = viewport.unproject(
				new Vector2 ( click_pos.x(), click_pos.y() ) );
		click_pos.setX( projection.x + l.getScroll() );
		click_pos.setY( tiles_per_cam_height - projection.y - 1 ) ;
		if( click_pos.x() < 0 )
			click_pos.setX( 0 );
		else if( click_pos.x() > l.levelWidth() )
			click_pos.setX( l.levelWidth() );
		if( click_pos.y() < 0 )
			click_pos.setY( 0 );
		else if( click_pos.y() > l.levelHeight() )
			click_pos.setY( l.levelHeight() );
		click_tile_pos.setX( (int) click_pos.x() );
		click_tile_pos.setY( (int) Math.ceil( click_pos.y() ) );

		
		//Checking input
		checkInput();
		
		//Updating game
		if( state == GameState.PLAYING ) {
			int temp_scroll = l.scroll( dt, 2, tiles_per_cam_width);
			if( temp_scroll != 0 ) {
				p.updateXFromMapChange( temp_scroll );
				for( Bullet bullet : bullets )
					bullet.updateXFromMapChange( temp_scroll );
				enemies.addAll( l.getnewEnemies(texture_region) );
			}
			p.update( dt );
			
			Iterator itor = bullets.iterator();
			
			while( itor.hasNext() ) {
				bullet.update( dt );
				bullet.isColliding( p, enemies, l.getScroll(), 
						tiles_per_cam_width,  tiles_per_cam_height );
			}
			
			for( Actor enemy : enemies ) {
				enemy.update(dt);
				enemy.isOutOfBounds( l.getScroll(), tiles_per_cam_width, 
						tiles_per_cam_height);
				if( enemy.isColliding( p ) )
					p.notifyOfCollision();
				
//				if( enemy.okayToDelete() )
//					enemies.remove( enemy );
			}
			
			try {
				if ( traversed[ (int) p.tilePos().x() ][ (int) p.tilePos().y()])
					traversed[ (int)p.tilePos().x() ]
							[ (int)p.tilePos().y() ]=false;
			}
			catch( Exception e ) {};
		}
		traversed_timer += dt;
		
		//Drawing game
		Gdx.gl.glClearColor(0, 0, 0, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if( state == GameState.PLAYING || state == GameState.DRAWING ) {
			l.draw();
			batch.begin();
			//Drawing walk path
			for( int x = 0; x < l.levelWidth(); x++ )
				for( int y = 0; y < l.levelHeight(); y++)
					if( traversed[x][y] )
						batch.draw( traversed_anim.
								getKeyFrame( traversed_timer, true),
						x*cam_height/tiles_per_cam_height -
						l.getScroll() * 32f,
						cam_height - (y + 1) 
						* cam_width/tiles_per_cam_width,
						cam_width/tiles_per_cam_width, 
						cam_height/tiles_per_cam_height);

			p.draw(batch, texture_region[1],l.getScroll(), cam_width,cam_height,
					tiles_per_cam_width, tiles_per_cam_height);
			
			for( Actor enemy : enemies ) {
				enemy.draw( batch, texture_region[1], l.getScroll(), cam_width, 
						cam_height, tiles_per_cam_width, tiles_per_cam_height);
			}
			
			for( Bullet bullet : bullets ) {
				bullet.draw2(batch, l.getScroll(), cam_width, cam_height, 
						tiles_per_cam_width, tiles_per_cam_height);
			}
			batch.end();
			l.total_maps();
		}
	}
	
	@Override
	public void resize(int width, int height) {
	    stage.getViewport().update(width, height, false);
	}

	private void checkInput() {
		//Checking input
		if(arrows[0])
			p.addToPath( new GridNode( p.lastPathPos().x() - 1, 
					p.lastPathPos().y() ));
		else if( arrows[1] )
			p.addToPath( new GridNode( p.lastPathPos().x(),
					p.lastPathPos().y() - 1 ));
		else if( arrows[2] )
			p.addToPath( new GridNode( p.lastPathPos().x(),
					p.lastPathPos().y() + 1 ));
		else if( arrows[3] )
			p.addToPath( new GridNode( p.lastPathPos().x() + 1, 
					p.lastPathPos().y()));
		if( mouse_down ) {
			if(	click_tile_pos.equals( p.tilePos() ) 
							&& state == GameState.PLAYING ){
				p.clearPath();
				state = GameState.DRAWING;
				traversed = new boolean[ l.levelWidth() ][ l.levelHeight() ];
				traversed_timer = 0;
			}
			else if( state == GameState.PLAYING ) {
				if( p.canShoot( true ) )
				bullets.add( new Bullet ( p.pos(), click_pos, 4, 
						true, texture_region[7]  ) );
			}
			else if ( state == GameState.DRAWING ) {
				if( GridNode.canTraverse( click_tile_pos, p.lastPathPos(), l,
						traversed) ) {
					p.addToPath( new GridNode( click_tile_pos ));
					traversed[ (int) click_tile_pos.x() ]
							[ (int) click_tile_pos.y() ] = true;
				}
			}
		}
		else {
			if( state == GameState.DRAWING )
				state = GameState.PLAYING;
		}
	}
}

