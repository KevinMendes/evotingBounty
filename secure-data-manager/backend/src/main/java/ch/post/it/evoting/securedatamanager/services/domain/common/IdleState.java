/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.common;

public class IdleState {

	private boolean idle;

	public IdleState() {
		this.idle = false;
	}

	public boolean isIdle() {
		return idle;
	}

	public void setIdle(final boolean idle) {
		this.idle = idle;
	}
}
