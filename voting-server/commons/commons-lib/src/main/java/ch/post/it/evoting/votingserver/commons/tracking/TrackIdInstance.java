/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.tracking;

import javax.enterprise.context.RequestScoped;

import org.slf4j.MDC;

/**
 * The Class TrackIdInstance for setting or getting trackId in a request.
 */
@RequestScoped
public class TrackIdInstance {

	private String trackId;

	/**
	 * Gets the track id.
	 *
	 * @return the track id.
	 */
	public String getTrackId() {
		return trackId;
	}

	/**
	 * Sets the track id.
	 *
	 * @param trackId the new track id.
	 */
	public void setTrackId(final String trackId) {
		this.trackId = trackId;
		MDC.put("trackId", trackId);
	}
}
