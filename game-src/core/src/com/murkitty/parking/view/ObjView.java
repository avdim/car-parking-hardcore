package com.murkitty.parking.view;

import com.murkitty.parking.model.WorldModel;
import com.murkitty.parking.overlap2d.WorldOverlap;
public class ObjView {
private final WorldOverlap.ItemOverlap item;
private WorldModel.Obj obj;
public ObjView(WorldOverlap.ItemOverlap item, WorldModel.Obj obj) {
	this.item = item;
	obj.getXY();
	obj.getAngle();
	this.obj = obj;
	applyTransform();
}
public void applyTransform() {
	item.setXYA(obj.getXYA());
}
public void destroy() {

}
}
