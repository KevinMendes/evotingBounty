/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import org.springframework.stereotype.Component;

import com.orientechnologies.orient.core.Orient;

/**
 * OrientManager is used to manage the life-cycle of {@link Orient}.
 * <p>
 * Implementation must be thread-safe.
 */
@Component
public final class OrientManager {
	private final Orient orient;

	/**
	 * Constructor. For internal use only.
	 *
	 * @param orient
	 */
	OrientManager(final Orient orient) {
		this.orient = orient;
	}

	/**
	 * Constructor.
	 */
	public OrientManager() {
		this(Orient.instance());
	}

	/**
	 * Returns if the underlying {@link Orient} instance is active.
	 *
	 * @return the underlying {@link Orient} instance is active.
	 */
	public boolean isActive() {
		return orient.isActive();
	}

	/**
	 * Shutdowns the underlying {@link Orient} instance.
	 */
	public void shutdown() {
		orient.shutdown();
	}
}
