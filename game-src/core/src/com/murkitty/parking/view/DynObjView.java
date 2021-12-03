package com.murkitty.parking.view;
import com.murkitty.parking.lib.SignalListener;
import com.murkitty.parking.model.DynObj;
import com.murkitty.parking.overlap2d.WorldOverlap;
public class DynObjView extends ObjView {

private DynObj obj;
private SignalListener<Void> listener;

public DynObjView(WorldOverlap.ItemOverlap entity, DynObj obj) {
	super(entity, obj);
	this.obj = obj;
	listener = new SignalListener<Void>() {
		@Override
		public void onSignal(Void arg) {
			applyTransform();
		}
	};
	obj.transformUpdate.add(listener);
}

@Override
public void destroy() {
	obj.transformUpdate.remove(listener);
	obj = null;
	listener = null;
	super.destroy();
}
}
