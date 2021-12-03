package com.murkitty.parking;
public interface Joystick {
public float getForce();
public float getTurn();
}

//			if(currentDragPos != null) {
//				controlledCarKinematic.setForce((startDownPos.y - currentDragPos.y) / 150);
//				float angle = (startDownPos.x - currentDragPos.x) / 150 * 45;
//				angle = Math.max(-45, angle);
//				angle = Math.min(45, angle);
//				controlledCarKinematic.setSteeringAngle(angle);
//			} else {
//				controlledCarKinematic.setSteeringAngle(controlledCarKinematic.getSteeringAngle() / 1.03f);
//				controlledCarKinematic.setForce(0);
//			}

