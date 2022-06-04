/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority.ElectoralAuthorityRepository;

/**
 * The Class ControlComponentKeysAccessorService. It is used to download the control component keys from the secure data manager database and write
 * them to the secure data manager configuration area, to facilitate subsequent verification processes.
 */
@Service
public class ControlComponentKeysAccessorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControlComponentKeysAccessorService.class);

	private final ElectoralAuthorityRepository electoralAuthorityRepository;

	public ControlComponentKeysAccessorService(final ElectoralAuthorityRepository electoralAuthorityRepository) {
		this.electoralAuthorityRepository = electoralAuthorityRepository;
	}

	/**
	 * Downloads the control component mixing keys, for the provided electoral authority, from the secure data manager database.
	 *
	 * @param electoralAuthorityId the electoral authority ID.
	 * @return the JSON array containing the mixing keys.
	 * @throws ResourceNotFoundException if electoral authority cannot be found.
	 */
	public JsonArray downloadMixingKeys(final String electoralAuthorityId) throws ResourceNotFoundException {
		LOGGER.info(
				"Downloading control component mixing keys for electoral authority from the secure data manager database. [electoralAuthorityId: {}}]",
				electoralAuthorityId);

		final String electoralAuthorityJsonStr = electoralAuthorityRepository.find(electoralAuthorityId);

		if (electoralAuthorityJsonStr.isEmpty() || JsonConstants.EMPTY_OBJECT.equals(electoralAuthorityJsonStr)) {
			throw new ResourceNotFoundException(
					String.format("Electoral authority could not be found in the secure data manager database. [electoralAuthorityId: %s]",
							electoralAuthorityId));
		}

		final JsonObject electoralAuthorityJsonObj = JsonUtils.getJsonObject(electoralAuthorityJsonStr);

		final String mixingKeysJsonArrayStr = electoralAuthorityJsonObj.getString(Constants.MIX_DEC_KEY_LABEL);

		return JsonUtils.getJsonArray(mixingKeysJsonArrayStr);
	}

}
