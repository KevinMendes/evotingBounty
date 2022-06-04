/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.ws.application.operation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationResult;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.domain.election.model.platform.PlatformInstallationData;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationService;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCARepository;

public class EaPlatformDataResourceTest {
	private final String platform = "-----BEGIN CERTIFICATE-----" + "MIIDYjCCAkqgAwIBAgIVAK1iv49QAkCz0vpq/cfHtrFdPv9BMA0GCSqGSIb3DQEB"
			+ "CwUAMFgxFjAUBgNVBAMMDVNjeXRsIFJvb3QgQ0ExFjAUBgNVBAsMDU9ubGluZSBW" + "b3RpbmcxDjAMBgNVBAoMBVNjeXRsMQkwBwYDVQQHDAAxCzAJBgNVBAYTAkVTMB4X"
			+ "DTE2MTAyMDAwMDAwMFoXDTE4MTAyMDIzMDAwMFowWDEWMBQGA1UEAwwNU2N5dGwg" + "Um9vdCBDQTEWMBQGA1UECwwNT25saW5lIFZvdGluZzEOMAwGA1UECgwFU2N5dGwx"
			+ "CTAHBgNVBAcMADELMAkGA1UEBhMCRVMwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw" + "ggEKAoIBAQDKit/euH0XFMSot/CEDW9MN0Uoxxo6lKFb6gtyraQUoS1hSawXTRyr"
			+ "sMzjEbnZQY1lPWgFAZCMzSVK7RZVetiqng3uTmWFll5/XtyxR+P4GD0Q6OW0icFr" + "oDAQ4AD/YMEVX7T/dWsnh1j/NIg1sXsTLjNZyswXiXlhe71wrEFb61/NtxFu//rn"
			+ "ZtPL/oQn/MLmXtrQZVPEQ1KDhPpAbvruNyqkThWyNvoEf3pUMeYuwMwNqcgPiOj3" + "1z9orIeMCnTagquJc3KbEE2/WFPaWmETl4w+gpUcafxmFvsSXijvmaMW86sc+GT2"
			+ "L5NjFpewSXG0vghg7f167tC9uIZBQ2rhAgMBAAGjIzAhMA8GA1UdEwEB/wQFMAMB" + "Af8wDgYDVR0PAQH/BAQDAgEGMA0GCSqGSIb3DQEBCwUAA4IBAQAEU1EcIPEUQOf8"
			+ "Do0Z+R3JwRo6YmknCbATSDNmjGHLLwsaJn0SnI+fQXLdw1eE8XR3WnmF2Oi0ZDO+" + "6wFo+cZYFHtaCoO4TqpJjyJt9lDXOGv0+tTkmHRZdGfceVhbg1aX3tEL5a3Nkyt5"
			+ "dolPZmtMqLVUsxOQOU+/q6S9cRaSeSukEc/xgydeOAJ3LBrAGCGUXMNxqf/g7u8n" + "jQ3QEOydTxfZh7UKndufnFSzP8RlPYAJsoknwmsl/SgSCNcaGNJoTBAUKw30xATe"
			+ "NzP7MQZ+wvdix18dshSyCemV5cdTNT4ZpEh8DUuc80KrCKOIHSeydpUUe2Khvdb8" + "cD7sazdu" + "-----END CERTIFICATE-----";
	private EaPlatformDataResource resource;

	@Before
	public void setUp() {
		resource = new EaPlatformDataResource();
		resource.certificateValidationService = mock(CertificateValidationService.class);
		resource.platformRepository = mock(PlatformCARepository.class);
	}

	@Test
	public void testSuccessfulResponse() throws CryptographicOperationException {

		PlatformInstallationData data = new PlatformInstallationData();
		data.setPlatformRootCaPEM(platform);
		data.setPlatformRootIssuerCaPEM("");
		X509CertificateValidationResult valid = new X509CertificateValidationResult(true);
		when(resource.certificateValidationService.validateRootCertificate(any())).thenReturn(valid);
		Response savePlatformData = resource.savePlatformData(data);
		Assert.assertEquals(Status.OK.getStatusCode(), savePlatformData.getStatus());
	}

}
