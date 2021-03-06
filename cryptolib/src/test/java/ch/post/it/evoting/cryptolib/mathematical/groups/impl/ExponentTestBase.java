/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.impl;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeAll;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.mathematical.groups.MathematicalGroup;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.CryptoRandomInteger;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.SecureRandomFactory;

public abstract class ExponentTestBase {

	protected static BigInteger _smallP;

	protected static BigInteger _largeP;

	protected static BigInteger _smallQ;

	protected static BigInteger _largeQ;

	protected static BigInteger _smallG;

	protected static BigInteger _largeG;

	protected static MathematicalGroup<?> _smallGroup;

	protected static MathematicalGroup<?> _largeGroup;

	protected static CryptoRandomInteger _cryptoRandomInteger;

	@BeforeAll
	public static void setUp() throws GeneralCryptoLibException {

		_smallP = new BigInteger("23");

		_smallQ = new BigInteger("11");

		_smallG = new BigInteger("2");

		_smallGroup = new ZpSubgroup(_smallG, _smallP, _smallQ);

		_largeP = new BigInteger(
				"25878792566670842099842137716422866466252991028815773139028451679515364679624923581358662655689289205766441980239548823737806954397019411202244121935752456749381769565031670387914863935577896116425654849306598185507995737892509839616944496073707445338806101425467388977937489020456783676102620561970644684015868766028080049372849872115052208214439472603355483095640041515460851475971118272125133224007949688443680429668091313474118875081620746919907567682398209044343652147328622866834600839878114285018818463110227111614032671442085465843940709084719667865761125514800243342061732684028802646193202210299179139410607");

		_largeQ = new BigInteger(
				"12939396283335421049921068858211433233126495514407886569514225839757682339812461790679331327844644602883220990119774411868903477198509705601122060967876228374690884782515835193957431967788948058212827424653299092753997868946254919808472248036853722669403050712733694488968744510228391838051310280985322342007934383014040024686424936057526104107219736301677741547820020757730425737985559136062566612003974844221840214834045656737059437540810373459953783841199104522171826073664311433417300419939057142509409231555113555807016335721042732921970354542359833932880562757400121671030866342014401323096601105149589569705303");

		_largeG = new BigInteger(
				"23337993065784550228812110720552652305178266477392633588884900695706615523553977368516877521940228584865573144621632575456086035440118913707895716109366641541746808409917179478292952139273396531060021729985473121368590574110220870149822495151519706210399569901298027813383104891697930149341258267962490850297875794622068418425473578455187344232698462829084010585324877420343904740081787639502967515631687068869545665294697583750184911025514712871193837246483893950501015755683415509019863976071649325968623617568219864744389709563087949389080252971419711636380986100047871404548371112472694814597772988558887480308242");

		_largeGroup = new ZpSubgroup(_largeG, _largeP, _largeQ);

		final SecureRandomPolicy secureRandomPolicy = getSecureRandomPolicy();
		_cryptoRandomInteger = new SecureRandomFactory(secureRandomPolicy).createIntegerRandom();
	}

	protected static SecureRandomPolicy getSecureRandomPolicy() {

		return () -> {
			switch (OperatingSystem.current()) {
			case WINDOWS:
				return ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
			case UNIX:
				return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
			default:
				throw new CryptoLibException("OS not supported");
			}
		};
	}
}
