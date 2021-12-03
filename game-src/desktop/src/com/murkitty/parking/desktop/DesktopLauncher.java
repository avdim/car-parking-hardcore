package com.murkitty.parking.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.murkitty.parking.App;
import com.murkitty.parking.HardcoreParking;
import com.murkitty.parking.ICallback;
import com.murkitty.parking.Language;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 640;
		config.height = 600;
//		config.width = 1024;
//		config.height = 1024;
		new LwjglApplication(new HardcoreParking(false, Language.get("en"), null), config);
	}
}
