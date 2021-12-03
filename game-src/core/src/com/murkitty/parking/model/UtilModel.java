package com.murkitty.parking.model;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import java.util.ArrayList;
public class UtilModel {

public static ArrayList<FixtureDef> getFixturesDefFromVertices(Vector2[][] verticesArr, float density, float friction, float restitution, boolean sensor) {
	ArrayList<FixtureDef> result = new ArrayList<FixtureDef>();
	for(Vector2[] vertices : verticesArr) {
		Vector2[] scaledVertices = new Vector2[vertices.length];
		for(int i = 0; i < vertices.length; i++) {
			scaledVertices[i] = new Vector2(vertices[i].x * ConstModel.SCALE, vertices[i].y * ConstModel.SCALE);
		}

		PolygonShape shape = new PolygonShape();
		shape.set(scaledVertices);
		FixtureDef controlFixture = new FixtureDef();
		controlFixture.shape = shape;
		controlFixture.isSensor = sensor;
		controlFixture.density = density;
		controlFixture.friction = friction;
		controlFixture.restitution = restitution;//todo maybe remove?
		result.add(controlFixture);
//		shape.dispose(); //todo dispose нужно делать только после того как body.createFixture(fixtureDef);
	}
	return result;
}

public static ArrayList<FixtureDef> getFixturesDefFromVertices(Vector2[][] vertivesArr, boolean sensor) {
	return getFixturesDefFromVertices(vertivesArr, 0,0,0,sensor);
}

public static ArrayList<FixtureDef> getFixturesDefFromVertices(Vector2[][] vertivesArr) {
	return getFixturesDefFromVertices(vertivesArr, false);
}

public static ArrayList<FixtureDef> getFixturesDefFromVertices(Vector2[][] vertivesArr, float density, float friction, float restitution) {
	return  getFixturesDefFromVertices(vertivesArr, density, friction, restitution, false);
}

}
