package com.murkitty.parking;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.murkitty.parking.model.XY;
public class TempLabel extends AnimateLabel {
public float liveTime = 2.5f;
public TempLabel(float animationTime, XY pos, int currentValue, Color color, ShaderProgram shader) {
	super(animationTime, pos, currentValue, color, shader);
}
@Override
public String calcStr() {
	int v = calcValue();
	String result = "";
	if(v > 0) {
		return "+" + calcValue() + "$";
	} else {
		return calcValue() + "$";
	}
}
}
