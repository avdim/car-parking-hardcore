package com.murkitty.parking.view;
import com.murkitty.parking.lib.SignalListener;
import com.murkitty.parking.model.Barrier;
import com.murkitty.parking.overlap2d.WorldOverlap;
import com.uwsoft.editor.renderer.components.MainItemComponent;
public class BarrierView extends StatObjView {
private WorldOverlap.BarrierOverlap overlap;
private SignalListener<Void> listener;
private Barrier barrier;
public BarrierView(final WorldOverlap.BarrierOverlap overlap, final Barrier barrier) {
	super(overlap, barrier);
	this.barrier = barrier;
	this.overlap = overlap;
	listener = new SignalListener<Void>() {
		@Override
		public void onSignal(Void arg) {
			if(barrier.isOpen()) {
				overlap.close.hide();
				overlap.open.show();
			} else {
				overlap.open.hide();
				overlap.close.show();
			}
		}
	};
	if(barrier.isOpen()) {
		overlap.close.hide();
	} else {
		overlap.open.hide();
	}
	barrier.update.add(listener);
}
@Override
public void destroy() {
	barrier.update.remove(listener);
	overlap.entity.removeAll();
	overlap = null;
	super.destroy();
}
}
