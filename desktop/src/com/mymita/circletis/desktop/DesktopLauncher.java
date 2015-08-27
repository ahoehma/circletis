package com.mymita.circletis.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mymita.circletis.Circletis;

public class DesktopLauncher {
	public static void main(final String[] arg) {
		final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.resizable = false;
		config.disableAudio = true;
		new LwjglApplication(new Circletis(), config);
	}
}
