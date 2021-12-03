package com.murkitty.parking.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.murkitty.parking.lib.Signal;
public class DynObj extends WorldModel.VerticesObj {

public final Signal<Void> transformUpdate = new Signal<Void>();

public DynObj(Vector2[][] vertices) {
	super(vertices);
}
@Override
protected ShapeObjParams getInitShapeParams() {
	ShapeObjParams shapeObjParams = new ShapeObjParams();
	shapeObjParams.density =1;
	shapeObjParams.friction = 1;
	shapeObjParams.restitution=0.5f;
	return shapeObjParams;
}

@Override
protected BodyDef.BodyType getType() {
	return BodyDef.BodyType.DynamicBody;
}
}
