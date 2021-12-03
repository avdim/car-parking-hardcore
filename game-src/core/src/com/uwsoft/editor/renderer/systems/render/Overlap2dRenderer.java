package com.uwsoft.editor.renderer.systems.render;

import box2dLight.RayHandler;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.murkitty.parking.App;
import com.uwsoft.editor.renderer.commons.IExternalItemType;
import com.uwsoft.editor.renderer.components.*;
import com.uwsoft.editor.renderer.physics.PhysicsBodyLoader;
import com.uwsoft.editor.renderer.systems.render.logic.DrawableLogicMapper;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;

public class Overlap2dRenderer extends IteratingSystem {
private ComponentMapper<ViewPortComponent> viewPortMapper = ComponentMapper.getFor(ViewPortComponent.class);
private ComponentMapper<CompositeTransformComponent> compositeTransformMapper = ComponentMapper.getFor(CompositeTransformComponent.class);
private ComponentMapper<NodeComponent> nodeMapper = ComponentMapper.getFor(NodeComponent.class);
private ComponentMapper<ParentNodeComponent> parentNodeMapper = ComponentMapper.getFor(ParentNodeComponent.class);
private ComponentMapper<TransformComponent> transformMapper = ComponentMapper.getFor(TransformComponent.class);
private ComponentMapper<MainItemComponent> mainItemComponentMapper = ComponentMapper.getFor(MainItemComponent.class);
private ComponentMapper<ShaderComponent> shaderComponentComponentMapper = ComponentMapper.getFor(ShaderComponent.class);

private DrawableLogicMapper drawableLogicMapper;
private RayHandler rayHandler;

public static float timeRunning = 0;

public Batch batch;

public Overlap2dRenderer(Batch batch) {
	super(Family.all(ViewPortComponent.class).get());
	this.batch = batch;
	drawableLogicMapper = new DrawableLogicMapper();
}
public void addDrawableType(IExternalItemType itemType) {
	drawableLogicMapper.addDrawableToMap(itemType.getTypeId(), itemType.getDrawable());
}
@Override
public void processEntity(Entity entity, float deltaTime) {
	timeRunning += deltaTime;
	ViewPortComponent ViewPortComponent = viewPortMapper.get(entity);
	Camera camera = ViewPortComponent.viewPort.getCamera();
	camera.update();
	batch.setProjectionMatrix(camera.combined);
	batch.begin();
	drawRecursively(entity, 1f);
	batch.end();
//	kinda not cool (this should be done in separate lights renderer maybe?
	if(rayHandler != null) {
//	rayHandler.setCulling(false);
		OrthographicCamera orthoCamera = (OrthographicCamera) camera;
		camera.combined.scl(1f / PhysicsBodyLoader.getScale());
		rayHandler.setCombinedMatrix(orthoCamera);
		rayHandler.updateAndRender();
	}
}
private void drawRecursively(Entity rootEntity, float parentAlpha) {
	CompositeTransformComponent curCompositeTransformComponent = compositeTransformMapper.get(rootEntity);
	TransformComponent transform = transformMapper.get(rootEntity);
	ShaderComponent shaderComponent = shaderComponentComponentMapper.get(rootEntity);
	boolean shaderExist = shaderComponent != null && shaderComponent.getShader() != null;
	if(shaderExist) {
		batch.setShader(shaderComponent.getShader());
	}
	if(curCompositeTransformComponent.transform || transform.rotation != 0 || transform.scaleX != 1 || transform.scaleY != 1) {
		computeTransform(rootEntity);
		applyTransform(rootEntity, batch);
	}
	TintComponent tintComponent = ComponentRetriever.get(rootEntity, TintComponent.class);
	parentAlpha *= tintComponent.color.a;
	if(parentAlpha < 1) {
		App.breakpoint();
	}
	drawChildren(rootEntity, batch, curCompositeTransformComponent, parentAlpha);
	if(curCompositeTransformComponent.transform || transform.rotation != 0 || transform.scaleX != 1 || transform.scaleY != 1)
		resetTransform(rootEntity, batch);
	if(shaderExist) {
		batch.setShader(null);
	}
}
private void drawChildren(Entity rootEntity, Batch batch, CompositeTransformComponent curCompositeTransformComponent, float parentAlpha) {
	NodeComponent nodeComponent = nodeMapper.get(rootEntity);
	TransformComponent transform = transformMapper.get(rootEntity);
	float offsetX = 0;
	float offsetY = 0;
	if(curCompositeTransformComponent.transform || transform.rotation != 0 || viewPortMapper.has(rootEntity) || transform.scaleX != 1 || transform.scaleY != 1) {

	} else {
		offsetX = transform.x;
		offsetY = transform.y;
	}
	Entity[] children = nodeComponent.children.begin();
	for(int i = 0, n = nodeComponent.children.size; i < n; i++) {
		Entity child = children[i];
		MainItemComponent childMainItemComponent = mainItemComponentMapper.get(child);
		if(!childMainItemComponent.visible) {
			App.breakpoint();
			continue;
		}
		if(!App.params.performanceFixes2()) {
			LayerMapComponent rootLayers = ComponentRetriever.get(rootEntity, LayerMapComponent.class);
			ZIndexComponent childZIndexComponent = ComponentRetriever.get(child, ZIndexComponent.class);
			if(!rootLayers.isVisible(childZIndexComponent.layerName)) {
				continue;
			}
		}
		TransformComponent childTransformComponent = transformMapper.get(child);
		childTransformComponent.x += offsetX;
		childTransformComponent.y += offsetY;
		if(nodeMapper.get(child) == null) {
			drawableLogicMapper.getDrawable(childMainItemComponent.entityType).draw(batch, child, parentAlpha);
		} else {
			drawRecursively(child, parentAlpha);
		}
		childTransformComponent.x -= offsetX;
		childTransformComponent.y -= offsetY;
	}
	nodeComponent.children.end();
}
protected Matrix4 computeTransform(Entity rootEntity) {
	CompositeTransformComponent curCompositeTransformComponent = compositeTransformMapper.get(rootEntity);
	//NodeComponent nodeComponent = nodeMapper.get(rootEntity);
	ParentNodeComponent parentNodeComponent = parentNodeMapper.get(rootEntity);
	TransformComponent curTransform = transformMapper.get(rootEntity);
	Affine2 worldTransform = curCompositeTransformComponent.worldTransform;
	float x = curTransform.x;
	float y = curTransform.y;
	float rotation = curTransform.rotation;
	float scaleX = curTransform.scaleX;
	float scaleY = curTransform.scaleY;
	worldTransform.setToTrnRotScl(x, y, rotation, scaleX, scaleY);
	CompositeTransformComponent parentTransformComponent = null;
	Entity parentEntity = null;
	if(parentNodeComponent != null) {
		parentEntity = parentNodeComponent.parentEntity;
	}
	if(parentEntity != null) {
		parentTransformComponent = compositeTransformMapper.get(parentEntity);
		TransformComponent transform = transformMapper.get(parentEntity);
		if(curCompositeTransformComponent.transform || transform.rotation != 0 || transform.scaleX != 1 || transform.scaleY != 1)
			worldTransform.preMul(parentTransformComponent.worldTransform);
	}
	curCompositeTransformComponent.computedTransform.set(worldTransform);
	return curCompositeTransformComponent.computedTransform;
}
protected void applyTransform(Entity rootEntity, Batch batch) {
	CompositeTransformComponent curCompositeTransformComponent = compositeTransformMapper.get(rootEntity);
	curCompositeTransformComponent.oldTransform.set(batch.getTransformMatrix());
	batch.setTransformMatrix(curCompositeTransformComponent.computedTransform);
}
protected void resetTransform(Entity rootEntity, Batch batch) {
	CompositeTransformComponent curCompositeTransformComponent = compositeTransformMapper.get(rootEntity);
	batch.setTransformMatrix(curCompositeTransformComponent.oldTransform);
}
public void setRayHandler(RayHandler rayHandler) {
	this.rayHandler = rayHandler;
}

}

