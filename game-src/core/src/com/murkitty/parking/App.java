package com.murkitty.parking;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.murkitty.parking.lib.Signal;
import com.murkitty.parking.lib.SignalListener;
public class App {

private static int breakpointsCalls = 0;
public static final MurLog log = new MurLog();
public static Signal<Float> tickSignal = new Signal<Float>();
public static Params params = new Params();
public static float maxCarDifficult = 0;
public static float time = 0;
static {
	Gdx.app.setLogLevel(Application.LOG_DEBUG);
	tickSignal.add(new SignalListener<Float>() {
		@Override
		public void onSignal(Float arg) {
			time+=arg;
		}
	});
}
public static void breakpoint() {
	breakpointsCalls++;
}
public static void todo() {
	//TODO
}
public static class MurLog {
	private static final String TAG = "mur-tag";
	public void error(String text) {
		Gdx.app.error(TAG, text);
	}
	public void warning(String text) {
		Gdx.app.error(TAG, text);
	}
	public void info(String text) {
		Gdx.app.log(TAG, text);
	}
	public void debug(String text) {
		Gdx.app.debug(TAG, text);
	}
}

public enum CarParam {
	car1(1, 0.5f, 0),//синяя
	car2(1.15f, 0.5f, 0),//кот
	car3(1.09f, 0.3f, 0),//чёрный седан
	car4(1.28f, 0.3f, 800),//чёрный внедорожник
	car5(1.65f, 0.1f, 1500),//пикап
	car6(1.05f, 0.6f, 0),//красная
	car7(0.66f, 0.2f, 0),//мелкая
	car8(1.2f, 0.2f, 0);//жёлтый фургон
	public final float difficult;
	public final String id;
	public final int bonus = 100;
	public final float probability;
	public final int minScore;
	CarParam(float difficult, float probability, int minScore) {
		this.difficult = difficult;
		this.probability = probability;
		this.minScore = minScore;
		id = this.toString();
		maxCarDifficult = Math.max(maxCarDifficult, difficult);
	}
	public static CarParam get(String id) {
		return valueOf(id);
	}
}

}