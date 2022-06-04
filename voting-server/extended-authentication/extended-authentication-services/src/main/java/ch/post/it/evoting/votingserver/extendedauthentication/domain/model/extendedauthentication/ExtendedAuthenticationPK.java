/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication;

import java.io.Serializable;
import java.util.Objects;

public class ExtendedAuthenticationPK implements Serializable {

	private static final long serialVersionUID = 4915015458000887699L;

	private String authId;

	private String tenantId;

	private String electionEvent;

	public ExtendedAuthenticationPK() {
		super();
	}

	public ExtendedAuthenticationPK(String authId, String tenantId, String electionEvent) {
		super();
		this.authId = authId;
		this.tenantId = tenantId;
		this.electionEvent = electionEvent;
	}

	@Override
	public int hashCode() {
		return Objects.hash(electionEvent, tenantId, authId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ExtendedAuthenticationPK)) {
			return false;
		}
		ExtendedAuthenticationPK other = (ExtendedAuthenticationPK) obj;
		if (electionEvent == null) {
			if (other.electionEvent != null) {
				return false;
			}
		} else if (!electionEvent.equals(other.electionEvent)) {
			return false;
		}
		if (tenantId == null) {
			if (other.tenantId != null) {
				return false;
			}
		} else if (!tenantId.equals(other.tenantId)) {
			return false;
		}
		if (authId == null) {
			return other.authId == null;
		} else {
			return authId.equals(other.authId);
		}
	}

}
