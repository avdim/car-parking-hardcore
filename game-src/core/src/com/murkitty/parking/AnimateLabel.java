package com.murkitty.parking;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.murkitty.parking.model.XY;
public class AnimateLabel extends ValueLabel {
private final XY pos;
public float startTime = 0;
private final float animationTime;
private int currentValue = 0;
private int targetValue = 0;
public AnimateLabel(float animationTime, XY pos, int currentValue, Color color, ShaderProgram shader) {
	super(color, shader);
	startTime = App.time;
	this.animationTime = animationTime;
	this.startTime = currentValue;
	this.pos = pos;
}
public void add(int increase) {
	setValue(targetValue + increase);
}
public void setValue(int value) {
	targetValue = value;
	startTime = App.time;
}
public int calcValue() {
	currentValue += Math.floor(
					(targetValue - currentValue) * Math.sqrt(Math.min((App.time - startTime) / animationTime, 1))
	);
	return currentValue;
}
@Override
public XY getPos() {
	return pos;
}
}
