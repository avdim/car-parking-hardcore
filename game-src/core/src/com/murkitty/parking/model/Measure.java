package com.murkitty.parking.model;

public class Measure {

private int count;
private double average = 0;
private double max;
private double min;
private int zeros = 0;
public void add(double value) {
	if(value == 0) {
		zeros++;
	}
	if(Double.isNaN(max)) {
		max = value;
	} else {
		max = Math.max(max, value);
	}
	if(Double.isNaN(min)) {
		min = value;
	} else {
		min = Math.min(min, value);
	}
	average = (average * count + value) / ++count;
}
@Override
public String toString() {
	return "a=" + average + ", min=" + min + ", max=" + max + ", zeroes=" + zeros;
}
}
