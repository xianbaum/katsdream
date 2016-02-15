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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** The main LibGDX method for the game.
 * @author Christian
 */
public class KatsDream extends ApplicationAdapter {
	public Level l;
	public int cam_width;
	public int cam_height;
	public int tiles_per_cam_width;
	public int tiles_per_cam_height;
	private Stage stage;
	public boolean mouse_down;
	public Point click_pos;
	public Point click_tile_pos;
	public enum GameState { MENU, HOWTO, PLAYING, GAMEOVER, DRAWING };
	public GameState state;
	private FitViewport viewport;
	private OrthographicCamera sprite_camera;
	public ArrayList<Actor> actors;
	private TextureRegion[] traversed_frames;
	private float game_timer;
	private float score;
	private BitmapFont font;
	private boolean has_advanced;
	private boolean has_been_down;
	private boolean music_initialized;
	/** The entire texture region of all of the actors */
	private Texture title_image;
	public TextureRegion[][][] texture_region;
	private Music music;
	private int level_num;
	public Sound[] sfx;
	public Dir arrow;

	@Override
	public void create () {
		
		//Initializing input
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
		Texture[] images = new Texture[11];
		for(int i = 0; i < 10; i++ )
			images[i] =
			new Texture(Gdx.files.internal("gfx/actors"+i+".png"));
		texture_region = new TextureRegion[10][][];
		texture_region[0] = TextureRegion.split(images[0], 8, 8);
		for(int i = 1; i < 8; i++)
			texture_region[i] = TextureRegion.split(images[i], 16, 16);
		texture_region[8] = TextureRegion.split(images[8], 64, 64);
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
		sfx = new Sound[8];
		sfx[0] = Gdx.audio.newSound( Gdx.files.internal("mfx/shootan.wav"));
		sfx[1] = Gdx.audio.newSound( Gdx.files.internal("mfx/kill.ogg"));
		sfx[2] = Gdx.audio.newSound( Gdx.files.internal("mfx/kill2.ogg"));
		sfx[3] = Gdx.audio.newSound( Gdx.files.internal("mfx/die.ogg"));
		sfx[4] = Gdx.audio.newSound( Gdx.files.internal("mfx/launch.ogg"));
		sfx[5] = Gdx.audio.newSound( Gdx.files.internal("mfx/dingan.wav"));
		sfx[6] = Gdx.audio.newSound( Gdx.files.internal("mfx/beep.wav"));
		sfx[7] = Gdx.audio.newSound( Gdx.files.internal("mfx/explosion.ogg"));
		//Initializing game
		state = GameState.MENU;
		level_num = 1;
	}
	
	@Override
	public void render () {
		float dt = Gdx.graphics.getDeltaTime();
		//Updating
		if( dt > 1 ) //Not more than 1 second of lag
			dt = 1;
		game_timer += dt;
		if( state == GameState.DRAWING)
			dt = 0;
		//Checking input
		checkInput();
		
		//Updating game
		if( state == GameState.PLAYING || state == GameState.GAMEOVER ||
				state == GameState.DRAWING ) {
			int temp_scroll = 0;
			if( state == GameState.PLAYING && has_advanced) {
				temp_scroll = l.scroll( dt, 1.9f, tiles_per_cam_width);
				score += dt;
			}
			if( temp_scroll != 0 ) {
				for( Actor actor : actors )
					actor.updateXFromMapChange( temp_scroll );
				actors.addAll( l.getnewEnemies(texture_region,
						tiles_per_cam_height));
			}
			ArrayList<Actor> actors_to_add = new ArrayList<Actor>();
			Iterator<Actor> actor_iter = actors.iterator();	
			while( actor_iter.hasNext() ) {
				Actor actor = actor_iter.next();
				actor.update(dt, this, actors_to_add);
				if( actor.okayToDelete() )
					actor_iter.remove();
				if( state == GameState.PLAYING && actor.isExploding() ) {
					sfx[7].play();
					game_timer = 0;
					disposeMusic();
					for( Actor actor2 : actors )
						actor2.kill();
				}
			}
			actors.addAll(actors_to_add);
			if( state == GameState.PLAYING && ((Player)actors.get(0)).isDead() ) {
				state = GameState.GAMEOVER;
				sfx[3].play();
				game_timer = 0;
				disposeMusic();
			}
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
			for( Actor actor : actors ) {
				actor.draw( batch, this);
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
			font.draw( batch, "Touch to begin!\nv1.10", cam_width/tiles_per_cam_width*6f,
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
		//Left and right mouse buttons, and touch screen are gotten.
		mouse_down = Gdx.input.isButtonPressed(Buttons.LEFT) ||
				Gdx.input.isButtonPressed(Buttons.RIGHT) || 
				Gdx.input.isTouched();
		click_pos = new Point ( Gdx.input.getX() , Gdx.input.getY() );
		Vector2 projection = viewport.unproject(
				new Vector2 ( click_pos.x, click_pos.y ) );
		float scroll = 0, width = 15, height = 20;
		if( state == GameState.PLAYING ||
			state == GameState.DRAWING ||
			state == GameState.GAMEOVER) {
			scroll = l.getScroll();
			width = l.levelWidth();
			height = l.levelHeight();
		}
		click_pos.x = projection.x + scroll;
		click_pos.y = tiles_per_cam_height - projection.y - 1;
		if( click_pos.x < 0 )
			click_pos.x = 0;
		else if( click_pos.x > width )
			click_pos.x = width;
		if( click_pos.y < 0 )
			click_pos.y = 0;
		else if( click_pos.y > height)
			click_pos.y = height;
		click_tile_pos.x = (int) click_pos.x;
		click_tile_pos.y = (float) Math.ceil( click_pos.y);
		
		//Checking input
		if(Gdx.input.isKeyJustPressed(Keys.LEFT))
			arrow = Dir.LEFT;
		else if( Gdx.input.isKeyJustPressed(Keys.RIGHT))
			arrow = Dir.RIGHT;
		else if( Gdx.input.isKeyJustPressed(Keys.UP))
			arrow = Dir.UP;
		else if( Gdx.input.isKeyJustPressed(Keys.DOWN) )
			arrow = Dir.DOWN;
		if( mouse_down ) {
			if ( state == GameState.GAMEOVER && !has_been_down) {
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
	
	public void setState( GameState state ) {
		this.state = state;
	}
	
	private void startNewGame() {
		//Initializing game variables
		l = new Level( tiles_per_cam_width, tiles_per_cam_height );
		actors = new ArrayList<Actor>();
		actors.add(new Player(6,5, texture_region[1], this ));
		has_advanced = false;
		state = GameState.PLAYING;
		l.scroll(1, 0, cam_width);
		score = 0;
		game_timer = 0;
		disposeMusic();
	}
	
	private void updateMusic() {
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

	private void disposeMusic() {
		if( music_initialized) {
			music.stop();
			music.dispose();
			music_initialized = false;
		}
	}

	public void advance() {
		has_advanced = true;
	}
}
