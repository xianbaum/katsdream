package com.christianbaum.games.katsdream;

import java.util.ArrayList;
import java.util.Iterator;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.christianbaum.games.katsdream.Actor.State;

/** The main LibGDX method for the game.
 * @author Christian
 */
public class KatsDream extends ApplicationAdapter {
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
	private enum GameState { MENU, HOWTO, PLAYING, GAMEOVER, DRAWING };
	private GameState state;
	private FitViewport viewport;
	private OrthographicCamera sprite_camera;
	private ArrayList<Bullet> bullets;
	private ArrayList<Enemy> enemies;
	private boolean traversed[][];
	private TextureRegion[] traversed_frames;
	private Animation traversed_anim;
	private float traversed_timer;
	private float game_timer;
	private float score;
	private BitmapFont font;
	private boolean has_advanced;
	private boolean has_been_down;
	private boolean music_initialized;
	/** The entire texture region of all of the actors */
	private Texture title_image;
	private TextureRegion[][][] texture_region;
	private Music music;
	private int level_num;
	private Sound[] sfx;

	@Override
	public void create () {
		
		//Initializing input
		arrows		 	= new boolean[4];
		mouse_down	 	= false;
		has_been_down   = false;
		click_pos	 	= new Point( 0, 0 );
		click_tile_pos	= new Point( 0, 0 );

		//Initializing screen size and aspect ratio stuff
		tiles_per_cam_width	= 20;
		tiles_per_cam_height= 15;
		cam_width           = Gdx.graphics.getWidth();
		cam_height			= Gdx.graphics.getHeight();
		if( cam_height > cam_width ){
			int temp   = cam_height;
			cam_height = cam_width;
			cam_width  = temp;
		}
		sprite_camera = new OrthographicCamera();
		viewport = new FitViewport( tiles_per_cam_width,
				tiles_per_cam_height, sprite_camera);
		stage = new Stage( viewport );
		//Initializing textures and sprites
		Texture[] images = new Texture[10];
		for(int i = 0; i < 10; i++ )
			images[i] =
			new Texture(Gdx.files.internal("gfx/actors"+i+".png"));
		texture_region = new TextureRegion[10][][];
		for(int i = 0; i < 7; i++)
			texture_region[i] = TextureRegion.split(images[i], 16, 16);
		texture_region[7] = TextureRegion.split(images[7], 8, 8);
		texture_region[8] = TextureRegion.split(images[7], 64, 64);
		texture_region[9] = TextureRegion.split(images[9], 32, 32);
		TextureRegion player_frames[][] = TextureRegion.split(images[1],16, 16);
		traversed_frames = new TextureRegion[2];
		for(int i = 0; i < 2; i++ )
			traversed_frames[i] = player_frames[i+2][3];
		title_image = new Texture( Gdx.files.internal("gfx/title.png"));

		//Initializing font
		font = new BitmapFont(
				Gdx.files.internal("gfx/font.fnt"),false);
		font.getData().setScale(cam_width/320);
		
		//Initializing sounds
		sfx = new Sound[6];
		sfx[0] = Gdx.audio.newSound( Gdx.files.internal("mfx/shootan.wav"));
		sfx[1] = Gdx.audio.newSound( Gdx.files.internal("mfx/kill.ogg"));
		sfx[2] = Gdx.audio.newSound( Gdx.files.internal("mfx/kill2.ogg"));
		sfx[3] = Gdx.audio.newSound( Gdx.files.internal("mfx/die.ogg"));
		sfx[4] = Gdx.audio.newSound( Gdx.files.internal("mfx/launch.ogg"));
		sfx[5] = Gdx.audio.newSound( Gdx.files.internal("mfx/dingan.wav"));
		//Initializing game
		state = GameState.MENU;
		level_num = 1;
	}
	
