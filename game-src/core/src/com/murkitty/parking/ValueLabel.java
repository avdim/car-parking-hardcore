package com.murkitty.parking;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.murkitty.parking.model.XY;
abstract public class ValueLabel {
private Color color;
private ShaderProgram shader;
public ValueLabel(Color color, ShaderProgram shader) {
	this.color = color;
	this.shader = shader;
}
public Color getColor() {
	return color;
}
abstract protected int calcValue();
abstract public XY getPos();
public String calcStr() {
	return calcValue() + "$";
}
public ShaderProgram getShader() {
	return shader;
}
}


