package com.murkitty.parking.lib;
public enum SchedulerPriority {
	box2d,
	logic;

public int getPriority() {
	return this.ordinal();
}

}
