package com.murkitty.parking.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
public class XY {
public float x;
public float y;
public XY(float x, float y) {
	this.x = x;
	this.y = y;
}
public XY() {
	x = 0;
	y = 0;
}
public XY(Vector2 vector) {
	this.x = vector.x;
	this.y = vector.y;
}
public XY(Vector3 vector) {
	x = vector.x;
	y = vector.y;
}
public XY add(XY a) {
	XY result = new XY();
	result.x = this.x + a.x;
	result.y = this.y + a.y;
	return result;
}

public XY sub(XY a) {
	return add(a.scale(-1));
}

public XY scale(float scl) {
	XY result = new XY();
	result.x = this.x * scl;
	result.y = this.y * scl;
	return result;
}

public Vector2 getVector() {
	return new Vector2(x, y);
}
public double dst(XY xy) {
	return Math.sqrt((xy.x - x) * (xy.x - x) + (xy.y - y) * (xy.y - y));
}
public XY rotate(Angle angleA) {
	Vector2 vector = this.getVector();
	Angle angle = new DegreesAngle(vector.angle()).add(angleA);
	float len = vector.len();
	return new XY(len*angle.cos(), len * angle.sin());
}
}
