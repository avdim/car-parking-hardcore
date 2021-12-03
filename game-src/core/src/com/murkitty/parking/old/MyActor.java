package com.murkitty.parking.old;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class MyActor extends Actor {
private Texture texture = new Texture("steering_wheel.png");
private ShapeRenderer shapeRenderer = new ShapeRenderer(200);
@Override
public void draw(Batch batch, float parentAlpha) {
	batch.draw(texture, 30, 30);
	batch.end();
	shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
	shapeRenderer.setColor(Color.BLUE);
	shapeRenderer.circle(500, 500, 200);
	shapeRenderer.end();
	batch.begin();
}
}
