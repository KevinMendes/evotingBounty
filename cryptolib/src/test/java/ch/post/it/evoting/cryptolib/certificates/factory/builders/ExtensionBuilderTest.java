/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.factory.builders;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.certificates.bean.extensions.BasicConstraintsExtension;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.ExtensionType;

@ExtendWith(MockitoExtension.class)
class ExtensionBuilderTest {

	@Mock
	private X509v3CertificateBuilder certificateBuilder;

	@Mock
	private BasicConstraintsExtension certificateExtension;

	@Test
	void testAddExtension() throws CertIOException {
		when(certificateExtension.getExtensionType()).thenReturn(ExtensionType.BASIC_CONSTRAINTS);
		when(certificateExtension.isCritical()).thenReturn(true);
		when(certificateExtension.isCertificateAuthority()).thenReturn(true);

		certificateBuilder.addExtension(Extension.basicConstraints, certificateExtension.isCritical(),
				new BasicConstraints(certificateExtension.isCertificateAuthority()));

		assertDoesNotThrow(() -> new ExtensionBuilder(certificateBuilder).addExtension(certificateExtension));
	}
}
