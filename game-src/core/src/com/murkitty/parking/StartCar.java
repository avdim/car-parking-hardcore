package com.murkitty.parking;
import com.murkitty.parking.overlap2d.WorldOverlap;
public class StartCar {
public final WorldOverlap.CarOverlap overlap;
public final App.CarParam param;
public StartCar(App.CarParam param, WorldOverlap.CarOverlap overlap) {
	this.param = param;
	this.overlap = overlap;
}
}
