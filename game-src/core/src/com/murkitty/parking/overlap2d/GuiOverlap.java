package com.murkitty.parking.overlap2d;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.murkitty.parking.ShaderHelper;
import com.murkitty.parking.model.XY;
import com.uwsoft.editor.renderer.components.PolygonComponent;
import com.uwsoft.editor.renderer.components.ShaderComponent;
import com.uwsoft.editor.renderer.utils.ItemWrapper;
public class GuiOverlap extends AbstractOverlap {
public final SteeringOverlap steering;
public final AccelerationOverlap acceleration;
public final RectangleVerticesOverlap friend;
public final float kx=0.07f;
public final float ky=0.07f;
public GuiOverlap(Viewport viewport) {
	super(viewport, "gui");
	steering = new SteeringOverlap(root, root.getChild("steering"));
	acceleration = new AccelerationOverlap(root, root.getChild("acceleration"));
	friend = new RectangleVerticesOverlap(root, root.getChild("friend"));
}
public class SteeringOverlap extends RectangleVerticesOverlap {
	public final ItemOverlap wheel;
	public SteeringOverlap(ItemWrapper parent, ItemWrapper item) {
		super(parent, item);
		wheel = new ItemOverlap(item, item.getChild("wheel"));
	}
}
public class AccelerationOverlap extends RectangleVerticesOverlap {
	public final ItemOverlap pedal;
	public AccelerationOverlap(ItemWrapper parent, ItemWrapper item) {
		super(parent, item);
		pedal = new ItemOverlap(item, item.getChild("pedal"));
	}
}
public class RectangleVerticesOverlap extends HideChildOverlap {
	public final float width;
	public final float height;
	public final float y;
	public final float x;
	public RectangleVerticesOverlap(ItemWrapper parent, ItemWrapper item) {
		super(parent, item);
		ShaderComponent component = new ShaderComponent();
		component.setShader("alpha", ShaderHelper.getShader(1,1,1,0.5f));
		addComponent(component);
		Float minX = Float.POSITIVE_INFINITY;
		Float minY = Float.POSITIVE_INFINITY;
		Float maxX = Float.NEGATIVE_INFINITY;
		Float maxY = Float.NEGATIVE_INFINITY;
		for(Vector2[] vertice : item.getComponent(PolygonComponent.class).vertices) {
			for(Vector2 a : vertice) {
				minX = Math.min(a.x, minX);
				minY = Math.min(a.y, minY);
				maxX = Math.max(a.x, maxX);
				maxY = Math.max(a.y, maxY);
			}
		}
		x=minX;
		y=minY;
		width = maxX - minX;
		height = maxY - minY;
	}
	public boolean testLocalPoint(XY local) {
		return local.x > x - width * kx && local.x < x + width + width * kx && local.y > y - height * ky && local.y < y + height + height * ky;
	}
	public boolean testGlobalPoint(XY global) {
		return testLocalPoint(globalToLocal(global));
	}
	public XY globalToLocal(XY global) {
		return global.sub(getPos()).scale(1/getScale());
	}
}
}
