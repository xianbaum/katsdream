package com.christianbaum.games.katsdream.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.christianbaum.games.katsdream.KatsDream;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Kat's Dream";
		config.width = 640;
		config.height = 480;
		new LwjglApplication(new KatsDream(), config);
	}
}
