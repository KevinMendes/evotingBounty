/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.service;

import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.commons.concurrent.BaseFactory;

/**
 * Factory of {@link CertificatesService} objects which are non thread-safe.
 */
public class BasicCertificatesServiceFactory extends BaseFactory<CertificatesServiceAPI> {
	/**
	 * Default constructor. This factory will create services configured with default values.
	 */
	public BasicCertificatesServiceFactory() {
		super(CertificatesServiceAPI.class);
	}

	@Override
	public CertificatesServiceAPI create() {
		return new CertificatesService();
	}
}
