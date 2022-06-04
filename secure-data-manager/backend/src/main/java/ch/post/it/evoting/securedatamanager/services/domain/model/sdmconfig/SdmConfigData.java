/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model.sdmconfig;

import java.util.HashMap;
import java.util.Map;

public class SdmConfigData {

	Map<String, Object> config = new HashMap<String, Object>();

	public Map<String, Object> getConfig() {
		return config;
	}

	public void setConfig(final Map<String, Object> config) {
		this.config = config;
	}

}
