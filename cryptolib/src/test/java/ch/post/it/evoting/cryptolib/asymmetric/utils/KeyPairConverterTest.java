/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;

class KeyPairConverterTest {

	private static final String EMPTY_STRING = "";
	private static KeyPairConverter target;
	private static KeyPair keyPairForEncryption;
	private static KeyPair keyPairForSigning;

	private final String publicKeyForEncryptionPEM = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA53It4Iqf"
			+ "/5vd3EDjfFa3yIA2pxvBYE6sSPhQUHoxNXZFmhhj9RCbknUekEE/WdgsC/+5zti1r7pubLmuwtIbkHyHPIVg5e/Xu/hoRtmbTZHWAELDjvRqsD3prNba+t"
			+ "+W0piYA99Wn4Fb1M/Uo6zyqTBgK3Rejtp2bE1w2s/Lzuk"
			+ "/GzAKIXzmZk589YMl0hm8JxN2JprPemK0LVXogijR4AmCgzT9fPpZx1QNIzmFisQzYc3EpnlnQ7mRxMjr60laUM5s+G00lCfnc2a8ZKOYuJPJjO"
			+ "/8hnNfaEDSNbOto3K92RtE2O+xjhzo1oELTovnPjJKinbZA2deojtSjiaJxQIDAQAB-----END PUBLIC KEY-----";

	private final String privateKeyForEncryptionPEM = "-----BEGIN RSA PRIVATE "
			+ "KEY-----MIIEpAIBAAKCAQEA53It4Iqf/5vd3EDjfFa3yIA2pxvBYE6sSPhQUHoxNXZFmhhj9RCbknUekEE/WdgsC/+5zti1r7pubLmuwtIbkHyHPIVg5e/Xu"
			+ "/hoRtmbTZHWAELDjvRqsD3prNba+t+W0piYA99Wn4Fb1M/Uo6zyqTBgK3Rejtp2bE1w2s/Lzuk"
			+ "/GzAKIXzmZk589YMl0hm8JxN2JprPemK0LVXogijR4AmCgzT9fPpZx1QNIzmFisQzYc3EpnlnQ7mRxMjr60laUM5s+G00lCfnc2a8ZKOYuJPJjO"
			+ "/8hnNfaEDSNbOto3K92RtE2O+xjhzo1oELTovnPjJKinbZA2deojtSjiaJxQIDAQABAoIBAQCcmi1gmWvZUGW1"
			+ "+lHyd9qy184jFCysNY9tcFcnnQZe3kAKHCbGUw5w8r5TbVKoQBTNqaLXytpkpQjCmIEfYXs1MI1w7e66pqaakWI9TlA"
			+ "/FEZwtrwLpmXqCnpqcJaK2W774DQ7qoq6MpUoUdfXR9aJlCn+PSceEcO/VEbgR2nn/bEpRycgX2ueJ4HgLt25Vlkirpf2O5IwUkTdWcSTRhOy40sPubOnLupUWJGyDOaa8Xd"
			+ "+bn0KS1jH18PMo3C0qvAohA0BtcwpLX5ac/oFCfmk8/IwPXwYhePwxNLLfBgG0lfLQl629gE+Avkq57G8vRBYcOD6kwdonDIRxFs+PmQBAoGBAPnWWWU2V"
			+ "+qeYLMmBkJLV9GwRNSNdUvBn4jedApV2bBNKw5PY31B6kvC/EoB10FYIYQdat9dSOWncRbzX1jAXvcUqNn0RbLy/B3D4"
			+ "/4vV39UeVWtK8OgLkxbBPSVFGwK3Nivuy0rcG2p0hT4yaxUGj1H2Y1hyrI9fAzPPAkL8ILhAoGBAO0nsb2Vb0VpsddAfyn01mhPgXzyNz81NBsxJb9ubgjQNSdhX"
			+ "/XPn36kTwtwpPPuJMWe9yxzlaHf/HxFP0kKhPBaviK1cKDm/gWWR9UEdRKEB9hkr+BIfhYbi"
			+ "+3KSWzmcE8fV0KHdcvV28JINmW95zhRI1dw4bbBR6xD1bE5RsdlAoGAaD9KmfLtCFcBnn8VSYBKqpJUhiRody3ZtbCs1ssvxGLOvm"
			+ "/d4ZwpeWdpAjB2cyulAI2N1JoGGt2dUKhIdq3"
			+ "+cjbKpfdJRfwhuwHMFnoGlnjXECrsAfrKls276ZpzJQn7UOcywQxJI1ki8eFFtYR6VmuumVHe1DTXmDi4okW7G8ECgYEAiQ1xiHB9t42XeyAI3URjTDD2UjDggKTMkhJbEEBPUsSQk0uQ20u7jsKB88iLa3Tqx1JQ4d2CUeRR07dpFVsA7K5kR0a36iTUFIJ+zLogtiybJBE8Gs+KHliZCzjmKgsaSH+CPC5wgvX6ZFK7LR0MLN2nIPdZWfZk50bkjeDd6IkCgYA7yqQhMsen+PWjf2MPh4tTml3zE1MHEPw0JeLnWaheviGFXtaX6U1eLjS/2EIPQShR+YgMeuk6+C31YiUuCKfvVzMtc1L0DQCyqIPE3B1QLnijH8nZvDnJXBZWyVZE57WRnSaa7JshRMC8llkL0Z+jZlDc7C+qgjYfgiDUIpd/eQ==-----END RSA PRIVATE KEY-----";

