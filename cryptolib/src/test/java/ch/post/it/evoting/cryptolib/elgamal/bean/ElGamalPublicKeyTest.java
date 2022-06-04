/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;

class ElGamalPublicKeyTest {

	private static ZpSubgroup smallGroup;
	private static int numKeys;
	private static List<ZpGroupElement> pubKeys;
	private static ElGamalPublicKey elGamalPublicKey;

	@BeforeAll
	static void setUp() throws GeneralCryptoLibException {

		BigInteger p = new BigInteger("23");
		BigInteger q = new BigInteger("11");
		BigInteger g = new BigInteger("2");

		smallGroup = new ZpSubgroup(g, p, q);

		numKeys = 2;

		pubKeys = new ArrayList<>();
		pubKeys.add(new ZpGroupElement(g, smallGroup));
		pubKeys.add(new ZpGroupElement(g, smallGroup));

		elGamalPublicKey = new ElGamalPublicKey(pubKeys, smallGroup);
	}

	@Test
	void givenNullKeysListWhenCreatePublicKeyThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new ElGamalPublicKey(null, smallGroup));
	}

	@Test
	void givenEmptyKeysListWhenCreatePublicKeyThenException() {
		final List<ZpGroupElement> emptyKeysList = new ArrayList<>();
		assertThrows(GeneralCryptoLibException.class, () -> new ElGamalPublicKey(emptyKeysList, smallGroup));
	}

	@Test
	void givenNullGroupWhenCreatePublicKeyThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new ElGamalPublicKey(pubKeys, null));
	}

	@Test
	void givenPublicKeyWhenGetKeysThenOK() {
		final List<ZpGroupElement> returnedKeys = elGamalPublicKey.getKeys();

		String errorMsg = "The created public key does not have the expected number of elements";
		assertEquals(numKeys, returnedKeys.size(), errorMsg);

		errorMsg = "The public does not have the expected list of keys";
		assertEquals(pubKeys, returnedKeys, errorMsg);
	}

	@Test
	void givenPublicKeyWhenGetGroupThenOK() {
		final String errorMsg = "The created public key does not have the expected group";
		assertEquals(smallGroup, elGamalPublicKey.getGroup(), errorMsg);
	}

	@Test
	void givenJsonStringWhenReconstructThenEqualToOriginalPublicKey() throws GeneralCryptoLibException {
		final BigInteger p = new BigInteger("23");
		final BigInteger q = new BigInteger("11");
		final BigInteger g = new BigInteger("2");
		final ZpSubgroup smallGroup = new ZpSubgroup(g, p, q);

		final List<ZpGroupElement> pubKeys = new ArrayList<>();
		pubKeys.add(new ZpGroupElement(g, smallGroup));
		pubKeys.add(new ZpGroupElement(g, smallGroup));
		final ElGamalPublicKey expectedElGamalPublicKey = new ElGamalPublicKey(pubKeys, smallGroup);

		final String jsonStr = elGamalPublicKey.toJson();

		final ElGamalPublicKey reconstructedPublicKey = ElGamalPublicKey.fromJson(jsonStr);

		final String errorMsg = "The reconstructed ElGamal public key is not equal to the expected key";
		assertEquals(expectedElGamalPublicKey, reconstructedPublicKey, errorMsg);
	}

	@Test
	void givenJsonStringWhenReconstructKeyThenDifferentFromOtherPublicKey() throws GeneralCryptoLibException {
		final BigInteger p = new BigInteger("7");
		final BigInteger q = new BigInteger("3");
		final BigInteger g = new BigInteger("2");
		final ZpSubgroup smallGroup = new ZpSubgroup(g, p, q);
		final List<ZpGroupElement> pubKeys = new ArrayList<>();
		pubKeys.add(new ZpGroupElement(g, smallGroup));
		pubKeys.add(new ZpGroupElement(g, smallGroup));
		final ElGamalPublicKey expectedElGamalPublicKey = new ElGamalPublicKey(pubKeys, smallGroup);

		final String jsonStr = elGamalPublicKey.toJson();

		final ElGamalPublicKey reconstructedPublicKey = ElGamalPublicKey.fromJson(jsonStr);

		final String errorMsg = "The reconstructed ElGamal public key was unexpectedly equal to";
		assertNotEquals(reconstructedPublicKey, expectedElGamalPublicKey, errorMsg);
	}

	@Test
	void givenJsonStringWhenReconstructThenEqualToOriginalPublicKeySmall() throws GeneralCryptoLibException {
		final BigInteger p = new BigInteger("23");
		final BigInteger q = new BigInteger("11");
		final BigInteger g = new BigInteger("2");
		final ZpSubgroup groupSmall = new ZpSubgroup(g, p, q);

		final List<ZpGroupElement> pubKeys = new ArrayList<>();
		final BigInteger k1 = new BigInteger("8");
		final BigInteger k2 = new BigInteger("4");
		final BigInteger k3 = new BigInteger("18");
		pubKeys.add(new ZpGroupElement(k1, groupSmall));
		pubKeys.add(new ZpGroupElement(k2, groupSmall));
		pubKeys.add(new ZpGroupElement(k3, groupSmall));
		final ElGamalPublicKey expectedElGamalPublicKey = new ElGamalPublicKey(pubKeys, groupSmall);

		final String jsonStr = expectedElGamalPublicKey.toJson();

		final ElGamalPublicKey reconstructedPublicKey = ElGamalPublicKey.fromJson(jsonStr);

		final String errorMsg = "The reconstructed ElGamal public key is not equal to the expected key";
		assertEquals(expectedElGamalPublicKey, reconstructedPublicKey, errorMsg);
	}

	@Test
	void givenJsonStringWhenReconstructThenEqualToOriginalPublicKeyLarge() throws GeneralCryptoLibException {
		final BigInteger p = new BigInteger(
				"25878792566670842099842137716422866466252991028815773139028451679515364679624923581358662655689289205766441980239548823737806954397019411202244121935752456749381769565031670387914863935577896116425654849306598185507995737892509839616944496073707445338806101425467388977937489020456783676102620561970644684015868766028080049372849872115052208214439472603355483095640041515460851475971118272125133224007949688443680429668091313474118875081620746919907567682398209044343652147328622866834600839878114285018818463110227111614032671442085465843940709084719667865761125514800243342061732684028802646193202210299179139410607");
		final BigInteger q = new BigInteger(
				"12939396283335421049921068858211433233126495514407886569514225839757682339812461790679331327844644602883220990119774411868903477198509705601122060967876228374690884782515835193957431967788948058212827424653299092753997868946254919808472248036853722669403050712733694488968744510228391838051310280985322342007934383014040024686424936057526104107219736301677741547820020757730425737985559136062566612003974844221840214834045656737059437540810373459953783841199104522171826073664311433417300419939057142509409231555113555807016335721042732921970354542359833932880562757400121671030866342014401323096601105149589569705303");
		final BigInteger g = new BigInteger(
				"23337993065784550228812110720552652305178266477392633588884900695706615523553977368516877521940228584865573144621632575456086035440118913707895716109366641541746808409917179478292952139273396531060021729985473121368590574110220870149822495151519706210399569901298027813383104891697930149341258267962490850297875794622068418425473578455187344232698462829084010585324877420343904740081787639502967515631687068869545665294697583750184911025514712871193837246483893950501015755683415509019863976071649325968623617568219864744389709563087949389080252971419711636380986100047871404548371112472694814597772988558887480308242");
		final ZpSubgroup groupLarge = new ZpSubgroup(g, p, q);

		final List<ZpGroupElement> pubKeys = new ArrayList<>();
		final BigInteger k1 = new BigInteger(
				"10813600900774814195443606333960059840422668831651681762833741452209135730650864452130214650095752196571587702814444040062441118243327189166429884552159103387262050740332278254634882734924804644475224947872860032029859548750165768253226371255576644038692950180048665862716256869418687777905840129397057458233193867066348913299433809742746548665012143326171826247072111249488645241753486115635552758795831876906471529661551590917603438603247246774800973230633192834223627007745551660750221242611378094020803537796039757375005102923882805858377750794407562467092342593013192207441450132449006118950054688176587133961409");
		final BigInteger k2 = new BigInteger(
				"9853567379170719777334566185703517962033907422082302290399539440945952704005195929747815492644386838518442489536168205168599971300023125537903954233589099450715318090993081445448000242320602780219383745672665060815409904594899636902384560406070750905212550608478584637229051043378424454988191292610492156611060987968379597746425215082609688334075995471885941390029962061830957304051378886661637882858376301662242976957205952138813983434210148145737465280463387503441953147312554462853944227422544694220254912841114943534241459615181877522308107316310818302531331583848802731174268791963178918947077447910490965216158");
		final BigInteger k3 = new BigInteger(
				"15794379152029066838713804099734041126263932884995939213200727618402701551640677264676158281588744352993384547095094861783499389509798314014022069284171927996645400863301983568835178765405753107568243261485121885250020254379553972077632932375247364025813645736274557285578258334201060356153010774984159753076425595005818465482275931002143043064230447825744365372619735966376047086115776775312766029914867090765966581873109229928865155261673639010273082385667041760650533946369556580260795606883221254081319827267155147350340767060491743854037592935823565514546490962430394933911057965977363230092438984010824700325167");
		pubKeys.add(new ZpGroupElement(k1, groupLarge));
		pubKeys.add(new ZpGroupElement(k2, groupLarge));
		pubKeys.add(new ZpGroupElement(k3, groupLarge));
		final ElGamalPublicKey expectedElGamalPublicKey = new ElGamalPublicKey(pubKeys, groupLarge);

		final String jsonStr = expectedElGamalPublicKey.toJson();

		final ElGamalPublicKey reconstructedPublicKey = ElGamalPublicKey.fromJson(jsonStr);

		final String errorMsg = "The reconstructed ElGamal public key is not equal to the expected key";
		assertEquals(expectedElGamalPublicKey, reconstructedPublicKey, errorMsg);
	}
}
