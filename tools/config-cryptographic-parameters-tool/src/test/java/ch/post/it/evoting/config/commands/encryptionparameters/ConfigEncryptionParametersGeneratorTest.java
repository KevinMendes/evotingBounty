/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.encryptionparameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.VerifiableElGamalEncryptionParameters;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;

@DisplayName("A ConfigEncryptionParametersGenerator")
class ConfigEncryptionParametersGeneratorTest {

	private static final BigInteger expectedP = new BigInteger(
			"FA5F0594BD256783E05BAB37A84C51903EE7D37F40ACBD0B8D872E06B5EA9BCE03E3A1FCF84DE84EB95FA189552CB588E89DD0D15436C0E2CBCC6909D769000640219F7C251307C58522E019BAA23F7D06A5FDFDFDD0245A9F2482588935AE7FD7E0C2C36AD756045E0A956B066F7586A18C0007FB0AE2E19E90FB8F3CA6BD63E776646322C8D127DC056E7A8BC733CF3D5E03ECB5005F91858CA5126939804340B31C4341BDF42FDD2916A9A0A4E9A9213A8ADBD6AAE1287F6D925F7B1EEC34E753AD12D0A9F9E8E68230A026CFC891D62EFBB2056D13C83B91E0F4C588A0DED40E0BB82684347A1DEAF392664ECAD1B1FE62D009E7D80A1BC35F884BA66543",
			16);
	private static final BigInteger expectedQ = new BigInteger(
			"7D2F82CA5E92B3C1F02DD59BD42628C81F73E9BFA0565E85C6C397035AF54DE701F1D0FE7C26F4275CAFD0C4AA965AC4744EE868AA1B607165E63484EBB480032010CFBE128983E2C291700CDD511FBE8352FEFEFEE8122D4F92412C449AD73FEBF06161B56BAB022F054AB58337BAC350C60003FD857170CF487DC79E535EB1F3BB323191646893EE02B73D45E399E79EAF01F65A802FC8C2C65289349CC021A0598E21A0DEFA17EE948B54D05274D4909D456DEB5570943FB6C92FBD8F761A73A9D6896854FCF4734118501367E448EB177DD902B689E41DC8F07A62C4506F6A0705DC13421A3D0EF579C933276568D8FF316804F3EC050DE1AFC425D332A1",
			16);
	private static final BigInteger expectedG = new BigInteger("3", 16);

	private static VerifiableElGamalEncryptionParameters expectedParameters;
	private static ConfigEncryptionParametersGenerator configEncryptionParametersGenerator;

	@BeforeAll
	static void setUpAll() throws GeneralCryptoLibException {
		configEncryptionParametersGenerator = new ConfigEncryptionParametersGenerator(new ElGamalService());
		expectedParameters = new VerifiableElGamalEncryptionParameters(expectedP, expectedQ, expectedG, "4106");
	}

	@Test
	@DisplayName("calling generate with null seed path throws NullPointerException")
	void generateWithNullSeedPath() {
		assertThrows(NullPointerException.class, () -> configEncryptionParametersGenerator.generate(null));
	}

	@Test
	@DisplayName("calling generate with empty seed throws IllegalArgumentException")
	void generateWithEmptySeed() throws URISyntaxException {
		final Path seedPath = Paths.get(this.getClass().getResource("/seed_empty.txt").toURI());

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> configEncryptionParametersGenerator.generate(seedPath));
		assertEquals("The seed must not be an empty string.", exception.getMessage());
	}

	@Test
	@DisplayName("calling generate with invalid seed path throws UncheckedIOException")
	void generateWithInvalidSeedPath() {
		final Path invalidSeedPath = Paths.get("/invalid/path/seed.txt");

		final UncheckedIOException exception = assertThrows(UncheckedIOException.class,
				() -> configEncryptionParametersGenerator.generate(invalidSeedPath));
		assertEquals(String.format("Failed to read seed located at: %s", invalidSeedPath), exception.getMessage());
	}

	@ParameterizedTest
	@DisplayName("calling generate with valid seeds gives expected results")
	@Timeout(30)
	@ValueSource(strings = { "/seed.txt", "/seed_with_crlf.txt", "/seed_with_leading_whitespace.txt", "/seed_with_trailing_whitespace.txt" })
	void generateWithValidSeeds(final String seedFileResource) throws URISyntaxException {
		final Path seedPath = Paths.get(this.getClass().getResource(seedFileResource).toURI());

		final VerifiableElGamalEncryptionParameters encryptionParameters = configEncryptionParametersGenerator.generate(seedPath);

		assertEquals(expectedParameters, encryptionParameters);
	}
}
