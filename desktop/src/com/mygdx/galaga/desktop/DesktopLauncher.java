package com.mygdx.galaga.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.galaga.Galaga;

public class DesktopLauncher
{
	public static void main (String[] arg) 
	{
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Galaga";
		config.width = 1366;
		config.height = 768;
		new LwjglApplication(new Galaga(), config);
	}
}