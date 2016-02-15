package com.christianbaum.games.katsdream;

import java.util.ArrayList;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

/** The Level class two maps, randomly chosen.
 * @author Christian
 */
public class Level {
	/** The maps for each level. Must be at at least as large as the screen */
	private TiledMap[] map = new TiledMap[2];
	/** Two OrthogonalTiledMapRenderers for each map */
	private OrthogonalTiledMapRenderer[] renderer
		= new OrthogonalTiledMapRenderer[2];
	/** Two OrthographicCameras, one for each map */
	private OrthographicCamera[] camera = new OrthographicCamera[2];
	/** Two collision instances, one for each map */
	private int[][] collision = new int[2][];
	/** The total amount the level has scrolled */
	private float total_scroll;
	/** The amount the current left map has scrolled, relative to the left map*/
	private float left_map_scroll;
	/** The total amount of maps passed */
	private int total_maps;
	/** The amount of tiles of the left map */
	private int left_map_width;
	/** The left map alternates. This byte keeps track of which map is left. */
	private byte left_map;
	
	/** Constructor. Sets up a level
	 * @param The amount of tiles displayed on the screen at once (X value)
	 * @param The amount of tiles displayed on the screen at once (Y value)
	 */
	Level( int tiles_per_cam_width , int tiles_per_cam_height ) {
		total_scroll = 0;
		left_map_scroll = 0;
		left_map_width = 20;
		left_map = 0;
		collision = new int[2][];
		_load(0,0);
		_load(1,1);
		for(int i=0; i<2; i++) {
			camera[i]= new OrthographicCamera();
			camera[i].setToOrtho(false, tiles_per_cam_width, 
					tiles_per_cam_height ); }
	}
	
	/** Gets the left map.
	 * @return Level.left_map
	 */
	private byte l() {
		return left_map;
	}
	
	/** Gets the right map
	 * @return The opposite of Level.left_map
	 */
	private byte r() {
		return (left_map == 0 ) ? (byte) 1 : 0;
	}
	
	/** Updates the camera's x position
	 * @param The amount of tiles per at once
	 */
	private void scrollCam( int tiles_per_cam_width ) {
		camera[l()].position.x = left_map_scroll + tiles_per_cam_width/2;
		camera[l()].update();
		camera[r()].position.x = left_map_scroll - left_map_width 
				+ tiles_per_cam_width/2;
		camera[r()].update();
	}
	
	/** Loads a map.
	 * @param map_no The left or right map to load to
	 * @param level The level to load
	 */
	private void _load(int map_no, int level) {
        map[map_no] = new TmxMapLoader(new InternalFileHandleResolver()).
        		load("map/"+level+".tmx");
       	renderer[map_no] = new OrthogonalTiledMapRenderer(map[map_no], 1/16f);
       	TiledMapTileLayer layer = (TiledMapTileLayer) 
       			map[map_no].getLayers().get(0);
       	int width = map[map_no].getProperties().get("width", Integer.class);
       	collision[map_no] = new int[width*15];
       	for(int x=0; x<width; x++)
       		for(int y=0; y<15; y++) {
       			collision[map_no][x*15+y] = (layer.getCell(x, 14-y).getTile().
       					getProperties().get( "col" , Integer.class) == null) 
       					? 0 : Integer.parseInt( layer.getCell(x, 14-y).getTile().
       						getProperties().get( "col" , String.class));
       		}
	}

	/** Gets the total scroll amount
	 * @return Level.total_scroll, the total scroll amount
	 */
	public float total_scroll() {
		return total_scroll;
	}
	
	/** Gets the total amount of maps scrolled.
	 * @return Level.total_maps, the total amount of maps loaded
	 */
	public int total_maps() {
		check( 1 );

		return total_maps;
	}
	
	/** Checks if the next map should be loaded or not.
	 * Loads a new map if it should.
	 * Only needs to be called when scrolled.
	 * @param tiles_per_cam_width The amount of tiles displayed on the screen at once (X)
	 * 
	 * @return true if a new map is loaded; false if not.
	 */
	private int check(int tiles_per_cam_width) {
		if( left_map_scroll >= left_map_width) {
			total_maps++;
			map[l()].dispose();
			left_map = r();
			int random_map = (int)Math.round(2+Math.random()*14);
			if( left_map == 1 )
				_load(r(), random_map );
			else //for later
				_load(r(), random_map );
			int left_map_width_return = left_map_width;
	        left_map_scroll -= left_map_width;
			left_map_width = map[l()].
					getProperties().get("width", Integer.class);
			scrollCam( tiles_per_cam_width );
			return left_map_width_return;
		}
		scrollCam( tiles_per_cam_width );
		return 0;
	}
	
