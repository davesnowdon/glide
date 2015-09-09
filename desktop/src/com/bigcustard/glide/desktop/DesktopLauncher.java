package com.bigcustard.glide.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglInput;
import com.bigcustard.glide.PlanetApplication;

public class DesktopLauncher {
	public static void main (String[] arg) {
		System.setProperty("org.jruby.embed.localvariable.behavior", "transient");
		System.setProperty("org.lwjgl.opengl.Display.enableOSXFullscreenModeAPI", "true");

		LwjglInput.keyRepeatInitialTime = 0.2f;
		LwjglInput.keyRepeatTime = 0.04f;
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "General Language Integrated Development Environment";
        config.width = 1024;
        config.height = 768;
		new LwjglApplication(new PlanetApplication(new LwjglMouseWindowChecker()), config);
	}
}