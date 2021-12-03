package com.murkitty.parking.model;

import com.murkitty.parking.App;
import com.murkitty.parking.lib.Lib;

public class CarKinematic {
private final double ACCELERATION = 0.00015f * ConstModel.SCALE;
private final double BREAK_KOEFF = 0.01f * ConstModel.SCALE;//todo эффективность тормозов пропорциональна квадрату скорости
private final double MIN_VELOCITY = 0.001f*ConstModel.SCALE;
private final double FORM = 0.01;//Обтикаемость. Чем меньше тем быстрее машина
private final float length;
public Angle direction = new Angle(0);
private boolean isBreak;
public double x = 0;
public double y = 0;
public double velocity = 0;
private double acceleration;
private double steeringAngleRadians;//radians

public CarKinematic(float length) {
	this.length = length;
}
public boolean isBreak() {
	return isBreak;
}
public void tick(long dt) {
	double previousVelocity = velocity;
	velocity += acceleration * dt - velocity * FORM;
	if(previousVelocity * velocity < 0) {
		velocity = 0; // Полная остановка и переключение передачи
	} else {
		if(Math.abs(velocity) < MIN_VELOCITY && acceleration == 0) {
			velocity = 0;
		}
	}
	if(Math.abs(acceleration) > 0) {
		App.breakpoint();
	}
	if(velocity != 0) {
		double S = velocity * dt;
		double smallS;
		if(getRadius().isInfinite()) {
			smallS = S;
		} else {
			Angle deltaAngle = new Angle(S / getRadius());
			if(deltaAngle.getRadians() < 0.0001) {//Из за погрешности в теореме косинсов
				smallS = S;
			} else {
				smallS = Math.sqrt(2 * getRadius() * getRadius() * (1 - deltaAngle.cos())) * Lib.sign(velocity);//Cosinuses theoreme
			}
			direction = direction.add(deltaAngle);
		}
		x += smallS * direction.cos();
		y += smallS * direction.sin();
	}
	if(velocity == 0) {
		isBreak = true;
	} else {
//		isBreak = false;
	}
}

public float getX() {
	return (float) x;
}

public float getY() {
	return (float) y;
}

public void setForce(float value) { //from -1 to 1
	if(value * velocity < 0) { // Если знаки разные то торможение
		acceleration = value * Math.sqrt(Math.abs(velocity)) * BREAK_KOEFF;
		isBreak = true;
	} else {
		acceleration = value * ACCELERATION;
		isBreak = false;
	}
}

public void setSteeringAngle(float value) {//degrees from -45 to 45
	steeringAngleRadians = value * Math.PI / 180;
}

public float getSteeringAngle() {
	return (float) (steeringAngleRadians * 180 / Math.PI);
}

private Double getRadius() {
	return length / Math.tan(steeringAngleRadians);
}

public float getLength() {
	return length;
}

public Angle getDirection() {
	return direction;
}

public double getVelocity() {
	return velocity;
}

}
