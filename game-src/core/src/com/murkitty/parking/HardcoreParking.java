package com.murkitty.parking;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.murkitty.parking.lib.Lib;
import com.murkitty.parking.lib.LibAllGwt;
import com.murkitty.parking.lib.SignalListener;
import com.murkitty.parking.model.Angle;
import com.murkitty.parking.model.Barrier;
import com.murkitty.parking.model.Car;
import com.murkitty.parking.model.ConstModel;
import com.murkitty.parking.model.ControlCarChange;
import com.murkitty.parking.model.DamageResult;
import com.murkitty.parking.model.DegreesAngle;
import com.murkitty.parking.model.DynObj;
import com.murkitty.parking.model.StatObj;
import com.murkitty.parking.model.VerticesComplexShape;
import com.murkitty.parking.model.WorldModel;
import com.murkitty.parking.model.XY;
import com.murkitty.parking.model.XYA;
import com.murkitty.parking.overlap2d.AbstractOverlap;
import com.murkitty.parking.overlap2d.GuiOverlap;
import com.murkitty.parking.overlap2d.WorldOverlap;
import com.murkitty.parking.view.BarrierView;
import com.murkitty.parking.view.CarView;
import com.murkitty.parking.view.DynObjView;
import com.murkitty.parking.view.WorldView;
import com.uwsoft.editor.renderer.components.ShaderComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
public class HardcoreParking extends ApplicationAdapter {
public static final int NEW_CAR_BONUS = 50;
public final static float MAX_DIFFICULT = 10;
public static final int SIZE = 1600;
private final boolean displayWheel;
private final boolean touchScreen;
//private final boolean singleTap;
public int width;
public int height;
private SpriteBatch spriteBatch;
private BitmapFont bitmapFont;
Viewport worldViewport;
Viewport guiViewport;
private OrthographicCamera camera = new OrthographicCamera();
private Map<Integer, TouchData> touchMap = new TreeMap<Integer, TouchData>(new Comparator<Integer>() {
	@Override
	public int compare(Integer o1, Integer o2) {
		return o1 - o2;
	}
});
private Car newCar;
private float newCarBonus;
private WorldModel world;
private WorldOverlap overlap;
private WorldModel.Sensor<Car> barrierInSensor;
private Barrier barrierIn;
private Barrier barrierOut;
private WorldModel.Sensor<Car> barrierOutSensor;
private WorldModel.Sensor<Car> wrongCarSensor;
private WorldView view;
private GuiOverlap guiOverlap;
private List<TempLabel> incomeLabels = new ArrayList<TempLabel>();
private List<TempLabel> damageLabels = new ArrayList<TempLabel>();
private List<ValueLabel> valueLabels = new ArrayList<ValueLabel>();
private float vibrateOverTimeMs = 0;
private int score = 100;
private int currentScore = score;
private float currentScoreTime;
private XY greenPath1Start;
private XY greenPath2Start;
private XY greenPath3Start;
private final ICallback<Void> inviteFriends;
public final String wrongCar;
public final String takeMe;
private boolean keyPressed = false;

public HardcoreParking(boolean displayWheel, Language language, ICallback<Void> inviteFriends) {
	this.displayWheel = displayWheel;
	this.touchScreen = displayWheel;
	this.inviteFriends = inviteFriends;
	switch(language) {
		case ru:
			wrongCar = "Другая\nмашина";
			takeMe = "Возьми меня";
			break;
		default:
			wrongCar = "Wrong\ncar";
			takeMe = "Take me";
	}
}

@Override
public void create() {
	width = Gdx.graphics.getWidth();
	height = Gdx.graphics.getHeight();
	if(true) {
		Texture texture;
		FileHandle fontTexture = Gdx.files.internal("dejavu_sans_mono.png");
		if(true) {
			//Increase size
			texture = new Texture(fontTexture);
			texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		} else {
			//To make your font look better when down-scaled:
			texture = new Texture(fontTexture, true);
			if(true) {
				texture.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear);
			} else {
				//slower
				texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
			}
		}
		bitmapFont = new BitmapFont(Gdx.files.internal("dejavu_sans_mono.fnt"), new TextureRegion(texture), false);
	} else {
		bitmapFont = new BitmapFont(Gdx.files.internal("dejavu_sans_mono.fnt"));
	}
	bitmapFont.setUseIntegerPositions(true);
	worldViewport = new ExtendViewport(SIZE / 2, SIZE / 2, SIZE, SIZE, camera);
	overlap = new WorldOverlap(worldViewport);
	guiViewport = new ScreenViewport();
	guiOverlap = new GuiOverlap(guiViewport);
	if(inviteFriends == null) {
		guiOverlap.friend.hide();
	}
	guiOverlap.acceleration.hide();
	guiOverlap.steering.hide();
	world = new WorldModel(new Joystick() {
		@Override
		public float getForce() {
			float force = getForce2();
			if(force > 1) {
				force = 1;
			} else if(force < -1) {
				force = -1;
			}
			return force;
		}
		private float getForce2() {
			if(Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
				return 1;
			} else if(Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
				return -1;
			}
			if(!touchScreen) {
				return 0;
			}
			for(TouchData touch : touchMap.values()) {
				if(touchMap.values().size() > 1) {
					App.breakpoint();
				}
				if(displayWheel && guiOverlap.acceleration.testGlobalPoint(touch.down)) {
					return guiOverlap.acceleration.globalToLocal(touch.current).y / (guiOverlap.acceleration.height / 2);
				} else if(!displayWheel || !guiOverlap.steering.testGlobalPoint(touch.down)) {
					return (touch.current.y - touch.down.y) / (guiOverlap.acceleration.height * guiOverlap.acceleration.getScale() * 0.5f);
				}
			}
			return 0;
		}
		@Override
		public float getTurn() {
			if(Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
				return -1;
			}
			if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
				return 1;
			}
			if(!touchScreen) {
				return 0;
			}
			float turn = getTurn2();
			if(turn > 1) {
				turn = 1;
			} else if(turn < -1) {
				turn = -1;
			}
			return turn;
		}
		private float getTurn2() {
			for(TouchData touch : touchMap.values()) {
				if(displayWheel && guiOverlap.steering.testGlobalPoint(touch.down)) {
					return guiOverlap.steering.globalToLocal(touch.current).x / (guiOverlap.steering.width / 2);
				} else if(!displayWheel || !guiOverlap.acceleration.testGlobalPoint(touch.down)) {
					return (touch.current.x - touch.down.x) / (guiOverlap.steering.width * guiOverlap.steering.getScale() * 0.5f);
				}
			}
			return 0;
		}
	});
	world.onDamage.add(new SignalListener<DamageResult>() {
		@Override
		public void onSignal(final DamageResult arg) {
			AnimateLabel text = LibAllGwt.doNow(new LibAllGwt.IDoNow<AnimateLabel>() {
				@Override
				public AnimateLabel doNow() {
					for(AnimateLabel d : damageLabels) {
						if(d.getPos().dst(arg.pos) < 130) {
							return d;
						}
					}
					TempLabel result = new TempLabel(2.0f, arg.pos, 0, Color.RED, ShaderHelper.FONT_RED);
					damageLabels.add(result);
					valueLabels.add(result);
					return result;
				}
			});
			text.add(-arg.damage);
			addScore(-arg.damage);
			if(App.params.vibrate && arg.damage > 1) {
				if(App.time > vibrateOverTimeMs) {
					int vibrateIntervalMs = (int) (40 + 40 * (1 - 1f / arg.damage));
					vibrateOverTimeMs = App.time + vibrateIntervalMs/1000f + 2.5f;
					Gdx.input.vibrate(vibrateIntervalMs);
				}
			}
		}
	});
	view = new WorldView(world, overlap);
	world.addStatObj(new StatObj(overlap.wall.vertices), overlap.wall.getXYA());
	DynObj trashcan1 = new DynObj(overlap.trashcan1.vertices);
	world.addDynObj(trashcan1, overlap.trashcan1.getXYA());
	new DynObjView(overlap.trashcan1, trashcan1);
	DynObj trashcan2 = new DynObj(overlap.trashcan2.vertices);
	world.addDynObj(trashcan2, overlap.trashcan2.getXYA());
	new DynObjView(overlap.trashcan2, trashcan2);
	barrierOut = new Barrier(overlap.barrierOut.vertices);
	world.addStatObj(barrierOut, overlap.barrierOut.getXYA());
	new BarrierView(overlap.barrierOut, barrierOut);
	barrierIn = new Barrier(overlap.barrierIn.vertices);
	world.addStatObj(barrierIn, overlap.barrierIn.getXYA());
	new BarrierView(overlap.barrierIn, barrierIn);
	WorldModel.Sensor<Car> exitSensor = new WorldModel.Sensor<Car>(Car.class, new VerticesComplexShape(overlap.sensorExit.vertices), BodyDef.BodyType.StaticBody);
	world.addSensor(exitSensor, overlap.sensorExit.getXYA());
	barrierInSensor = new WorldModel.Sensor<Car>(Car.class, new VerticesComplexShape(overlap.sensorBarrierIn.vertices), BodyDef.BodyType.StaticBody);
	world.addSensor(barrierInSensor, overlap.sensorBarrierIn.getXYA());
	wrongCarSensor = new WorldModel.Sensor<Car>(Car.class, new VerticesComplexShape(overlap.sensorWrongCar.vertices), BodyDef.BodyType.StaticBody);
	world.addSensor(wrongCarSensor, overlap.sensorWrongCar.getXYA());
	barrierOutSensor = new WorldModel.Sensor<Car>(Car.class, new VerticesComplexShape(overlap.sensorBarrierOut.vertices), BodyDef.BodyType.StaticBody);
	world.addSensor(barrierOutSensor, overlap.sensorBarrierOut.getXYA());
	List<StartCar> starts = new ArrayList<StartCar>();
	starts.add(new StartCar(App.CarParam.car1, overlap.car1));
	starts.add(new StartCar(App.CarParam.car2, overlap.car2));
	starts.add(new StartCar(App.CarParam.car4, overlap.car4));
	starts.add(new StartCar(App.CarParam.car6, overlap.car6));
	starts.add(new StartCar(App.CarParam.car7, overlap.car7));
	for(StartCar start : starts) {
		if(Math.random() > targetDifficult() / MAX_DIFFICULT) {
			Car car = new Car(start.overlap.vertices, start.overlap.carLength, start.overlap.light1.getPos(), start.overlap.light2.getPos());
			car.params = start.param;
			world.addCar(car, new XYA(start.overlap.getXYA().xy, start.overlap.getXYA().angle));
			view.addCarView(new CarView(start.overlap, car));
		} else {
			start.overlap.hide();
		}
	}
	if(false) {
		testPerformance();
	}
	overlap.car1 = null;
	overlap.car2 = null;
	overlap.car4 = null;
	overlap.car6 = null;
	overlap.car7 = null;
	greenPath1Start = overlap.greenPath1.getPos();
	greenPath2Start = overlap.greenPath2.getPos();
	greenPath3Start = overlap.greenPath3.getPos();
	hideGreenPath();
	overlap.roadSignWrongWay.hide();
	barrierOutSensor.onEnter.add(new SignalListener<Car>() {
		@Override
		public void onSignal(Car arg) {
			if(arg == world.getMyCar() && arg.isMovedOut()) {
				barrierOut.open();
			}
		}
	});
	barrierOutSensor.onExit.add(new SignalListener<Car>() {
		@Override
		public void onSignal(Car arg) {
			if(!barrierOutSensor.getCollisionElements().contains(world.getMyCar())) {
				barrierOut.close();
			}
		}
	});
	world.myCarChange.add(new SignalListener<ControlCarChange>() {
		@Override
		public void onSignal(ControlCarChange arg) {
			if(barrierOutSensor.getCollisionElements().contains(world.getMyCar())) {
				barrierOut.open();
			}
		}
	});
	exitSensor.onEnter.add(new SignalListener<Car>() {
		@Override
		public void onSignal(Car car) {
			if(car.isMovedOut()) {
				TempLabel e = new TempLabel(1.5f, new XY(car.getCenter().x, Math.max(car.getCenter().y, diagonale() / 10)), 0, Color.GREEN, ShaderHelper.FONT_GREEN);
				e.setValue(car.score());
				incomeLabels.add(e);
				valueLabels.add(e);
			}
			if(car.isMovedOut()) {
				if(world.getCars().contains(car)) {
					if(car == world.getMyCar()) {
						world.stopControlCar();
					}
					view.removeCarView(car);
					world.removeCar(car);
					if(displayWheel) {
						guiOverlap.steering.hide();
						guiOverlap.acceleration.hide();
					}
					addScore(car.score());
				}
			}
		}
	});
	Gdx.input.setInputProcessor(new InputAdapter() {
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			TouchData touchData = new TouchData();
			touchData.downCamera = new XY(camera.position);
			touchData.down = new XY(screenX, Gdx.graphics.getHeight() - screenY);
			touchData.current = touchData.down;
			touchData.time = App.time;
			touchData.pointer = pointer;
			touchMap.put(pointer, touchData);
			return true;
		}
		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			TouchData touchData = touchMap.get(pointer);
			touchData.current = new XY(screenX, Gdx.graphics.getHeight() - screenY);
			if(pointer == 0 && (world.getMyCar() == null || !touchScreen) && touchMap.size() == 1) {
				Vector2 diff = worldViewport.unproject(touchData.current.getVector()).add(worldViewport.unproject(touchData.down.getVector()).scl(-1));
				camera.position.x = touchData.downCamera.x - diff.x;
				camera.position.y = touchData.downCamera.y + diff.y;
			}
			return true;
		}
		@Override
		public boolean touchUp(final int screenX, final int screenY, int pointer, int button) {
			if(pointer == 0 && touchMap.size() == 1) {
				TouchData touchData = touchMap.get(pointer);
				if(App.time - touchData.time < 0.5 && touchData.current.dst(touchData.down) < 0.08 * diagonale()) {
					XY worldPoint = new XY(worldViewport.unproject(new Vector2(screenX, screenY)));
					XY guiPoint = new XY(screenX, Gdx.graphics.getHeight() - screenY);
					if(inviteFriends != null && guiOverlap.friend.testGlobalPoint(guiPoint) && guiOverlap.friend.testGlobalPoint(touchData.down)) {
						inviteFriends.onCallback(null);
					}else if(world.getMyCar() == null) {
						Car car = world.getCarByPoint(worldPoint);
						if(car != null) {
							if(barrierOutSensor.getCollisionElements().contains(car) && car.isMovedOut()) {
								barrierOut.open();
							}
							world.takeControlCar(car);
							keyPressed = true;
							if(displayWheel) {
								guiOverlap.steering.show();
								guiOverlap.acceleration.show();
							}
							if(App.params.vibrate) {
								Gdx.input.vibrate(25);
							}
							if(car.isMovedOut()) {
								showExitPath();
							}
						} else {
							if(newCar != null && newCar.testPoint(worldPoint)) {
								view.getCarView(newCar).blinkRed();
							}
						}
					} else {
						if(!displayWheel || !guiOverlap.steering.testGlobalPoint(guiPoint) && !guiOverlap.acceleration.testGlobalPoint(guiPoint)) {
							if(world.getMyCar().testPoint(worldPoint)
											|| App.time - touchData.time < 0.25 && touchData.current.dst(touchData.down) < 0.04 * diagonale()) {//todo duplicate
								world.stopControlCar();
								barrierOut.close();
								if(displayWheel) {
									guiOverlap.steering.hide();
									guiOverlap.acceleration.hide();
								}
								if(App.params.vibrate) {
									Gdx.input.vibrate(25);
								}
							}
						}
					}
				}
			}
			touchMap.remove(pointer);
			overlap.greenPath1.addComponent(new ShaderComponent());
			overlap.greenPath2.addComponent(new ShaderComponent());
			overlap.greenPath3.addComponent(new ShaderComponent());
			return true;
		}
	});
}
private void showExitPath() {
	//Bezie
	overlap.greenPath1.show();
	overlap.greenPath2.show();
	overlap.greenPath3.show();
	XY beziePoint = greenPath3Start.add(new XY(0, Math.abs(world.getMyCar().getCenter().x - greenPath3Start.x)));
	final Bezier<Vector2> b = new Bezier<Vector2>(world.getMyCar().getCenter().getVector(), beziePoint.getVector(), greenPath1Start.getVector());
	final int steps = (int) (b.approxLength(20) / 60);
	if(steps < 4) {
		showExit();
		return;
	}
	final float start = App.time;
	final float duration = steps * 0.1f;
	final float interval = duration / (steps - 3);
	App.tickSignal.add(new SignalListener<Float>() {
		@Override
		public void onSignal(Float arg) {
			float currentTime = App.time - start;
			if(currentTime >= duration) {
				App.tickSignal.remove(this);
				showExit();
				return;
			}
			int step = (int) (currentTime / interval);
			draw(overlap.greenPath1, step + 2, 1 - (currentTime - step * interval) / interval / 3);
			draw(overlap.greenPath2, step + 1, 1 - (currentTime - (step - 1) * interval) / interval / 3);
			draw(overlap.greenPath3, step, 1 - (currentTime - (step - 2) * interval) / interval / 3);
		}
		private void draw(AbstractOverlap.ItemOverlap item, int step, float alpha) {
			Vector2 p1 = new Vector2();
			Vector2 p2 = new Vector2();
			b.valueAt(p1, step / (float) steps);
			b.valueAt(p2, (step + 1) / (float) steps);
			item.setPos(new XY(p1));
			item.setAngle(new DegreesAngle(p2.sub(p1).angle()));
			item.getComponent(ShaderComponent.class).setShader("alpha", ShaderHelper.getShader(1, 1, 1, alpha));
			if(false) {
				AbstractOverlap.ItemOverlap greenPath = overlap.createNewGreenPathOverlap("green_path");
			}
		}
	});
}
private void showExit() {
	if(world.getMyCar() != null && world.getMyCar().isMovedOut()) {
		overlap.greenPath1.setPos(greenPath1Start);
		overlap.greenPath2.setPos(greenPath2Start);
		overlap.greenPath3.setPos(greenPath3Start);
		overlap.greenPath1.setAngle(new DegreesAngle(-90));
		overlap.greenPath2.setAngle(new DegreesAngle(-90));
		overlap.greenPath3.setAngle(new DegreesAngle(-90));
		final SignalListener<Float> exitArrowListener = new SignalListener<Float>() {
			@Override
			public void onSignal(Float arg) {
				final float speed = 4.0f;
				overlap.greenPath1.getComponent(ShaderComponent.class).setShader("alpha", ShaderHelper.getShader(1, 1, 1, (float) Lib.positive(Math.cos(speed * App.time))));
				overlap.greenPath2.getComponent(ShaderComponent.class).setShader("alpha", ShaderHelper.getShader(1, 1, 1, (float) Lib.positive(Math.cos(speed * App.time + Math.PI / 6))));
				overlap.greenPath3.getComponent(ShaderComponent.class).setShader("alpha", ShaderHelper.getShader(1, 1, 1, (float) Lib.positive(Math.cos(speed * App.time + Math.PI / 4))));
			}
		};
		App.tickSignal.add(exitArrowListener);
		world.myCarChange.addOnce(new SignalListener<ControlCarChange>() {
			@Override
			public void onSignal(ControlCarChange arg) {
				App.tickSignal.remove(exitArrowListener);
				hideGreenPath();
			}
		});
	} else {
		hideGreenPath();
	}
}
private void hideGreenPath() {
	overlap.greenPath1.hide();
	overlap.greenPath2.hide();
	overlap.greenPath3.hide();
}
public float diagonale() {
	return (float) Math.sqrt(Gdx.graphics.getWidth() * Gdx.graphics.getWidth() + Gdx.graphics.getHeight() * Gdx.graphics.getHeight());
}
public float targetDifficult() {
	final float MIN_DIFFICULT = 2;
	return MIN_DIFFICULT + (MAX_DIFFICULT - MIN_DIFFICULT) * Lib.arg0toInf(score, 850);
}
public float currentDifficult() {
	float result = 0;
	for(Car car : world.getCars()) {
		if(car.isMovedOut()) {
			result += car.params.difficult / 2;
		} else {
			result += car.params.difficult;
		}
	}
	if(newCar != null) {
		result += newCar.params.difficult;
	}
	return result;
}
public void addNewCar(String libId, final ICallback<Car> callback) {
	WorldOverlap.CarOverlap newCarOverlap = overlap.createNewCarOverlap(libId);
	final Car car = new Car(newCarOverlap.vertices, newCarOverlap.carLength, newCarOverlap.light1.getPos(), newCarOverlap.light2.getPos());
	car.params = App.CarParam.get(libId);
	newCar = car;
	newCarBonus = NEW_CAR_BONUS;
	final ValueLabel newCarLabel = new ValueLabel(Color.LIGHT_GRAY, ShaderHelper.FONT_LIGHT_GRAY) {
		@Override
		public int calcValue() {
			return (int) newCarBonus;
		}
		@Override
		public XY getPos() {
			float koeff;
			if(newCar.getRelativePos(1).y <= 0) {
				koeff = 1.4f;
			} else {
				koeff = 1.4f - Lib.arg0toInf(newCar.getRelativePos(1).y, 150) * 0.6f;
			}
			return newCar.getRelativePos(koeff);
		}
	};
	valueLabels.add(newCarLabel);
	world.addFormObj(car, new XYA(new XY(1270, -500), new DegreesAngle(90)));
	final CarView newCarView = new CarView(newCarOverlap, car);
	newCarView.alpha();
	view.addCarView(newCarView);
	final WorldModel.Sensor<DynObj> parktronic1 = new WorldModel.Sensor<DynObj>(DynObj.class, new WorldModel.CirlceCompexShape(70), BodyDef.BodyType.DynamicBody);
	world.addSensor(parktronic1, new XYA(car.localToGlobal(car.parktronic1), new Angle(0)));
	final WorldModel.Sensor<DynObj> parktronic2 = new WorldModel.Sensor<DynObj>(DynObj.class, new WorldModel.CirlceCompexShape(70), BodyDef.BodyType.DynamicBody);
	world.addSensor(parktronic2, new XYA(car.localToGlobal(car.parktronic2), new Angle(0)));
	final WorldModel.Hand hand = world.createHand(car, parktronic1, parktronic2);
	final XY speed = new XY(0, 100);
	hand.setSpeed(speed);
	final SignalListener<Float> tickListener = new SignalListener<Float>() {
		@Override
		public void onSignal(Float arg) {
			if(newCar != null) {//todo? redundant, but null pointer exception if not check then engine timer works in GUI thread
				newCar.transformUpdate.dispatch(null);
			}
		}
	};
	final SignalListener<Float> waitObstacle = new SignalListener<Float>() {
		@Override
		public void onSignal(Float arg) {
			newCarBonus -= arg * 0.7 * (newCarBonus) / NEW_CAR_BONUS;
			if(world.getMyCar() == null) {
				//Моргаем фарами
				float period = 3.0f;
				Angle angle = new Angle(App.time * 2 * Math.PI / period);
				if(Math.abs(angle.sin()) * angle.cos() > 0.32f) {
					view.followNewCarLightsToCar(view.getCarView(newCar));
					view.showNewCarLights();
				} else {
					view.hideNewCarLights();
				}
			}
		}
	};
	SignalListener<DynObj> parktronicEnter = new SignalListener<DynObj>() {
		@Override
		public void onSignal(DynObj arg) {
			if(arg != car) {
				hand.setSpeed(new XY(0, 0));
				App.tickSignal.remove(tickListener);
				App.tickSignal.add(waitObstacle);
			}
		}
	};
	parktronic1.onEnter.add(parktronicEnter);
	parktronic2.onEnter.add(parktronicEnter);
	SignalListener<DynObj> parktronicExit = new SignalListener<DynObj>() {
		@Override
		public void onSignal(DynObj arg) {
			for(DynObj dynObj : parktronic1.getCollisionElements()) {
				if(dynObj != newCar) {
					return;
				}
			}
			for(DynObj dynObj : parktronic2.getCollisionElements()) {
				if(dynObj != newCar) {
					return;
				}
			}
			hand.setSpeed(speed);
			App.tickSignal.add(tickListener);
			App.tickSignal.remove(waitObstacle);
			App.tickSignal.addOnce(new SignalListener<Float>() {
				@Override
				public void onSignal(Float arg) {
					view.hideNewCarLights();
				}
			});
		}
	};
	parktronic1.onExit.add(parktronicExit);
	parktronic2.onExit.add(parktronicExit);
	barrierInSensor.setTarget(car);
	barrierInSensor.onEnter.addOnce(new SignalListener<Car>() {
		@Override
		public void onSignal(Car arg) {
			barrierIn.open();
		}
	});
	App.tickSignal.add(tickListener);
	barrierInSensor.onExit.addOnce(new SignalListener<Car>() {
		@Override
		public void onSignal(Car arg) {
			barrierIn.close();
			hand.destroy();
			world.removeSensor(parktronic1);
			parktronic1.destroy();
			world.removeSensor(parktronic2);
			parktronic2.destroy();
			XYA xya = car.getXYA();
			world.removeFormObj(car);
			world.addCar(car, xya);
			App.tickSignal.remove(tickListener);
			valueLabels.remove(newCarLabel);
			addScore((int) newCarBonus);
			TempLabel e = new TempLabel(1.5f, newCarLabel.getPos(), 0, Color.GREEN, ShaderHelper.FONT_GREEN);
			e.setValue((int) newCarBonus);
			incomeLabels.add(e);
			valueLabels.add(e);
			newCarView.noAlpha();
			newCar = null;
			worldViewport.project(car.getXY().scale(1 / 20f).getVector());
			callback.onCallback(car);
		}
	});
}
private boolean first = true;//todo?
@Override
public void resize(int width, int height) {
	this.width = width;
	this.height = height;
	worldViewport.update(width, height);
	overlap.sceneLoader.rayHandler.update();
	overlap.sceneLoader.rayHandler.useCustomViewport(worldViewport.getScreenX(), worldViewport.getScreenY(), worldViewport.getScreenWidth(), worldViewport.getScreenHeight());
	if(first) {
		camera.position.x = 850;
		first = false;
	}
	if(true || displayWheel) {//Нужно для высчитывания размера джойстика
		guiViewport.update(width, height, true);
		guiOverlap.acceleration.setScale(diagonale() / 3 / guiOverlap.acceleration.height);
		guiOverlap.steering.setScale(diagonale() / 3 / guiOverlap.steering.width);
		guiOverlap.acceleration.setPos(new XY(0, height / 2));
		guiOverlap.steering.setPos(new XY(width / 2, 0));
		guiOverlap.friend.setPos(new XY(width - 10, height - 4));
		guiOverlap.friend.setScale(diagonale() / 4 / guiOverlap.steering.width);
	}
	if(App.params.fixScreenRotationSpriteBatchCrop) {
		if(spriteBatch != null) {
			spriteBatch.dispose();
		}
		spriteBatch = new SpriteBatch();
	}
}
public void fixCameraBounds() {
	float ration = ((float)Gdx.graphics.getWidth()) / Gdx.graphics.getHeight();
	if(ration > 2 || ration < 0.5) {
		return;
	}
	Vector2 p00 = worldViewport.unproject(new Vector2(0, 0));
	boolean changeX = false;
	boolean changeY = false;
	if(p00.x < 0) {
		camera.position.x -= p00.x;
		changeX = true;
	}
	if(p00.y > SIZE) {
		camera.position.y -= p00.y - SIZE;
		if(changeX) {
			return;
		} else {
			changeY = true;
		}
	}
	Vector2 p11 = worldViewport.unproject(new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
	if(p11.x > SIZE) {
		camera.position.x -= p11.x - SIZE;
		if(changeY) {
			return;
		}
	}
	if(p11.y < 0) {
		camera.position.y -= p11.y;
	}
}
@Override
public void render() {
	int moveOutCount = 0;
	ArrayList<Car> cars = new ArrayList<Car>(world.getCars());
	for(Car car : cars) {
		if(car.isMovedOut()) {
			moveOutCount++;
		}
	}
	if(moveOutCount < 2) {
		double probability = 0.40;
		if(moveOutCount == 1) {
			probability = 0.08;
		}
		if(Math.random() < probability && currentDifficult() > targetDifficult() * 0.95) {
			if(Math.random() < (currentDifficult() - targetDifficult()) / MAX_DIFFICULT) {
				Collections.sort(cars, new Comparator<Car>() {
					@Override
					public int compare(Car o1, Car o2) {
						return Lib.sign(o1.welcomeTime - o2.welcomeTime);
					}
				});
				for(Car car : cars) {
					if(!car.isMovedOut() && Math.random() < 0.7) {
						car.moveOut();
						break;
					}
				}
			}
		}
	}
	if(newCar == null && currentDifficult() < targetDifficult() * 1.05) {
		List<App.CarParam> params = Arrays.asList(App.CarParam.values());
		App.CarParam carParam = params.get(LibAllGwt.getRand(0, params.size() - 1));
		if(score >= carParam.minScore && Math.random() < carParam.probability) {
			addNewCar(carParam.id, new ICallback<Car>() {
				@Override
				public void onCallback(Car value) {

				}
			});
		}
	}
	targetDifficult();
	currentDifficult();
	currentScore += (score - currentScore) * Math.min((App.time - currentScoreTime) / 1.5, 1);
	if(displayWheel) {
		guiOverlap.steering.wheel.setPos(new XY(world.getTurn() * guiOverlap.steering.width / 2, guiOverlap.steering.wheel.getPos().y));
		guiOverlap.steering.wheel.setAngle(new DegreesAngle(-45 * world.getTurn()));
		guiOverlap.acceleration.pedal.setPos(new XY(guiOverlap.acceleration.pedal.getPos().x, world.getForce() * guiOverlap.acceleration.height / 2));
	}
	Gdx.gl.glClearColor(0.4f, 0.4f, 0.4f, 1);
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	worldViewport.apply();
	fixCameraBounds();
	App.tickSignal.dispatch(Gdx.graphics.getDeltaTime());
	if(Gdx.input.isKeyPressed(Input.Keys.ANY_KEY)) {
		keyPressed = true;
	}
	if(touchMap.size() > 0) {
		keyPressed = false;
	}
	if(world.getMyCar() != null && (touchScreen || keyPressed && touchMap.size() == 0)) {
		XY xy = world.getMyCar().getCenter();
		camera.position.x = xy.x;
		camera.position.y = xy.y;
	}
	if(App.params.box2dDebugRender) {
		Box2DDebugRenderer debugRender = new Box2DDebugRenderer();
		OrthographicCamera cameraDebug = new OrthographicCamera(Gdx.graphics.getWidth() * ConstModel.SCALE * 6, Gdx.graphics.getHeight() * ConstModel.SCALE * 6);
		cameraDebug.position.set(cameraDebug.viewportWidth * 0.4f, cameraDebug.viewportHeight * 0.4f, 0f);
		cameraDebug.update();
		debugRender.render(world.box2dWorld, cameraDebug.combined);
	}
	if(App.params.fixScreenRotationSpriteBatchCrop) {
		guiViewport.apply();
	}
	spriteBatch.begin();
	//Если задаётся Shader то Color игнорируется
	bitmapFont.getData().setScale(diagonale() / 1000);
	for(ValueLabel vl : valueLabels) {
		Vector2 screenPos = worldViewport.project(vl.getPos().scale(1 / 20f).getVector());
		if(App.params.useFontShader) {
			spriteBatch.setShader(vl.getShader());
		} else {
			bitmapFont.setColor(vl.getColor());
		}
		bitmapFont.draw(spriteBatch, vl.calcStr(), screenPos.x - Gdx.graphics.getWidth() / 4, screenPos.y, Gdx.graphics.getWidth() / 2, Align.center, true);
	}
	for(Car car : world.getCars()) {
		Vector2 screenPos = worldViewport.project(car.getCenter().scale(1 / 20f).getVector());
		String str;
		if(car.isMovedOut()) {
			str = car.score() + "$";
			if(world.getMyCar() == car) {
				if(App.params.useFontShader) {
					spriteBatch.setShader(ShaderHelper.FONT_GREEN);
				} else {
					bitmapFont.setColor(Color.GREEN);
				}
			} else {
				if(App.params.useFontShader) {
					spriteBatch.setShader(ShaderHelper.FONT_YELLOW);
				} else {
					bitmapFont.setColor(Color.YELLOW);
				}
				str += "\n" + takeMe;
			}
		} else if(car == world.getMyCar() && wrongCarSensor.getCollisionElements().contains(world.getMyCar())) {
			if(App.params.useFontShader) {
				spriteBatch.setShader(ShaderHelper.FONT_RED);
			} else {
				bitmapFont.setColor(Color.RED);
			}
			str = wrongCar;
		} else {
			continue;
		}
		bitmapFont.draw(spriteBatch, str, screenPos.x - Gdx.graphics.getWidth() / 4, screenPos.y, Gdx.graphics.getWidth() / 2, Align.center, true);
	}
	if(world.getMyCar() != null && wrongCarSensor.getCollisionElements().contains(world.getMyCar()) && !world.getMyCar().isMovedOut()) {
		overlap.roadSignWrongWay.show();
	} else {
		overlap.roadSignWrongWay.hide();
	}
	List<List<TempLabel>> allLabels = new ArrayList<List<TempLabel>>();
	allLabels.add(damageLabels);
	allLabels.add(incomeLabels);
	for(List<TempLabel> l : allLabels) {
		Iterator<TempLabel> iterator = l.iterator();
		while(iterator.hasNext()) {
			TempLabel next = iterator.next();
			if(App.time - next.startTime > next.liveTime) {
				iterator.remove();
				valueLabels.remove(next);
			}
		}
	}
	if(currentScore < score) {
		if(App.params.useFontShader) {
			spriteBatch.setShader(ShaderHelper.FONT_GREEN);
		} else {
			bitmapFont.setColor(Color.GREEN);
		}
	} else if(currentScore > score) {
		if(App.params.useFontShader) {
			spriteBatch.setShader(ShaderHelper.FONT_RED);
		} else {
			bitmapFont.setColor(Color.RED);
		}
	} else {
		if(App.params.useFontShader) {
			spriteBatch.setShader(ShaderHelper.FONT_LIGHT_GRAY);
		} else {
			bitmapFont.setColor(Color.LIGHT_GRAY);
		}
	}
	bitmapFont.getData().setScale(diagonale() / 1000);
	bitmapFont.draw(spriteBatch, currentScore + "$", 10, Gdx.graphics.getHeight() - 10);
	if(App.params.displayPerformance) {
		spriteBatch.setShader(null);
		bitmapFont.setColor(Color.LIGHT_GRAY);
		bitmapFont.getData().setScale(diagonale() / 1500);
		List<String> displayLog = new ArrayList<String>();
		displayLog.add("java:   " + Gdx.app.getJavaHeap() / 1024f / 1024f);
		displayLog.add("native: " + Gdx.app.getNativeHeap() / 1024f / 1024f);
		displayLog.add("libgdx fps:    " + String.valueOf(Gdx.graphics.getFramesPerSecond()));
		String result = "";
		for(String s : displayLog) {
			result += s + "\n";
		}
		bitmapFont.draw(spriteBatch, result, 10, 150);
	}
	spriteBatch.end();
	if(false && (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.N))) {
		testPerformance();
	}
}
@Override
public void dispose() {

}
public void addScore(int add) {
	score += add;
	if(score < 0) {
		score = 0;
	}
	currentScoreTime = App.time;
}
private void testPerformance() {
	for(Car car : world.removeAllCars()) {
		view.removeCarView(car);
	}
	for(int i = 0; i < 15; i++) {
		WorldOverlap.CarOverlap carOverlap;
		String libraryId = "car" + LibAllGwt.getRand(1, 8);
		carOverlap = overlap.createNewCarOverlap(libraryId);
		Car car = new Car(carOverlap.vertices, carOverlap.carLength, carOverlap.light1.getPos(), carOverlap.light2.getPos());
		car.params = App.CarParam.get(libraryId);
		world.addCar(car, new XYA(new XY(700, 900), new Angle(0)));
		view.addCarView(new CarView(carOverlap, car));
	}
}
}
