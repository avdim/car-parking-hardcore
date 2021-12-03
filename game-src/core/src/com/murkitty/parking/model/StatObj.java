package com.murkitty.parking.model;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
public class StatObj extends WorldModel.VerticesObj {
public StatObj(Vector2[][] vertices) {
	super(vertices);
}
@Override
protected ShapeObjParams getInitShapeParams() {
	return new com.murkitty.parking.model.ShapeObjParams();
}

@Override
protected BodyDef.BodyType getType() {
	return BodyDef.BodyType.StaticBody;
}
}
