package com.murkitty.parking.model;

public class DegreesAngle extends Angle {
public DegreesAngle(double degrees) {
	super(degrees/180*Math.PI);
}
}
