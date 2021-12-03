package com.murkitty.parking.view;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.murkitty.parking.App;
import com.murkitty.parking.ShaderHelper;
import com.murkitty.parking.lib.SignalListener;
import com.murkitty.parking.model.Angle;
import com.murkitty.parking.model.Car;
import com.murkitty.parking.model.XY;
import com.murkitty.parking.overlap2d.WorldOverlap;
import com.uwsoft.editor.renderer.components.ShaderComponent;
public class CarView extends DynObjView {
private SignalListener<Void> controlListener;
public WorldOverlap.CarOverlap overlap;
private SignalListener<Void> lightListener;
private Car car;
ShaderComponent shaderComp;
private ShaderProgram redShader;
public final static ShaderProgram alpha = ShaderHelper.getShader(1, 1, 1, 0.5f);
public ShaderProgram targetShader;

public CarView(final WorldOverlap.CarOverlap overlap, final Car car) {
	super(overlap, car);
	this.overlap = overlap;
	this.car = car;
	lightListener = new SignalListener<Void>() {
		@Override
		public void onSignal(Void arg) {
			if(car.isBreak()) {
				overlap.break1.show();
				overlap.break2.show();
			} else {
				overlap.break1.hide();
				overlap.break2.hide();
			}
			if(car.isMoveBack()) {
				overlap.back1.show();
				overlap.back2.show();
			} else {
				overlap.back1.hide();
				overlap.back2.hide();
			}
			if(car.isControll()) {
				overlap.tire1.show();
				overlap.tire2.show();
				overlap.tire1.setAngle(car.getSteeringAngle());
				overlap.tire2.setAngle(car.getSteeringAngle());
				overlap.light1.show();
				overlap.light2.show();
			} else {
				overlap.light1.hide();
				overlap.light2.hide();
				overlap.tire1.hide();
				overlap.tire2.hide();
			}
		}
	};
	controlListener = new SignalListener<Void>() {
		@Override
		public void onSignal(Void arg) {
			if(!car.isControll()) {
				overlap.tire1.setAngle(new Angle(0));
				overlap.tire2.setAngle(new Angle(0));
			}
		}
	};
	overlap.light1.hide();
	overlap.light2.hide();
	overlap.break1.hide();
	overlap.break2.hide();
	overlap.back1.hide();
	overlap.back2.hide();
	overlap.tire1.hide();
	overlap.tire2.hide();
	car.transformUpdate.add(lightListener);
	car.controlChange.add(controlListener);
}
public XY getLight1Pos() {
	return overlap.light1.getPos();
}
public XY getLight2Pos() {
	return overlap.light2.getPos();
}
public XY getBacklight1Pos() {
	return overlap.back1.getPos();
}
public XY getBacklight2Pos() {
	return overlap.back2.getPos();
}
public Car getCar() {
	return car;
}
@Override
public void destroy() {
	car.transformUpdate.remove(lightListener);
	car.controlChange.remove(controlListener);
	overlap.hide();
	super.destroy();
}
public void blinkRed() {
	final SignalListener<Float> listener = new SignalListener<Float>() {
		float time = 0;
		@Override
		public void onSignal(Float arg) {
			if(shaderComp == null) {
				shaderComp = new ShaderComponent();
				overlap.addComponent(shaderComp);
			}
			double x = 0.5 + 0.5 * Math.cos(time * 2 * Math.PI * 1.1f);
			redShader = ShaderHelper.getShader((float) (1 + x * 2), 1, 1, (float) (1 - 0.5f * x));
			shaderComp.setShader("red", redShader);
			if(time > 3) {
				overlap.getComponent(ShaderComponent.class).setShader("red", targetShader);
			}
			time += arg;
		}
	};
	App.tickSignal.add(listener);
}
public void alpha() {
	if(shaderComp == null) {
		shaderComp = new ShaderComponent();
		overlap.addComponent(shaderComp);
	}
	shaderComp.setShader("red", alpha);
	targetShader = alpha;
}
public void noAlpha() {
	if(shaderComp == null) {
		shaderComp = new ShaderComponent();
		overlap.addComponent(shaderComp);
	}
	shaderComp.setShader("red", null);
	targetShader = null;
}
}
