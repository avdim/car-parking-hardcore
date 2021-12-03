package com.murkitty.parking.model;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import java.util.ArrayList;
import java.util.List;
public class VerticesComplexShape implements ComplexShape {

ArrayList<ShapeAndPos> result = new ArrayList<ShapeAndPos>();
public VerticesComplexShape(Vector2[][] verticesArr) {
	for(Vector2[] vertices : verticesArr) {
		XY xy = new XY();
		Vector2[] scaledVertices = new Vector2[vertices.length];
		for(int i = 0; i < vertices.length; i++) {
			scaledVertices[i] = new Vector2(vertices[i].x * ConstModel.SCALE, vertices[i].y * ConstModel.SCALE);
			xy.x+=scaledVertices[i].x;
			xy.y+=scaledVertices[i].y;
		}
		PolygonShape shape = new PolygonShape();
		shape.set(scaledVertices);
		ShapeAndPos e = new ShapeAndPos();
		e.shape = shape;
		e.pos = xy.scale(1f/vertices.length);
		result.add(e);
	}
}
@Override
public List<ShapeAndPos> getShapes() {
	return result;
}

}
