package com.murkitty.parking.lib;

import java.util.ArrayList;
import java.util.List;
public class SyncScheduler {
private List<ActionPriority> list = new ArrayList<ActionPriority>();//todo sorted List
private ISyncAction current;
public void add(SchedulerPriority priority, ISyncAction action) {
	if(current == null) {
		execute(action);
	} else {
		list.add(new ActionPriority(priority, action));
//		list.sort();//todo?
	}
}
private void execute(ISyncAction action) {
	current = action;
	action.doAction();
	if(list.size() > 0) {
		execute(list.remove(0).action);
	} else {
		current = null;
	}
}
}
class ActionPriority {
public SchedulerPriority priority;
public ISyncAction action;
public ActionPriority(SchedulerPriority priority, ISyncAction action) {
	this.priority = priority;
	this.action = action;
}
}