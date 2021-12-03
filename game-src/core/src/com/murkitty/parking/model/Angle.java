package com.murkitty.parking.model;
import com.murkitty.parking.App;
public class Angle {

static {
	Angle pi = new Angle(2 * Math.PI);
//	if(Math.abs(pi.radians) > 0.0001f) {
//		throw new RuntimeException("test fail");
//	}
//	Angle minusPi = new Angle(-2.00001 * Math.PI);
//	if(Math.abs(minusPi.radians) > 0.01f) {
//		throw new RuntimeException("test fail");
//	}
//	Angle angle1 = new Angle(2 * Math.PI + 0.5f);
//	if(angle1.radians < 0 || angle1.radians > 2 * Math.PI) {
//		throw new RuntimeException("test fail");
//	}
//	Angle angle2 = new Angle(-2 * Math.PI - 0.5f);
//	if(angle2.radians < 0 || angle2.radians > 2 * Math.PI) {
//		throw new RuntimeException("test fail");
//	}
}

private float radians;

public Angle(double radians) {
	this.radians = (float) radians;
	fix();
}

public Angle(float radians) {
	this.radians = radians;
	fix();
}

private void fix() {
	int circles = (int) (radians / (2 * Math.PI));
	if(Math.abs(circles) > 0) {
		App.breakpoint();
	}
//	radians -= circles * 2 * Math.PI;
//	if(radians < 0) {
//		radians += 2 * Math.PI;
//	}
}

public float getRadians() {
	return radians;
}

public float getDegrees() {
	return (float) (radians * 180 / Math.PI);
}

public float getTransformRotation() {
	return getDegrees();
}

public float sin() {
	return (float) Math.sin(radians);
}

public float cos() {
	return (float) Math.cos(radians);
}

public Angle add(double radians) {
	return new Angle(this.radians + radians);
}

public void addThis(double radians) {
	this.radians+=radians;
	fix();
	throw new RuntimeException("bad");
}

public Angle add(Angle deltaAngle) {
	return new Angle(this.radians + deltaAngle.radians);
}

public Angle subtract(Angle sub) {
	return new Angle(this.radians - sub.radians);
}

}
