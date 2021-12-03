package com.murkitty.parking.model;
import com.badlogic.gdx.physics.box2d.Shape;

import java.util.List;
public interface ComplexShape {
	List<ShapeAndPos> getShapes();
}