	@BeforeAll
	static void setUp() {

		target = new KeyPairConverter();

		final AsymmetricService asymmetricService = new AsymmetricService();

		keyPairForEncryption = asymmetricService.getKeyPairForEncryption();

		keyPairForSigning = asymmetricService.getKeyPairForSigning();
	}

	@Test
	void whenConvertingToPemAPublicKeyForEncrypting() throws GeneralCryptoLibException {
		final String pemStr = target.exportPublicKeyForEncryptingToPem(keyPairForEncryption.getPublic());

		assertNotNull(pemStr);
	}

	@Test
	void whenConvertingToPemAPublicKeyForSigning() throws GeneralCryptoLibException {
		final String pemStr = target.exportPublicKeyForSigningToPem(keyPairForSigning.getPublic());

		assertNotNull(pemStr);
	}

	@Test
	void whenConvertingToPemAPrivateKeyForEncrypting() throws GeneralCryptoLibException {
		final String pemStr = target.exportPrivateKeyForEncryptingToPem(keyPairForEncryption.getPrivate());

		assertNotNull(pemStr);
	}

	@Test
	void whenConvertingToPemAPrivateKeyForSigning() throws GeneralCryptoLibException {
		final String pemStr = target.exportPrivateKeyForSigningToPem(keyPairForSigning.getPrivate());

		assertNotNull(pemStr);
	}

	@Test
	void whenConvertingFromPemAPublicKeyForEncrypting() throws GeneralCryptoLibException {
		final PublicKey publicKeyForEncryptingFromPem = target.getPublicKeyForEncryptingFromPem(publicKeyForEncryptionPEM);

		assertNotNull(publicKeyForEncryptingFromPem);
	}

	@Test
	void whenConvertingFromPemAPublicKeyForSigning() throws GeneralCryptoLibException {
		final String publicKeyForSigningPEM = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkpRgUTyTkMlY8nMqfRq/owRz"
				+ "+hZMSk7WV7v3BfXwhD38dovpSCsWluF4udey+57CFCu+sr1xd9RyJBBRhPn2V0sQMBaKnOIG5T6pmB2lb9S0UbRjQ4MWZ"
				+ "+ZQ0oCtpMDSBSmOPE1f8l54NFRXmMpI3e9njh8WQyQkiOWDE7FwmZ4vfLFiJ9Vm3oOhKIfYG4xS2SHnwvnDfOQwFqJaU3PHrk5j"
				+ "+/Fx52OtXH8lQjFux8HJwbpuPsBhAmYxcAbaReAf4HIbXrR/kz28YFyKOfD+2mwWR7OCXgQGajtrvnuAk2ZEcRWwG9Ed9wuyyHZ2IS3F+MZwNJNtYCdFkh0NRVTbvwIDAQAB"
				+ "-----END PUBLIC KEY-----";
		final PublicKey publicKeyForSigningFromPem = target.getPublicKeyForSigningFromPem(publicKeyForSigningPEM);

		assertNotNull(publicKeyForSigningFromPem);
	}

