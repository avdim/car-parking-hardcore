package com.murkitty.parking.model;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactFilter;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.murkitty.parking.App;
import com.murkitty.parking.Joystick;
import com.murkitty.parking.PerformanceMeasure;
import com.murkitty.parking.lib.Lib;
import com.murkitty.parking.lib.Signal;
import com.murkitty.parking.lib.SignalListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class WorldModel {
private final Joystick joystick;
public final World box2dWorld;//maybe private
private Car myCar;
private List<Car> cars = new ArrayList<Car>();
private List<DynObj> dynObjs = new ArrayList<DynObj>();
private CarKinematic kinematic;
private List<Sensor> sensors = new ArrayList<Sensor>();
private List<Hand> hands = new ArrayList<Hand>();
public static final int DELTA = 20;
public final Signal<ControlCarChange> myCarChange = new Signal<ControlCarChange>();
private Map<ContactWrapper, Damage> damageMap = new HashMap<ContactWrapper, Damage>();
final public Signal<DamageResult> onDamage = new Signal<DamageResult>();
private float currentForce = 0;
private float currentTurn = 0;
private float dForce;
private float dTurn;
public WorldModel(final Joystick joystick) {
	this.joystick = joystick;
	box2dWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(0, 0), true);
	box2dWorld.setAutoClearForces(true);
	box2dWorld.setContinuousPhysics(false);
	box2dWorld.setContactFilter(new ContactFilter() {
		@Override
		public boolean shouldCollide(Fixture fixtureA, Fixture fixtureB) {
			for(Hand hand : hands) {
				if(fixtureA.getBody() == hand.body || fixtureB.getBody() == hand.body) {
					return false;
				}
			}
			return true;
		}
	});
	box2dWorld.setContactListener(new ContactListener() {
		@Override
		public void beginContact(Contact contact) {
			Fixture overSensor = null;
			for(Sensor sensor : sensors) {
				if(contact.getFixtureA().getBody() == sensor.body) {
					overSensor = contact.getFixtureB();
				} else if(contact.getFixtureB().getBody() == sensor.body) {
					overSensor = contact.getFixtureA();
				}
				if(overSensor != null) {
					if(overSensor.isSensor()) {
						return;
					}
					if(sensor.validateTarget(overSensor.getBody().getUserData())) {
						sensor.collisions.add(overSensor);
						sensor.needUpdate = true;
					}
					break;
				}
			}
			if(overSensor == null) {//Если контакт между двумя физическими объектами
				Car carA = null;
				if(cars.contains(contact.getFixtureA().getBody().getUserData())) {
					carA = (Car) contact.getFixtureA().getBody().getUserData();
				}
				Car carB = null;
				if(cars.contains(contact.getFixtureB().getBody().getUserData())) {
					carB = (Car) contact.getFixtureB().getBody().getUserData();
				}
				if(carA != null || carB != null) {
					damageMap.put(new ContactWrapper(contact), new Damage());
				}
			}
		}
		@Override
		public void endContact(Contact contact) {
			for(Sensor sensor : sensors) {
				Fixture fixture = null;
				if(contact.getFixtureA().getBody() == sensor.body) {
					fixture = contact.getFixtureB();
				} else if(contact.getFixtureB().getBody() == sensor.body) {
					fixture = contact.getFixtureA();
				}
				if(fixture != null) {
					if(fixture.isSensor()) {
						return;
					}
					if(sensor.collisions.contains(fixture)) {
						sensor.collisions.remove(fixture);
						sensor.needUpdate = true;
					}
					break;
				}
			}
			if(damageMap.containsKey(new ContactWrapper(contact))) {
				damageMap.remove(new ContactWrapper(contact));
			}
		}
		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {

		}
		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {
			Body bodyA = contact.getFixtureA().getBody();
			Object userDataA = bodyA.getUserData();
			if(userDataA instanceof DynObj && dynObjs.contains(userDataA)) {
				((DynObj) userDataA).transformUpdate.dispatch(null);
			}
			Body bodyB = contact.getFixtureB().getBody();
			Object userDataB = bodyB.getUserData();
			if(userDataB instanceof DynObj && dynObjs.contains(userDataB)) {
				((DynObj) userDataB).transformUpdate.dispatch(null);
			}
			if(damageMap.containsKey(new ContactWrapper(contact))) {
				Damage d = damageMap.get(new ContactWrapper(contact));
				Car carA = null;
				if(cars.contains(bodyA.getUserData())) {
					carA = (Car) bodyA.getUserData();
				}
				Car carB = null;
				if(cars.contains(bodyB.getUserData())) {
					carB = (Car) bodyB.getUserData();
				}
				if(carA != null || carB != null) {
					d.damage += impulse.getNormalImpulses()[0] * 1.5;
					if(d.damage > 0.1f) {
						DamageResult dr = new DamageResult();
						dr.damage = (int) d.damage + 1;
						d.damage -= dr.damage;
						XY fa = (XY) contact.getFixtureA().getUserData();
						XY fb = (XY) contact.getFixtureB().getUserData();
						XY a = new XY(bodyA.getPosition());
						XY b = new XY(bodyB.getPosition());
						Angle angleA = new Angle(bodyA.getAngle());
						Angle angleB = new Angle(bodyB.getAngle());
						dr.pos = a.add(b).add(fa.rotate(angleA)).add(fb.rotate(angleB)).scale(0.5f).scale(1 / ConstModel.SCALE);
						dr.pos = (a.add(b).add(fa).add(fb)).scale(0.5f).scale(1 / ConstModel.SCALE);
						if(carA == myCar || carB == myCar) {
							kinematic.velocity *= 1 - Lib.arg0toInf(dr.damage, 5);//Уменьшаем скорость при ударе
						}
						onDamage.dispatch(dr);
					}
				}
			}
		}
	});
	if(true) {
		App.tickSignal.add(new SignalListener<Float>() {
			public float engineTickDelta = 0;
			@Override
			public void onSignal(Float deltaTime) {
				engineTickDelta+= deltaTime;
				final float engineTick = 0.016f;
				while(engineTickDelta > engineTick) {
					engineTickDelta -= engineTick;
					tick();//maybe need split to another Thread, not in GUI
				}
			}
		});
	}
	if(false) {
		//Иногда даёт ощибку на андройде когда ничего не двигается. Т.е. этот таймер не тикает, но зато в другом потоке
		Timer timer = new Timer();
		timer.scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				tick();
			}
		}, 0.016f, 0.016f);
		timer.start();
	}
}
public List<Car> getCars() {
	return cars;
}
public float getTurn() {
	return currentTurn;
}
public float getForce() {
	return currentForce;
}
public boolean myCarInContact() {
	for(Contact contact : box2dWorld.getContactList()) {
		if(contact.getFixtureA().getBody().getUserData() == myCar) {
			return true;
		} else if(contact.getFixtureB().getBody().getUserData() == myCar) {
			return true;
		}
	}
	return false;
}
private void tick() {
	final float deltaF = DELTA / 1000f;
	box2dWorld.step(deltaF, 1, 1);
	List<Sensor> needUpdate = new ArrayList<Sensor>();
	for(Sensor sensor : sensors) {
		//box2dWorld.isLocked()
		if(sensor.needUpdate) {
			sensor.needUpdate = false;
			needUpdate.add(sensor);
		}
	}
	for(Sensor s : needUpdate) {
		s.update();
	}
	//Диссипативные силы
	for(DynObj o : dynObjs) {
		if(o != myCar) {
			Vector2 linearVelocity = o.body.getLinearVelocity();
			o.body.setLinearVelocity(new Vector2(linearVelocity.x / 10, linearVelocity.y / 10));
			o.body.setAngularVelocity(o.body.getAngularVelocity() / 20);
		}
	}
	if(myCar != null) {
		kinematic.direction = new Angle(myCar.body.getAngle());
		kinematic.x = myCar.body.getPosition().x;
		kinematic.y = myCar.body.getPosition().y;
		float angleBefore = kinematic.getDirection().getRadians();
		float xBefore = myCar.body.getPosition().x;
		float yBefore = myCar.body.getPosition().y;
		final float minStep = 0.07f;
		if(joystick.getForce() == 0) {
			dForce = joystick.getForce() - currentForce;
			if(Math.abs(dForce) > minStep) {
				dForce = Lib.sign(dForce) * minStep;
			}
			currentForce += dForce;
		} else {
			currentForce = joystick.getForce();
		}
		if(joystick.getTurn() == 0 || App.params.smoothWheel) {
			dTurn = joystick.getTurn() - currentTurn;
			if(Math.abs(dTurn) > minStep) {
				dTurn = Lib.sign(dTurn) * minStep;
			}
			currentTurn += dTurn;
		} else {
			currentTurn = joystick.getTurn();
		}
		kinematic.setForce(currentForce);
		kinematic.setSteeringAngle(currentTurn * -45);
		kinematic.tick(DELTA);
		myCar.isBreak = kinematic.isBreak();
		myCar.isMoveBack = kinematic.getVelocity() < 0;
		myCar.steeringAngle = new DegreesAngle(kinematic.getSteeringAngle());
		float angleTarget = kinematic.getDirection().getRadians();
		float xTarget = kinematic.getX();
		float yTarget = kinematic.getY();
		float vx = ((xTarget - xBefore) / deltaF);
		float vy = ((yTarget - yBefore) / deltaF);
		myCar.body.setLinearVelocity(vx, vy);
		float omega = (angleTarget - angleBefore) / deltaF;
		myCar.body.setAngularVelocity(omega);
		myCar.transformUpdate.dispatch(null);
	}
	if(false) {
		box2dWorld.isLocked();
		box2dWorld.QueryAABB(new QueryCallback() {
			@Override
			public boolean reportFixture(Fixture fixture) {
				return true;
			}
		}, 0, 0, 2048 * ConstModel.SCALE, 2048 * ConstModel.SCALE);
	}
}
public Joint createFastJoint(Body bodyA, Body bodyB, float angle) {
	PrismaticJointDef prismaticJointDef = new PrismaticJointDef();
	prismaticJointDef.bodyA = bodyA;
	prismaticJointDef.bodyB = bodyB;
	prismaticJointDef.enableLimit = true;
	prismaticJointDef.referenceAngle = angle;
	return box2dWorld.createJoint(prismaticJointDef);
}
public void addCar(Car car, XYA xya) {
	car.welcomeTime = App.time;
	addDynObj(car, xya);
	this.cars.add(car);
}
public List<Car> removeAllCars() {
	ArrayList<Car> result = new ArrayList<Car>(cars);
	for(Car car : result) {
		removeCar(car);
	}
	return result;
}
public void removeCar(Car car) {
	this.cars.remove(car);
	removeDynObj(car);
}
public void addDynObj(DynObj obj, XYA xya) {
	addFormObj(obj, xya);
	this.dynObjs.add(obj);
}
public void addFormObj(FormObj obj, XYA xya) {
	addObj(obj, xya);
}
private void addObj(Obj obj, XYA xya) {
	obj.initBody(box2dWorld, xya);
}
public void addSensor(Sensor obj, XYA xya) {
	addFormObj(obj, xya);
	sensors.add(obj);
}
public void removeSensor(Sensor obj) {
	sensors.remove(obj);
	obj.removed = true;
	removeFormObj(obj);
}
public void removeDynObj(DynObj obj) {
	this.dynObjs.remove(obj);
	removeFormObj(obj);
}
public void removeFormObj(FormObj obj) {
	removeObj(obj);
}
private void removeObj(Obj obj) {
	box2dWorld.destroyBody(obj.body);
	obj.body = null;
}
public void addStatObj(StatObj obj, XYA xya) {
	addFormObj(obj, xya);
}
public void removeStatObj(StatObj obj) {
	removeFormObj(obj);
}
public Car getMyCar() {
	return myCar;
}
public void takeControlCar(Car car) {
	kinematic = new CarKinematic(car.pixelLength * ConstModel.SCALE);
	car.takeControl();
	myCar = car;
	ControlCarChange value = new ControlCarChange();
	value.next = car;
	myCarChange.dispatch(value);
}
public void stopControlCar() {
	myCar.stopControl();
	ControlCarChange value = new ControlCarChange();
	value.previous = myCar;
	myCar = null;
	myCarChange.dispatch(value);
}
public Car getCarByPoint(XY point) {
	for(Car car : cars) {
		if(car.testPoint(point)) {
			return car;
		}
	}
	return null;
}
abstract public static class VerticesObj extends FormObj {
	public VerticesObj(Vector2[][] vertices) {
		super(new VerticesComplexShape(vertices));
	}
}
abstract public static class FormObj extends Obj {
	private final ComplexShape shapeDef;
	public FormObj(ComplexShape shapeDef) {
		this.shapeDef = shapeDef;
	}
	public boolean testPoint(XY pt) {
		Vector2 scaledPoint = new Vector2(pt.x * ConstModel.SCALE, pt.y * ConstModel.SCALE);
//	body.getLocalPoint(pt);
		for(Fixture fixt : body.getFixtureList()) {
			if(fixt.testPoint(scaledPoint)) {
				return true;
			}
		}
		return false;
	}
	@Override
	final protected void initBody(World box2dWorld, XYA xya) {
		super.initBody(box2dWorld, xya);
		applyMass(getInitShapeParams());
	}
	final protected void destroyMass() {
		while(body.getFixtureList().size > 0) {
			body.destroyFixture(body.getFixtureList().get(0));
		}
	}
	final protected void applyMass(ShapeObjParams params) {
		for(ShapeAndPos shape : shapeDef.getShapes()) {
			FixtureDef fix = new FixtureDef();
			fix.shape = shape.shape;
			fix.isSensor = params.sensor;
			fix.density = params.density;
			fix.friction = params.friction;
			fix.restitution = params.restitution;
			body.createFixture(fix).setUserData(shape.pos);
//			shape.dispose();
		}
	}
	abstract protected ShapeObjParams getInitShapeParams();
}
abstract public static class Obj {
	Body body;
	private BodyDef.BodyType type;
	public XYA getXYA() {
		return new XYA(getXY(), getAngle());
	}
	public XY getXY() {
		return new XY(body.getPosition()).scale(1 / ConstModel.SCALE);
	}
	public Angle getAngle() {
		return new Angle(body.getAngle());
	}
	protected void initBody(com.badlogic.gdx.physics.box2d.World box2dWorld, XYA xya) {
		BodyDef def = new BodyDef();
		def.type = getType();
		def.position.x = xya.xy.x * ConstModel.SCALE;
		def.position.y = xya.xy.y * ConstModel.SCALE;
		def.angle = xya.angle.getRadians();
		body = box2dWorld.createBody(def);
		body.setUserData(this);
	}
	public final XY localToGlobal(XY in) {
		double cos = getAngle().cos();
		double sin = getAngle().sin();
		float x = (float) (in.x * cos - in.y * sin + getXY().x);
		float y = (float) (in.x * sin + in.y * cos + getXY().y);
		return new XY(x, y);
	}
	abstract protected BodyDef.BodyType getType();
}
public static class Sensor<T extends Obj> extends FormObj {
	public final Signal<T> onEnter = new Signal<T>();
	public final Signal<T> onExit = new Signal<T>();
	private final Set<Fixture> collisions = new HashSet<Fixture>();
	private final Set<T> elems = new HashSet<T>();
	private final Class<T> tClass;
	private T target;
	private boolean needUpdate = false;
	private boolean removed = false;
	private BodyDef.BodyType type;
	public Sensor(Class<T> tClass, ComplexShape shapeDef, BodyDef.BodyType type) {
		super(shapeDef);
		this.tClass = tClass;
		this.type = type;
	}
	public void setTarget(T target) {
		this.target = target;
	}
	@Override
	protected BodyDef.BodyType getType() {
		return type;
	}
	@Override
	protected ShapeObjParams getInitShapeParams() {
		ShapeObjParams shapeObjParams = new ShapeObjParams();
		shapeObjParams.sensor = true;
		return shapeObjParams;
	}
	private Set<Body> getCollisionBodies() {
		HashSet<Body> result = new HashSet<Body>();
		for(Fixture fixture : collisions) {
			if(!result.contains(fixture.getBody())) {
				result.add(fixture.getBody());
			}
		}
		return result;
	}
	public Set<T> getCollisionElements() {
		HashSet<T> result = new HashSet<T>();
		for(Body body1 : getCollisionBodies()) {
			result.add((T) body1.getUserData());
		}
		return result;
	}
	public void destroy() {
		removed = true;
		onEnter.destroy();
		onExit.destroy();
	}
	private boolean validateTarget(Object userData) {
		if(userData != null) {
			boolean parentT = ClassReflection.isAssignableFrom(tClass, userData.getClass());
			if(parentT) {
				if(target != null) {
					return target == userData;
				}
				return true;
			}
		}
		return false;
	}
	public void update() {
		if(!removed) {
			for(Body body : getCollisionBodies()) {
				if(!elems.contains(body.getUserData())) {
					if(validateTarget(body.getUserData())) {
						elems.add((T) body.getUserData());
						onEnter.dispatch((T) body.getUserData());
					}
				}
			}
			Iterator<T> iterator = elems.iterator();
			c1:
			while(iterator.hasNext()) {
				T elem = iterator.next();
				for(Body body1 : getCollisionBodies()) {
					if(body1.getUserData() == elem) {
						continue c1;
					}
				}
				iterator.remove();
				onExit.dispatch(elem);
			}
		}

	}
}
public static class CirlceCompexShape implements ComplexShape {
	ArrayList<ShapeAndPos> result = new ArrayList<ShapeAndPos>();
	public CirlceCompexShape(float pixelsRadius) {
		CircleShape circle = new CircleShape();
		circle.setRadius(pixelsRadius * ConstModel.SCALE);
		ShapeAndPos e = new ShapeAndPos();
		e.shape = circle;
		e.pos = new XY();
		result.add(e);
	}
	@Override
	public List<ShapeAndPos> getShapes() {
		return result;
	}
}
public class Hand extends Obj {
	private List<Body> fingers = new ArrayList<Body>();
	private List<Joint> joints = new ArrayList<Joint>();
	public Hand(Obj... objects) {
		for(Obj obj : objects) {
			BodyDef fingerDef = new BodyDef();
			fingerDef.type = BodyDef.BodyType.KinematicBody;
			fingerDef.position.set(obj.body.getPosition());
			Body finger = box2dWorld.createBody(fingerDef);
			Joint joint = createFastJoint(obj.body, finger, -obj.body.getAngle());
			fingers.add(finger);
			joints.add(joint);
		}
		hands.add(this);
	}
	public void destroy() {
		for(Joint joint : joints) {
			box2dWorld.destroyJoint(joint);
		}
		for(Body body1 : fingers) {
			box2dWorld.destroyBody(body1);
		}

		hands.remove(this);
	}
	public void setSpeed(XY speed) {
		for(Body body1 : fingers) {
			XY resultSpeed = speed.scale(ConstModel.SCALE);
			body1.setLinearVelocity(resultSpeed.getVector());
		}
	}
	@Override
	protected BodyDef.BodyType getType() {
		return BodyDef.BodyType.KinematicBody;
	}
}
public Hand createHand(Obj... objects) {
	return new Hand(objects);
}

}
