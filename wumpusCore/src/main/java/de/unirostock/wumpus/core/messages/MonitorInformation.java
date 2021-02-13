package de.unirostock.wumpus.core.messages;

import de.unirostock.wumpus.core.entities.Context;
import de.unirostock.wumpus.core.world.Action;

public class MonitorInformation extends Message {

	private Action action;

	public MonitorInformation() {}

	public MonitorInformation(Context context) {
		super(context, context.getMonitorUrl());
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public Action getAction() {
		return action;
	}
}