	/** Scrolls the screen from delta time and scroll speed.
	 * @param dt delta time
	 * @param scroll_speed The speed to scroll by
	 * @return whether a new map was loaded
	 */
	public int scroll( float dt, float scroll_speed, int cam_width) { 
		float scroll_to_add = scroll_speed*dt;
		total_scroll += scroll_to_add;
		left_map_scroll += scroll_to_add;
		return check(cam_width);
	}
	
	/** Gets the collision type for the current tile, from x and y
	 *  @param x the X position to be checked
	 *  @param y the Y position to be checked
	 * @return The collision type of the tile gotten by x and y
	 */
	public int getCollisionType(int x, int y) {	
		byte map_no = l();
		int index = x*15+y;
		if (index >= left_map_width*15) {
			map_no = r();
			index -= left_map_width*15;
		}
		//If it's out of bounds, it is not traversable.
		if(x < 0 || x >= levelWidth() ||
		   y < 0 || y >= levelHeight() ||
		   index >= collision[map_no].length )
			return 1;
		return collision[map_no][index];
	}
	
	/** Gets the collision type for the current tile, from a point
	 * @param point The point containing the x and y position
	 * @return The collision type of the tile gotten by x and y
	 */
	public int getCollisionType( Point point ) {
		return getCollisionType( (int)point.x, (int)point.y );
	}
	
	/** Gets the scroll from the left map.
	 * @return Level.left_map_scroll
	 */
	public float getScroll()
	{
		return left_map_scroll;
	}
	
	/** Gets the total scroll amount
	 * @return Level.total_scroll
	 */
	public float getTotalScroll()
	{
		return total_scroll;
	}
	
	/** Gets the total amount of maps scrolled
	 * @return total_maps
	 */
	public int getTotalMaps() {
		return total_maps;
	}
	
	/** Returns the level width based on both map widths
	 * @return Level width
	 */
	public int levelWidth() {
		return left_map_width + 
				map[r()].getProperties().get("width", Integer.class);
	}
	
	/** Returns the level height baed on map 0's height
	 * @return Level height
	 */
	public int levelHeight() {
		return map[0].getProperties().get("height", Integer.class);
	}
	
	/** Returns the width of the left map
	 * @return Left map width
	 */
	public int leftMapWidth() {
		return left_map_width;
	}
	
	/** Draws the map.
	 * Should be called immediately after clearing screen 
	 */
	public void draw() {
		for(int i=0; i<2; i++) {
			renderer[i].setView(camera[i]);
			renderer[i].render();
		}
	}
	
	/** Gets the enemies from the newly loaded map
	 * @param texture_region The list of textures
	 * @param tiles_per_cam_height The amount of tiles displayed on the screen at once
	 * @return an ArrayList containing the enemies
	 */
	public ArrayList<Enemy> getnewEnemies(TextureRegion[][][] texture_region,
			int tiles_per_cam_height ) {
		MapObjects objects = map[r()].getLayers().get(1).getObjects();
		ArrayList<Enemy> enemies = new ArrayList<Enemy>();
		for( MapObject object : objects ) {
			int x = object.getProperties().get("x", Float.class).intValue()/16+
					left_map_width;
			int y = tiles_per_cam_height-2-
					object.getProperties().get("y", Float.class).intValue()/16;
			switch( object.getProperties().get("e", "0", String.class) ) {
			case "0":
				enemies.add( new Cart( x, y, texture_region[6], false ) );
				break;
			case "1":
				enemies.add( new Cart( x, y, texture_region[6], true ) );
				break;
			case "2":
				enemies.add( new Slug( x, y, 2));
				break;
			case "3":
				enemies.add( new Slug( x, y, 3));
				break;
			case "4":
				enemies.add( new Slug( x, y, 4));
				break;
			case "5":
				enemies.add( new Slug( x, y, 5));
				break;
			case "6":
				enemies.add( new Plane( x, y));
				break;
			case "7":
				enemies.add( new Bomb( x, y));
				break;
			}
		}
		return enemies;		
	}
}
