package com.murkitty.parking;

public class PerformanceMeasure {
//private static final long allStart;
static {
//	allStart = System.nanoTime();
}
private long sum = 0;
public PerformanceMeasure() {
}
public Tick begin() {
	return new Tick();
}
public double relative() {
//	return ((double)sum)/(System.nanoTime() - allStart);
	return 0;
}
public class Tick {
//	private final long start;
//	private boolean complete = false;
//	private long pause = 0;
//	private long pauseStart;
	private Tick() {
//		start = System.nanoTime();
	}
	public void pause() {
//		pauseStart = System.nanoTime();
	}
	public void resume() {
//		pause += System.nanoTime() - pauseStart;
	}
	public void end() {
//		if(!complete) {
//			complete = true;
//			sum += System.nanoTime() - start - pause;
//		} else {
//			throw new RuntimeException("bad usage of PerformanceMeasure");
//		}
	}
}
}
