package com.murkitty.parking.lib;

import java.util.ArrayList;
import java.util.Iterator;

public class Signal<T> {
//todo weak reference
private ArrayList<Callback<T>> callbacks = new ArrayList<Callback<T>>();

public void dispatch(T value) {
	ArrayList<Callback<T>> currentCallbacks = new ArrayList<Callback<T>>(callbacks);
	Iterator<Callback<T>> iterator = currentCallbacks.iterator();
	while(iterator.hasNext()) {
		Callback<T> next = iterator.next();
		next.listener.onSignal(value);
		if(next.once) {
			next.removed = true;
		}
	}
	iterator = callbacks.iterator();
	while(iterator.hasNext()) {
		Callback<T> next = iterator.next();
		if(next.removed) {
			iterator.remove();
		}
	}
}
public void add(SignalListener<T> listener) {
	Callback<T> c = new Callback<T>();
	c.listener = listener;
	callbacks.add(c);
}
public void addOnce(SignalListener<T> listener) {
	Callback<T> c = new Callback<T>();
	c.listener = listener;
	c.once = true;
	callbacks.add(c);
}
public void remove(SignalListener<T> signalListener) {
	Iterator<Callback<T>> iterator = callbacks.iterator();
	while(iterator.hasNext()) {
		Callback<T> next = iterator.next();
		if(next.listener == signalListener) {
			next.removed = true;
			iterator.remove();
		}
	}
}
public void destroy() {
	callbacks.clear();
}
}
class Callback<T> {
public SignalListener<T> listener;
public boolean removed = false;
public boolean once = false;
}