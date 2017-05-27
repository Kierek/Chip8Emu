package com.kierek.chip8emu.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.kierek.chip8emu.Chip8Emu;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = 640;
		config.height = 320;

		new LwjglApplication(new Chip8Emu(), config);
	}
}