	@Test
	void whenConvertingFromPemAPrivateKeyForSigning() throws GeneralCryptoLibException {
		final String privateKeyForSigningPEM = "-----BEGIN RSA PRIVATE KEY-----MIIEogIBAAKCAQEAkpRgUTyTkMlY8nMqfRq/owRz"
				+ "+hZMSk7WV7v3BfXwhD38dovpSCsWluF4udey+57CFCu+sr1xd9RyJBBRhPn2V0sQMBaKnOIG5T6pmB2lb9S0UbRjQ4MWZ"
				+ "+ZQ0oCtpMDSBSmOPE1f8l54NFRXmMpI3e9njh8WQyQkiOWDE7FwmZ4vfLFiJ9Vm3oOhKIfYG4xS2SHnwvnDfOQwFqJaU3PHrk5j"
				+ "+/Fx52OtXH8lQjFux8HJwbpuPsBhAmYxcAbaReAf4HIbXrR/kz28YFyKOfD+2mwWR7OCXgQGajtrvnuAk2ZEcRWwG9Ed9wuyyHZ2IS3F"
				+ "+MZwNJNtYCdFkh0NRVTbvwIDAQABAoIBACIkP18Mrg5+Z6JpYs8rCNIOunjY8sIoJNLgrEQOKgyTZ5exNRAjQSq5r+YRsNXc/7W1DsERUTmpGUD/MA26Lz"
				+ "/b8xRQW8748sQ42UXeOeeXcrzJOInEIdbnlZHCeS+z3fdn24J+P0fz6ym9L34eN"
				+ "+TB26wF36iZ5YyhcOuIXALbsvceNh9ibGAtbTmpz85vA4mgEz59Wd4AadVPmGpg24vR9ipAtFRDjpTSlPV17VffwXoQnKh7UgiGb7C4gQhhxO44QQhxQiwZJcxZtqmg"
				+ "+FnIz1p5ojgJXQXNRBLJoHL36gMAoZAJQZV0CDlpA5qLAO"
				+ "/To2EG7YFIKVNbc3PWmgECgYEA5nFmNBmi08dNKQYQY0wGt2GjK256m4Ba4ctRAh0SVrmzrmRNkho8wND0dv76YZw20gtIjaF0QUl0bT0taSU2hL7CgGK5hWwCeR2VlL"
				+ "/gco11wSy0Yr9M0LaHbBI0E5d8p5q5YpMu9rxLHkunrtQZ1v7G90b+8fr3HkybzbrEDr8CgYEAotX6Roe9fNweAuQuR3nlMvFn0A8xBFfuJiI9aL"
				+ "+br6HABhHhtrKbPvthdnd9gdQIhYr5eHxLKMw9Pw5Nrn4LGpHpCi1Uq/Jqikq3v3htY6dAVn4cG54NRkBm37DhDid+Ia1QCMORTizbJlUKh"
				+ "/vIFvoOLTLWKacihpGlbNVPcwECgYAmiMntZmgK/+XQOVpeMGVjHumZJqVMAX+xCD2om9HoPK9mNxGdn9OO7qIij+qwjd3A6/Aa9ueozy6v4a4JSha+xeNicp"
				+ "/RRsl3FVJhCGDw5uTNA7u6U4D12b/1e2nH6OoIYOAlMrWEvuHU1BPbMU6M3BfLW8zgC/Zs06SuF1AXowKBgG"
				+ "+CuXGzY0AuPR8dw9OO1jacusrwnFCtJplWuVcSYDtWOXnupPPIPChVXdkS2xAdc/h/w3ePJqk5tHeKBAiKqG/5m+0sPd"
				+ "/CktNBd7PXdJHZ52VT10vSYKTLVae6KWTYBrhpR3W497jhXvJRKnUVJ2EUvMCXH7JGko41D2QIb"
				+ "/ABAoGAEjzTQoyUgRghH2iT1hHucABmnGmX9BVdwSY3yWx5ze8FVahVvXfKeAm2tkMUMsqDqrwkRJE6PZ/rxKngN/06"
				+ "/bV8vyqhAUkGfmv7wkoIt3aU2fQnGdAiAUgwOSiOO9micgB6HXpNm4zQZJF2ZAALOOWSAzaZRoHmWxJpwF0feVw=-----END RSA PRIVATE KEY-----";
		final PrivateKey privateKeyForSigningFromPem = target.getPrivateKeyForSigningFromPem(privateKeyForSigningPEM);

		assertNotNull(privateKeyForSigningFromPem);
	}

