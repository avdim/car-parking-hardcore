package com.murkitty.parking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.utils.Timer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class AndroidLauncher extends AndroidApplication {

	private AdView adView;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RelativeLayout layout = new RelativeLayout(this);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useGLSurfaceView20API18 = true;//todo test
		View gameView = initializeForView(new HardcoreParking(true, Language.en, null), config);
		gameView.setKeepScreenOn(true);
		layout.addView(gameView);

		if(false) {
			SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			if(!appPreferences.getBoolean("isAppInstalled",false)) {
				SharedPreferences.Editor editor = appPreferences.edit();
				editor.putBoolean("isAppInstalled", true);
				editor.apply();

				Intent shortcutIntent = new Intent(getApplicationContext(), AndroidLauncher.class);
				shortcutIntent.setAction(Intent.ACTION_MAIN);
				//shortcutIntent is added with addIntent
				Intent addIntent = new Intent();
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_launcher));
				addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
				// finally broadcast the new Intent
				getApplicationContext().sendBroadcast(addIntent);
			}
		}
		if(false) {
			adView = new AdView(this);
			adView.setAdListener(new AddListener());
			adView.setAdSize(AdSize.SMART_BANNER);
			adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");//todo
			RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			layout.addView(adView, adParams);
			AdRequest.Builder builder = new AdRequest.Builder();
			builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
			builder.addTestDevice("0B20D03D63EAB10357F80810013076F0");
			adView.loadAd(builder.build());
			adView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
					int height = adView.getHeight();
					App.breakpoint();
				}
			});
		}
		setContentView(layout);
		if(false) {
			SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
			sensorManager.registerListener(new SensorEventListener() {
				@Override
				public void onSensorChanged(SensorEvent sensorEvent) {
					float x = sensorEvent.values[0];
					float y = sensorEvent.values[1];
					float z = sensorEvent.values[2];
					App.breakpoint();
				}
				@Override
				public void onAccuracyChanged(Sensor sensor, int i) {
					App.breakpoint();
				}
			}, gravitySensor, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

private class AddListener extends AdListener {
	}

}
