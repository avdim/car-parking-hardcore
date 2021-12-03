package com.murkitty.parking.model;

import com.badlogic.gdx.math.Vector2;
import com.murkitty.parking.App;
import com.murkitty.parking.lib.Lib;
import com.murkitty.parking.lib.Signal;
public class Car extends DynObj {

public final float pixelLength;
public final XY parktronic1;
public final XY parktronic2;
boolean isBreak;
boolean isMoveBack;
private boolean controll;
Angle steeringAngle = new Angle(0);
public final Signal<Void> controlChange = new Signal<Void>();
private int maxScore=100;
private float startMoveOutTime = 0;
public App.CarParam params;
public float welcomeTime;
public Car(Vector2[][] vertices, float pixelLength, XY parktronic1, XY parctronic2) {
	super(vertices);
	this.pixelLength = pixelLength;
	this.parktronic1 = parktronic1;
	this.parktronic2 = parctronic2;
}
public boolean isBreak() {
	return isBreak;
}
public boolean isMoveBack() {
	return isMoveBack;
}
public Angle getSteeringAngle() {
	return steeringAngle;
}
public boolean isControll() {
	return controll;
}
public void takeControl() {
	//Then we control car, it changes his physics properties to looks fine in steering rotation
	destroyMass();
	applyMass(new ShapeObjParams());
	controll = true;
	controlChange.dispatch(null);
}
public void stopControl() {
	isBreak = false;
	isMoveBack = false;
	destroyMass();
	applyMass(getInitShapeParams());
	controll = false;
	transformUpdate.dispatch(null);
	controlChange.dispatch(null);
}
public XY getCenter() {
	return getRelativePos(0.5f);
}
public XY getRelativePos(float relative) {
	float dx = pixelLength * relative;
	return getXY().add(new XY(dx * getAngle().cos(), dx * getAngle().sin()));
}
public void moveOut() {
	startMoveOutTime = App.time;
}
public boolean isMovedOut() {
	return startMoveOutTime > 0;
}
public int score() {
	return (int) Math.floor(maxScore * (1 - Lib.arg0toInf(App.time - startMoveOutTime, 30)));
}
}
