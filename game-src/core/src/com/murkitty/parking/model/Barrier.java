package com.murkitty.parking.model;

import com.badlogic.gdx.math.Vector2;
import com.murkitty.parking.lib.Signal;
public class Barrier extends StatObj {

private boolean opened = false;
public final Signal<Void> update = new Signal<Void>();

public Barrier(Vector2[][] vertices) {
	super(vertices);
}
public void open() {
	destroyMass();
	opened = true;
	update.dispatch(null);
}

public void close() {
	applyMass(getInitShapeParams());
	opened = false;
	update.dispatch(null);
}

public boolean isOpen() {
	return opened;
}


}
