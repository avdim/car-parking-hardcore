package com.murkitty.parking.overlap2d;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.murkitty.parking.model.Angle;
import com.uwsoft.editor.renderer.components.PolygonComponent;
import com.uwsoft.editor.renderer.components.TransformComponent;
import com.uwsoft.editor.renderer.components.light.LightObjectComponent;
import com.uwsoft.editor.renderer.data.CompositeItemVO;
import com.uwsoft.editor.renderer.utils.ItemWrapper;
public class WorldOverlap extends AbstractOverlap{
public final WallOverlap wall;
public final TrashcanOverlap trashcan1;
public final TrashcanOverlap trashcan2;
public final BarrierOverlap barrierIn;
public final SensorOverlap sensorBarrierIn;//Первый сенсор который открывает и закрывает шлагбаум
public final SensorOverlap sensorExit;//Сенсор выхода машины из зоны парковки для её удаления
public final SensorOverlap sensorBarrierOut;//Сенсор шлагбаума на выход
public final SensorOverlap sensorWrongCar;//Сенсор когда хочет выехать не та машина
public final BarrierOverlap barrierOut;
public final LightOverlap light1;
public final LightOverlap light2;
public final LightOverlap newCarLight1;
public final LightOverlap newCarLight2;
public final LightOverlap backlight1;
public final LightOverlap backlight2;
public final ItemOverlap greenPath1;
public final ItemOverlap greenPath2;
public final ItemOverlap greenPath3;
public final ItemOverlap roadSignWrongWay;
public CarOverlap car1;
public CarOverlap car2;
public CarOverlap car4;
public CarOverlap car6;
public CarOverlap car7;
public WorldOverlap(Viewport viewport) {
	super(viewport, "MainScene");
	light1 = new LightOverlap(root, root.getChild("light1"));
	light2 = new LightOverlap(root, root.getChild("light2"));
	newCarLight1 = new LightOverlap(root, root.getChild("new_car_light1"));
	newCarLight2 = new LightOverlap(root, root.getChild("new_car_light2"));
	backlight1 = new LightOverlap(root, root.getChild("backlight1"));
	backlight2 = new LightOverlap(root, root.getChild("backlight2"));
	wall = new WallOverlap(root, root.getChild("wall"));
	trashcan1 = new TrashcanOverlap(root, root.getChild("trashcan1"));
	trashcan2 = new TrashcanOverlap(root, root.getChild("trashcan2"));
	barrierIn = new BarrierOverlap(root, root.getChild("barrier_in"));
	barrierOut = new BarrierOverlap(root, root.getChild("barrier_out"));
	sensorBarrierIn = new SensorOverlap(root, root.getChild("sensor_barrier_in"));
	sceneLoader.getEngine().removeEntity(sensorBarrierIn.entity);
	sensorExit = new SensorOverlap(root, root.getChild("sensor_exit"));
	sceneLoader.getEngine().removeEntity(sensorExit.entity);
	sensorBarrierOut = new SensorOverlap(root, root.getChild("sensor_barrier_out"));
	sceneLoader.getEngine().removeEntity(sensorBarrierOut.entity);
	sensorWrongCar = new SensorOverlap(root, root.getChild("sensor_wrong_car"));
	greenPath1 = new ItemOverlap(root, root.getChild("green_path1"));
	greenPath2 = new ItemOverlap(root, root.getChild("green_path2"));
	greenPath3 = new ItemOverlap(root, root.getChild("green_path3"));
	roadSignWrongWay = new ItemOverlap(root, root.getChild("road_sign_wrong_car"));
	sceneLoader.getEngine().removeEntity(sensorWrongCar.entity);
	car1 = new CarOverlap(root, root.getChild("car1"));
	car2 = new CarOverlap(root, root.getChild("car2"));
	new CarOverlap(root, root.getChild("car3")).hide();
	car4 = new CarOverlap(root, root.getChild("car4"));
	new CarOverlap(root, root.getChild("car5")).hide();
	car6 = new CarOverlap(root, root.getChild("car6"));
	car7 = new CarOverlap(root, root.getChild("car7"));
	new CarOverlap(root, root.getChild("car8")).hide();
}
public CarOverlap createNewCarOverlap(String libraryId) {
	return new CarOverlap(root, new ItemWrapper(createNewEntity(libraryId)));
}
public ItemOverlap createNewGreenPathOverlap(String libraryId) {
	return new ItemOverlap(root, new ItemWrapper(createNewEntity(libraryId)));
}
private Entity createNewEntity(String libraryId) {
	CompositeItemVO vo = sceneLoader.loadVoFromLibrary(libraryId);
	Entity carEntity = sceneLoader.entityFactory.createEntity(sceneLoader.getRoot(), vo);
	sceneLoader.entityFactory.initAllChildren(sceneLoader.getEngine(), carEntity, vo.composite);
	sceneLoader.getEngine().addEntity(carEntity);
	return carEntity;
}
public class WallOverlap extends ItemOverlap {
	public final Vector2[][] vertices;
	public WallOverlap(ItemWrapper parent, ItemWrapper item) {
		super(parent, item);
		vertices = item.getChild("body").getComponent(PolygonComponent.class).vertices;
	}
}
public class TrashcanOverlap extends ItemOverlap {
	public final Vector2[][] vertices;
	public TrashcanOverlap(ItemWrapper parent, ItemWrapper item) {
		super(parent, item);
		vertices = item.getChild("body").getComponent(PolygonComponent.class).vertices;
	}
}
public class SensorOverlap extends ItemOverlap {
	public final Vector2[][] vertices;
	public SensorOverlap(ItemWrapper parent, ItemWrapper item) {
		super(parent, item);
		vertices = entity.getComponent(PolygonComponent.class).vertices;
	}
}
public class LightOverlap extends ItemOverlap {
	private final LightObjectComponent lightComponent;
	public LightOverlap(ItemWrapper parent, ItemWrapper item) {
		super(parent, item);
		lightComponent = entity.getComponent(LightObjectComponent.class);
	}
	@Override
	public void hide() {
		lightComponent.lightObject.setActive(false);
//		super.hide();
	}
	@Override
	public void show() {
//		super.show();
		lightComponent.lightObject.setActive(true);
	}
	@Override
	public void setAngle(Angle angle) {
		lightComponent.directionDegree = angle.getDegrees();
	}
	public void setDistance(float value) {
		lightComponent.distance = value;
	}
	public void setDistance(double value) {
		setDistance((float)value);
	}
}
public class CarOverlap extends ItemOverlap {
	public final ItemOverlap light1;
	public final ItemOverlap light2;
	public final ItemOverlap body;
	public final ItemOverlap break1;
	public final ItemOverlap break2;
	public final ItemOverlap back1;
	public final ItemOverlap back2;
	public final ItemOverlap tire1;
	public final ItemOverlap tire2;
	public final Vector2[][] vertices;
	public final float carLength;
	public CarOverlap(ItemWrapper parent, ItemWrapper item) {
		super(parent, item);
		carLength = item.getChild("tire1").getComponent(TransformComponent.class).x;
		item.getChild("tire2");
		light1 = new HideChildOverlap(item, item.getChild("light1"));
		light2 = new HideChildOverlap(item, item.getChild("light2"));
		break1 = new HideChildOverlap(item, item.getChild("break1"));
		break2 = new HideChildOverlap(item, item.getChild("break2"));
		back1 = new HideChildOverlap(item, item.getChild("back1"));
		back2 = new HideChildOverlap(item, item.getChild("back2"));
		body = new ItemOverlap(item, item.getChild("body"));
		tire1 = new HideChildOverlap(item, item.getChild("tire1"));
		tire2 = new HideChildOverlap(item, item.getChild("tire2"));
		vertices = body.getComponent(PolygonComponent.class).vertices;
	}
}
public class BarrierOverlap extends ItemOverlap {
	public final ItemOverlap open;
	public final ItemOverlap close;
	public final Vector2[][] vertices;
	public BarrierOverlap(ItemWrapper parent, ItemWrapper item) {
		super(parent, item);
		open = new ItemOverlap(item, item.getChild("open"));
		close = new ItemOverlap(item, item.getChild("close"));
		vertices = close.entity.getComponent(PolygonComponent.class).vertices;
	}
}

}