	@Override
	public void render () {
		float dt = Gdx.graphics.getDeltaTime();
		//Updating
		if( dt > 1 )
			dt = 1;
		game_timer += dt;
		
		//Updating input
		arrows[0] = Gdx.input.isKeyJustPressed(Keys.LEFT);
		arrows[1] = Gdx.input.isKeyJustPressed(Keys.UP);
		arrows[2] = Gdx.input.isKeyJustPressed(Keys.DOWN);
		arrows[3] = Gdx.input.isKeyJustPressed(Keys.RIGHT);
		
		//Left and right mouse buttons, and touch screen are gotten.
		mouse_down = Gdx.input.isButtonPressed(Buttons.LEFT) ||
				Gdx.input.isButtonPressed(Buttons.RIGHT) || 
				Gdx.input.isTouched();
		click_pos = new Point ( Gdx.input.getX() , Gdx.input.getY() );
		//yuck
		Vector2 projection = viewport.unproject(
				new Vector2 ( click_pos.x(), click_pos.y() ) );
		float scroll = 0, width = 15, height = 20;
		if( state == GameState.PLAYING ||
			state == GameState.DRAWING ||
			state == GameState.GAMEOVER) {
			scroll = l.getScroll();
			width = l.levelWidth();
			height = l.levelHeight();
		}
		click_pos.setX( projection.x + scroll );
		click_pos.setY( tiles_per_cam_height - projection.y - 1 ) ;
		if( click_pos.x() < 0 )
			click_pos.setX( 0 );
		else if( click_pos.x() > width )
			click_pos.setX( width );
		if( click_pos.y() < 0 )
			click_pos.setY( 0 );
		else if( click_pos.y() > height)
			click_pos.setY( height);
		click_tile_pos.setX( (int) click_pos.x() );
		click_tile_pos.setY( (int) Math.ceil( click_pos.y() ) );
		
		//Checking input
		checkInput();
		
		//Updating game
		if( state == GameState.PLAYING || state == GameState.GAMEOVER) {
			int temp_scroll = 0;
			if( state == GameState.PLAYING && has_advanced) {
				temp_scroll = l.scroll( dt, 1.9f, tiles_per_cam_width);
				score += dt;
			}
			if( temp_scroll != 0 ) {
				p.updateXFromMapChange( temp_scroll );
				for( Bullet bullet : bullets )
					bullet.updateXFromMapChange( temp_scroll );
				for( Actor enemy : enemies )
					enemy.updateXFromMapChange( temp_scroll );
				enemies.addAll( l.getnewEnemies(texture_region,
						tiles_per_cam_height));
				traversed = new boolean[ l.levelWidth() ][ l.levelHeight() ];
			}
			if(!p.okayToDelete()) {
				p.update( dt );
				p.isOutOfBounds(l.getScroll() );
			}
			Iterator<Bullet> bull_iter = bullets.iterator();
			while( bull_iter.hasNext() ) {
				Bullet bullet = bull_iter.next();
				bullet.update( dt );
				bullet.isColliding( p, enemies, l.getScroll(), 
						tiles_per_cam_width,  tiles_per_cam_height, sfx );
				if(bullet.okayToDelete())
					bull_iter.remove();
			}
			Iterator<Enemy> enem_iter = enemies.iterator();	
			while( enem_iter.hasNext() ) {
				Enemy enemy = enem_iter.next();
				enemy.update(dt);
				enemy.isOutOfBounds( l.getScroll()+tiles_per_cam_width,
						tiles_per_cam_width, tiles_per_cam_height, sfx);
				if( enemy.isColliding( p ) )
					p.notifyOfCollision();
				Bullet b = enemy.updateAI( dt, p, l, texture_region[7] );
				if( b != null )
					bullets.add( b );
				if( enemy.okayToDelete() )
					enem_iter.remove();
			}
			try {
				if ( traversed[ (int) p.tilePos().x() ][ (int) p.tilePos().y()])
					traversed[ (int)p.tilePos().x() ]
							[ (int)p.tilePos().y() ]=false;
			}
			catch( Exception e ) {};
			if( p.state() == State.DEAD && state == GameState.PLAYING) {
				state = GameState.GAMEOVER;
				sfx[3].play();
				game_timer = 0;
				disposeMusic();
			}
			traversed_timer += dt;
		}
		
		//Updating music
		updateMusic();
		
		//Drawing game
		if( state == GameState.MENU)
			Gdx.gl.glClearColor(39f/255f, 31f/255f, 195f/255f, 1f);
		else
			Gdx.gl.glClearColor(0, 0, 0, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if( state == GameState.PLAYING || state == GameState.DRAWING 
				|| state == GameState.GAMEOVER && game_timer < 2.5 ) {
			if( has_advanced)
				l.draw();
			Batch batch = stage.getBatch();
			batch.begin();
			//Drawing walk path
			for( int x = 0; x < l.levelWidth(); x++ )
				for( int y = 0; y < l.levelHeight(); y++)
					if( traversed[x][y] )
						batch.draw( traversed_anim.
								getKeyFrame( traversed_timer, true), 
								x*cam_width/tiles_per_cam_width -
								l.getScroll() * cam_width/tiles_per_cam_width,
								cam_height - (y + 1)
								* cam_height/tiles_per_cam_height,
								cam_width/tiles_per_cam_width,
								cam_height/tiles_per_cam_height);
			if(!p.okayToDelete())
				p.draw(batch, texture_region[1],l.getScroll(), cam_width,cam_height,
					tiles_per_cam_width, tiles_per_cam_height);
			
			for( Actor enemy : enemies ) {
				enemy.draw( batch, texture_region[enemy.getTextureRegion()], l.getScroll(), cam_width, 
						cam_height, tiles_per_cam_width, tiles_per_cam_height);
			}
			
			for( Bullet bullet : bullets ) {
				bullet.draw(batch, l.getScroll(), cam_width, cam_height, 
						tiles_per_cam_width, tiles_per_cam_height);
			}
			//Drawing text
			if( !has_advanced )
				font.draw( batch, "First touch the player.\nThen draw a path to the right! ->",
						cam_width /tiles_per_cam_width *3, 
						cam_height/tiles_per_cam_height*7);
			else if( state == GameState.PLAYING || 
					state == GameState.DRAWING )
				font.draw( batch, String.format("%07d",(int)(score*5)*10), cam_width/tiles_per_cam_width*0.5f,
						cam_height/tiles_per_cam_height*13.5f);
			batch.end();
		}
		// title screen
		else if( state == GameState.MENU ) {
			Batch batch = stage.getBatch();
			batch.begin();
			batch.draw( title_image, 1.5F*cam_width/tiles_per_cam_width,
					5*cam_height/tiles_per_cam_height,
					16*cam_width/tiles_per_cam_width,
					8*cam_height/tiles_per_cam_height);
			font.draw( batch, "Touch to begin!\nv0.10", cam_width/tiles_per_cam_width*6f,
					cam_height/tiles_per_cam_height*3);
			batch.end();
		}
		else if( state == GameState.GAMEOVER && game_timer > 2.5f) {
			Batch batch = stage.getBatch();
			batch.begin();
			font.draw( batch, "GAME OVER", cam_width/tiles_per_cam_width*7.5f,
					cam_height/tiles_per_cam_height*7);
			if( state == GameState.GAMEOVER && game_timer > 5f) {
				font.draw( batch, "FINAL SCORE:" + ((int)score*50), cam_width/tiles_per_cam_width*6.5f,
						cam_height/tiles_per_cam_height*6);
			}
			batch.end();
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
			if(	state == GameState.PLAYING &&
				click_tile_pos.equals( p.tilePos() ) ){
				p.clearPath();
				state = GameState.DRAWING;
				has_advanced = true;
				traversed = new boolean[ l.levelWidth() ][ l.levelHeight() ];
				traversed_timer = 0;
			}
			else if( state == GameState.PLAYING ) {
				if( p.canShoot( true ) ) {
					sfx[0].play();
					bullets.add( new Bullet ( p.pos(), click_pos, 5.5f, 
						true, texture_region[7]  ) );
				}
			}
			else if ( state == GameState.DRAWING ) {
				if( GridNode.canTraverse( click_tile_pos, p.lastPathPos(), l,
						traversed) ) {
					p.addToPath( new GridNode( click_tile_pos ));
					traversed[ (int) click_tile_pos.x() ]
							[ (int) click_tile_pos.y() ] = true;
				}
			}
			else if ( state == GameState.GAMEOVER && !has_been_down) {
				if( game_timer < 2.5f)
					game_timer = 2.5f;
				else if(game_timer < 5)
					game_timer = 5;
				else {
					state = GameState.MENU;
					disposeMusic();
				}
			}
			else if ( state == GameState.MENU && !has_been_down )
			{
				startNewGame();
			}
			else if ( state == GameState.HOWTO ) {
				state = GameState.MENU;
			}
			has_been_down = true;
		}
		else {
			if( state == GameState.DRAWING )
				state = GameState.PLAYING;
			if( has_been_down )
				has_been_down = false;
		}
	}
	
	private void startNewGame() {
		//Initializing game variables
		l = new Level( tiles_per_cam_width, tiles_per_cam_height );
		p = new Player(6,5, texture_region[1]);
		bullets = new ArrayList<Bullet>();
		enemies = new ArrayList<Enemy>();
		has_advanced = false;
		//Initializing drawing variables
		traversed = new boolean[ l.levelWidth() ][ l.levelHeight() ];
		traversed_timer = 0;
		traversed_anim = new Animation( 0.4f, traversed_frames );
		state = GameState.PLAYING;
		l.scroll(1, 0, cam_width);
		score = 0;
		game_timer = 0;
		music_initialized = false;
		disposeMusic();
	}
	
	void updateMusic() {
		//Initializing audio
		if( state == GameState.MENU && !music_initialized) {
			music = Gdx.audio.newMusic( Gdx.files.internal("mfx/intro.ogg"));
			music_initialized = true;
			music.play();
		}
		else if ( state == GameState.PLAYING && !music_initialized &&
				has_advanced) {
			if(level_num > 4)
				level_num = 1;
			music = Gdx.audio.newMusic( Gdx.files.internal
					("mfx/level"+level_num+".ogg"));
			music.setLooping(true);
			music_initialized = true;
			music.play();
			level_num++;
		}
		else if ( state == GameState.GAMEOVER && !music_initialized && 
				game_timer > 5) {
			music = Gdx.audio.newMusic
					( Gdx.files.internal("mfx/highscore.ogg"));
			music.setLooping(true);
			music_initialized = true;
			music.play();
		}
	}

	void disposeMusic() {
		music.stop();
		music.dispose();
		music_initialized = false;
	}
}

