package com.murkitty.parking.view;
import com.murkitty.parking.lib.SignalListener;
import com.murkitty.parking.model.Car;
import com.murkitty.parking.model.ControlCarChange;
import com.murkitty.parking.model.WorldModel;
import com.murkitty.parking.overlap2d.WorldOverlap;

import java.util.HashMap;
import java.util.Map;
public class WorldView {
private final WorldModel world;
private final WorldOverlap overlap;
private final Map<Car, CarView> cars = new HashMap<Car, CarView>();
public WorldView(final WorldModel world, WorldOverlap overlap) {
	this.world = world;
	this.overlap = overlap;
	final SignalListener<Void> onCarMove = new SignalListener<Void>() {
		@Override
		public void onSignal(Void arg) {
			Car car = world.getMyCar();
			CarView carView = cars.get(car);
			followLightsToCar(carView);
			if(car.isBreak() || car.isMoveBack()) {
				showBacklights();
				followBacklightsToCar(carView);
			} else {
				hideBacklights();
			}
		}
	};
	if(world.getMyCar() != null) {
		world.getMyCar().transformUpdate.add(onCarMove);
	} else {
		hideLights();
		hideBacklights();
	}
	hideNewCarLights();
	world.myCarChange.add(new SignalListener<ControlCarChange>() {
		@Override
		public void onSignal(ControlCarChange value) {
			if(value.previous != null) {
				value.previous.transformUpdate.remove(onCarMove);
				hideLights();
				hideBacklights();
			}
			if(value.next != null) {
				value.next.transformUpdate.add(onCarMove);
				showLights();
			}
		}
	});
}
public CarView addCarView(CarView car) {
	cars.put(car.getCar(), car);
	return car;
}
public CarView removeCarView(Car car) {
	CarView remove = cars.remove(car);
	remove.destroy();
	return remove;
}
public CarView getCarView(Car car) {
	return cars.get(car);
}
public void showLights() {
	overlap.light1.show();
	overlap.light2.show();
}
public void hideLights() {
	overlap.light1.hide();
	overlap.light2.hide();
}
public void showNewCarLights() {
	overlap.newCarLight1.show();
	overlap.newCarLight2.show();
}
public void hideNewCarLights() {
	overlap.newCarLight1.hide();
	overlap.newCarLight2.hide();
}
public void showBacklights() {
	overlap.backlight1.show();
	overlap.backlight2.show();
}
public void hideBacklights() {
	overlap.backlight1.hide();
	overlap.backlight2.hide();
}
public void followLightsToCar(CarView car) {
	overlap.light1.setPos(car.getCar().localToGlobal(car.getLight1Pos()));
	overlap.light1.setAngle(car.getCar().getAngle());
	overlap.light2.setPos(car.getCar().localToGlobal(car.getLight2Pos()));
	overlap.light2.setAngle(car.getCar().getAngle());
}
public void followNewCarLightsToCar(CarView car) {
	overlap.newCarLight1.setPos(car.getCar().localToGlobal(car.getLight1Pos()));
	overlap.newCarLight1.setAngle(car.getCar().getAngle());
	overlap.newCarLight2.setPos(car.getCar().localToGlobal(car.getLight2Pos()));
	overlap.newCarLight2.setAngle(car.getCar().getAngle());
}
public void followBacklightsToCar(CarView car) {
	overlap.backlight1.setPos(car.getCar().localToGlobal(car.getBacklight1Pos()));
	overlap.backlight1.setAngle(car.getCar().getAngle());
	overlap.backlight2.setPos(car.getCar().localToGlobal(car.getBacklight2Pos()));
	overlap.backlight2.setAngle(car.getCar().getAngle());
}
}
