package com.murkitty.parking.model;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
public class ContactWrapper {
public final Fixture fixtureA;
public final Fixture fixtureB;
public ContactWrapper(Contact contact) {
	fixtureA = contact.getFixtureA();
	fixtureB = contact.getFixtureB();
}
@Override
public int hashCode() {
	return fixtureA.hashCode() * fixtureB.hashCode();
}
@Override
public boolean equals(Object obj) {
	ContactWrapper o = (ContactWrapper) obj;
	if(o.fixtureA == fixtureA) {
		return o.fixtureB == fixtureB;
	} else if(o.fixtureB == fixtureA) {
		return o.fixtureA == fixtureB;
	}
	return false;
}
}