	@Test
	void whenConvertingFromPemAPrivateKeyForEncrypting() throws GeneralCryptoLibException {
		final PrivateKey privateKeyForEncryptingFromPem = target.getPrivateKeyForEncryptingFromPem(privateKeyForEncryptionPEM);

		assertNotNull(privateKeyForEncryptingFromPem);
	}

	@Test
	void whenConvertingFromPemAPrivateKeyForEncryptingThenConvertingToPemAgain() throws GeneralCryptoLibException {
		final PrivateKey privateKeyForEncryptingFromPem = target.getPrivateKeyForEncryptingFromPem(privateKeyForEncryptionPEM);

		assertNotNull(privateKeyForEncryptingFromPem);

		String pemStr = target.exportPrivateKeyForEncryptingToPem(privateKeyForEncryptingFromPem);

		pemStr = pemStr.replaceAll("[\n\r]", "");

		assertEquals(pemStr, privateKeyForEncryptionPEM);
	}

	@Test
	void whenConvertingFromPemAPublicKeyForEncryptingThenConvertingToPemAgain() throws GeneralCryptoLibException {
		final PublicKey publicKeyForEncryptingFromPem = target.getPublicKeyForEncryptingFromPem(publicKeyForEncryptionPEM);

		assertNotNull(publicKeyForEncryptingFromPem);

		String pemStr = target.exportPublicKeyForEncryptingToPem(publicKeyForEncryptingFromPem);

		pemStr = pemStr.replaceAll("[\n\r]", "");

		assertEquals(pemStr, publicKeyForEncryptionPEM);
	}

	@Test
	void whenConvertingToPemANulPublicKeyForEncrypting() {
		assertThrows(GeneralCryptoLibException.class, () -> target.exportPublicKeyForEncryptingToPem(null));
	}

	@Test
	void whenConvertingToPemANullPublicKeyForSigning() {
		assertThrows(GeneralCryptoLibException.class, () -> target.exportPublicKeyForSigningToPem(null));
	}

	@Test
	void whenConvertingToPemANullPrivateKeyForEncrypting() {
		assertThrows(GeneralCryptoLibException.class, () -> target.exportPrivateKeyForEncryptingToPem(null));
	}

	@Test
	void whenConvertingToPemANullPrivateKeyForSigning() {
		assertThrows(GeneralCryptoLibException.class, () -> target.exportPrivateKeyForSigningToPem(null));
	}

	@Test
	void whenConvertingFromNullPemAPublicKeyForSigning() {
		assertThrows(GeneralCryptoLibException.class, () -> target.getPublicKeyForSigningFromPem(null));
	}

	@Test
	void whenConvertingFromNullPemAPrivateKeyForSigning() {
		assertThrows(GeneralCryptoLibException.class, () -> target.getPrivateKeyForSigningFromPem(null));
	}

	@Test
	void whenConvertingFromNullPemAPublicKeyForEncryption() {
		assertThrows(GeneralCryptoLibException.class, () -> target.getPublicKeyForEncryptingFromPem(null));
	}

	@Test
	void whenConvertingFromNullPemAPrivateKeyForEncryption() {
		assertThrows(GeneralCryptoLibException.class, () -> target.getPrivateKeyForEncryptingFromPem(null));
	}

	@Test
	void whenConvertingFromEmptyPemAPublicKeyForSigning() {
		assertThrows(GeneralCryptoLibException.class, () -> target.getPublicKeyForSigningFromPem(EMPTY_STRING));
	}

	@Test
	void whenConvertingFromEmptyPemAPrivateKeyForSigning() {
		assertThrows(GeneralCryptoLibException.class, () -> target.getPrivateKeyForSigningFromPem(EMPTY_STRING));
	}

	@Test
	void whenConvertingFromEmptyPemAPublicKeyForEncryption() {
		assertThrows(GeneralCryptoLibException.class, () -> target.getPublicKeyForEncryptingFromPem(EMPTY_STRING));
	}

	@Test
	void whenConvertingFromEmptyPemAPrivateKeyForEncryption() {
		assertThrows(GeneralCryptoLibException.class, () -> target.getPrivateKeyForEncryptingFromPem(EMPTY_STRING));
	}
}
