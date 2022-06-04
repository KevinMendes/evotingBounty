/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.messaging;

import ch.post.it.evoting.domain.returncodes.CCPublicKey;
import ch.post.it.evoting.domain.returncodes.KeyCreationDTO;

/**
 * Classes that are supported by MsgPack serialization/deserialization.
 */
public enum StreamSerializableClassType {

	KEY_CREATION_DTO(KeyCreationDTO.class.getName()),
	CC_PUBLIC_KEY(CCPublicKey.class.getName());

	private final String className;

	StreamSerializableClassType(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}
}
