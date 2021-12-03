package com.uwsoft.editor.renderer.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
public abstract class IteratingSystemWithSkips extends IteratingSystem {

private final int skips;
private int calls = 0;

public IteratingSystemWithSkips(Family family, int skips) {
	super(family);
	this.skips = skips;
}
public IteratingSystemWithSkips(Family family, int priority, int skips) {
	super(family, priority);
	this.skips = skips;
}
@Override
final protected void processEntity(Entity entity, float deltaTime) {
	if(skips == 0 || calls%skips == 0) {
		processEntity2(entity, deltaTime);
	}
	calls++;
}
abstract protected void processEntity2(Entity entity, float deltaTime);
}
