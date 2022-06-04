/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;

public class BaseExtendedKeyStoreTests {

	protected static ZpSubgroup group;

	public static X509Certificate loadX509Certificate(final String fileName) throws GeneralSecurityException, IOException {
		final CertificateFactory cf = CertificateFactory.getInstance("X.509");
		try (final InputStream is = getResourceAsStream(fileName)) {
			return (X509Certificate) cf.generateCertificate(is);
		}
	}

	public static PrivateKey loadPrivateKey(final String fileName) throws GeneralSecurityException, IOException {
		byte[] keyBytes = new byte[1679];
		try (final InputStream is = getResourceAsStream(fileName)) {
			is.read(keyBytes);
		}
		String privateKey = new String(keyBytes, StandardCharsets.UTF_8);
		privateKey = privateKey.replaceAll("(-+BEGIN RSA PRIVATE KEY-+\\r?\\n|-+END RSA PRIVATE KEY-+\\r?\\n?)", "");
		keyBytes = Base64.getMimeDecoder().decode(privateKey);

		// generate private key
		final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(spec);
	}

	public static SecretKey loadSecretKey(final String fileName) throws IOException {
		final byte[] keyBytes = new byte[16];
		try (final InputStream is = getResourceAsStream(fileName)) {
			is.read(keyBytes);
		}
		// generate private key
		return new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
	}

	public static ElGamalPrivateKey loadElGamalPrivateKey(final String fileName) throws IOException, GeneralCryptoLibException {
		final StringBuilder json = new StringBuilder();
		try (final InputStream is = getResourceAsStream(fileName); final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
			int c;
			while ((c = reader.read()) != -1) {
				json.append((char) c);
			}
		}
		return ElGamalPrivateKey.fromJson(json.toString());
	}

	private static InputStream getResourceAsStream(final String name) {
		return BaseExtendedKeyStoreTests.class.getResourceAsStream('/' + name);
	}
}
