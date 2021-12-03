package com.murkitty.parking.lib;

public class Lib {

static {
	if(arg0toInf(10, 10) != 0.5f) {
		throw new RuntimeException("Lib fail");
	}
}

public static int sign(float a) {
	return sign((double)a);
}
public static int sign(double a) {
	if(a == 0) {
		return 0;
	}
	return (int) (a/Math.abs(a));
}
public static double positive(double v) {
	if(v > 0) {
		return v;
	}
	return 0;
}
public static float positive(float v) {
	return (float)positive((double)v);
}
public static float arg0toInf(float y, float middle) {
	return y/middle/(1+y/middle);
}
}
