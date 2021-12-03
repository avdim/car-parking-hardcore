package com.murkitty.parking.overlap2d;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.systems.IntervalIteratingSystem;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.murkitty.parking.App;
import com.murkitty.parking.lib.SignalListener;
import com.murkitty.parking.model.Angle;
import com.murkitty.parking.model.TransformRotationAngle;
import com.murkitty.parking.model.XY;
import com.murkitty.parking.model.XYA;
import com.uwsoft.editor.renderer.SceneLoader;
import com.uwsoft.editor.renderer.components.MainItemComponent;
import com.uwsoft.editor.renderer.components.NodeComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.systems.ButtonSystem;
import com.uwsoft.editor.renderer.systems.CompositeSystem;
import com.uwsoft.editor.renderer.systems.LabelSystem;
import com.uwsoft.editor.renderer.systems.LayerSystem;
import com.uwsoft.editor.renderer.systems.LightSystem;
import com.uwsoft.editor.renderer.systems.ParticleSystem;
import com.uwsoft.editor.renderer.systems.PhysicsSystem;
import com.uwsoft.editor.renderer.systems.ScriptSystem;
import com.uwsoft.editor.renderer.systems.SpriteAnimationSystem;
import com.uwsoft.editor.renderer.systems.action.ActionSystem;
import com.uwsoft.editor.renderer.systems.render.Overlap2dRenderer;
import com.uwsoft.editor.renderer.utils.ComponentRetriever;
import com.uwsoft.editor.renderer.utils.ItemWrapper;

import java.util.ArrayList;
import java.util.List;
public abstract class AbstractOverlap {
public final SceneLoader sceneLoader;//todo private
protected final ItemWrapper root;
public AbstractOverlap(Viewport viewport, String scene) {
	sceneLoader = new SceneLoader();
	sceneLoader.loadScene(scene, viewport);
	if(true) {
		sceneLoader.getEngine().getSystem(PhysicsSystem.class).setPhysicsOn(false);
		sceneLoader.getEngine().getSystem(PhysicsSystem.class).setProcessing(false);
		sceneLoader.getEngine().getSystem(SpriteAnimationSystem.class).setProcessing(false);
		sceneLoader.getEngine().getSystem(ParticleSystem.class).setProcessing(false);
		sceneLoader.getEngine().getSystem(LabelSystem.class).setProcessing(false);
		sceneLoader.getEngine().getSystem(ButtonSystem.class).setProcessing(false);
		sceneLoader.getEngine().getSystem(ScriptSystem.class).setProcessing(false);
		sceneLoader.getEngine().getSystem(ActionSystem.class).setProcessing(false);
		sceneLoader.getEngine().getSystem(LayerSystem.class).setProcessing(true);
		sceneLoader.getEngine().getSystem(CompositeSystem.class).setProcessing(false);
		sceneLoader.getEngine().getSystem(Overlap2dRenderer.class);
		sceneLoader.getEngine().getSystem(SortedIteratingSystem.class);
		sceneLoader.getEngine().getSystem(IntervalIteratingSystem.class);
		sceneLoader.getEngine().getSystem(IteratingSystem.class);
	}
	if(false) {
		sceneLoader.getEngine().getSystem(Overlap2dRenderer.class).setRayHandler(null);
		sceneLoader.getEngine().getSystem(LightSystem.class).setProcessing(false);
	}
	root = new ItemWrapper(sceneLoader.getRoot());
	App.tickSignal.add(new SignalListener<Float>() {
		@Override
		public void onSignal(Float deltaTime) {
			sceneLoader.getEngine().update(deltaTime);
		}
	});
	if(false) {
		SnapshotArray<Entity> children = sceneLoader.getRoot().getComponent(NodeComponent.class).children;
		for(Entity child : children) {
			MainItemComponent mainItemComponent = ComponentRetriever.get(child, MainItemComponent.class);
			String itemIdentifier = mainItemComponent.itemIdentifier;
			new ItemWrapper(child);
		}
	}
}
public class ItemOverlap {
	private final TransformComponent transform;
	public final Entity entity;
	public final ItemWrapper item;
	private final ItemWrapper parent;
	private boolean visible = true;
	public ItemOverlap(ItemWrapper parent, ItemWrapper item) {
		this.parent = parent;
		this.item = item;
		this.entity = item.getEntity();
		transform = entity.getComponent(TransformComponent.class);
	}
	public XY getPos() {
		XY result = new XY();
		result.x = transform.x;
		result.y = transform.y;
		return result;
	}
	public void setPos(XY pos) {
		transform.x = pos.x;
		transform.y = pos.y;
	}
	public void setScale(float scale) {
		transform.scaleX = transform.scaleY = scale;
	}
	public float getScale() {
		return (transform.scaleX + transform.scaleY)/2;
	}
	public Angle getAngle() {
		return new TransformRotationAngle(transform.rotation);
	}
	public void setAngle(Angle angle) {
		transform.rotation = angle.getTransformRotation();
	}
	public final XYA getXYA() {
		return new XYA(getPos(), getAngle());
	}
	public final void setXYA(XYA xya) {
		setPos(xya.xy);
		setAngle(xya.angle);
	}
	public void hide() {
		if(visible) {
			if(App.params.visibleHide) {
				item.getComponent(MainItemComponent.class).visible = false;
			} else {
				parent.getComponent(NodeComponent.class).removeChild(entity);
				sceneLoader.getEngine().removeEntity(entity);
			}
			visible = false;
		}
	}
	public void show() {
		if(!visible) {
			if(App.params.visibleHide) {
				item.getComponent(MainItemComponent.class).visible = true;
			} else {
				parent.getComponent(NodeComponent.class).addChild(entity);
//				sceneLoader.getEngine().addEntity(entity);
			}
			visible = true;
		}
	}
	public <T extends Component> T getComponent(Class<T> clazz) {
		return entity.getComponent(clazz);
	}
	public void addComponent(Component component) {
		entity.add(component);
	}
}
public class HideChildOverlap extends ItemOverlap {
	List<ItemOverlap> childs = new ArrayList<ItemOverlap>();
	public HideChildOverlap(ItemWrapper parent, ItemWrapper item) {
		super(parent, item);
		NodeComponent nodeComponent = item.getComponent(NodeComponent.class);
		if(nodeComponent != null) {
			for(Entity child : nodeComponent.children) {
				ItemOverlap childOverlap = new HideChildOverlap(item, new ItemWrapper(child));
				childs.add(childOverlap);
			}
		}
	}
	@Override
	public void hide() {
		for(ItemOverlap child : childs) {
			child.hide();
		}
		super.hide();
	}
	@Override
	public void show() {
		super.show();
		for(ItemOverlap child : childs) {
			child.show();
		}
	}
}
}
