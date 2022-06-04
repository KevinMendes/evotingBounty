/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.returncode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.cryptolib.returncode.constants.VoterCodesConstants;
import ch.post.it.evoting.cryptolib.symmetric.service.SymmetricService;

class VoterCodesServiceImplTest {

	private static VoterCodesServiceImpl voterCodesServiceImpl;
	private static String electionEventId;
	private static String verificationCardId;

	static Stream<Arguments> getInvalidIdsCombinations() throws GeneralCryptoLibException {
		final String invalidId = "1234";

		final List<String> invalidCorrectnessIds = new ArrayList<>();
		invalidCorrectnessIds.add(invalidId);

		ZpGroupElement preReturnCode = new ZpGroupElement(new BigInteger("2"), new BigInteger("23"), new BigInteger("11"));

		return Stream.of(arguments(invalidId, verificationCardId, preReturnCode, Collections.emptyList()),
				arguments(electionEventId, invalidId, preReturnCode, Collections.emptyList()),
				arguments(electionEventId, verificationCardId, preReturnCode, invalidCorrectnessIds));
	}

	@BeforeAll
	static void setUp() {
		SymmetricServiceAPI symmetricService = new SymmetricService();

		PrimitivesServiceAPI primitivesService = new PrimitivesService();

		voterCodesServiceImpl = new VoterCodesServiceImpl(primitivesService, symmetricService);

		electionEventId = generateID();
		verificationCardId = generateID();
	}

	private static String generateID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	@RepeatedTest(1000)
	void generateValidVoteCastReturnCode() {
		final String voteCastReturnCode = voterCodesServiceImpl.generateShortVoteCastReturnCode();

		final String errorMsg = String
				.format("The generated vote cast return code did not have %s digits, it was: %s", VoterCodesConstants.NUM_DIGITS_VOTE_CASTING_CODE,
						voteCastReturnCode);

		assertEquals(VoterCodesConstants.NUM_DIGITS_VOTE_CASTING_CODE, voteCastReturnCode.length(), errorMsg);
	}

	@RepeatedTest(1000)
	void generateValidShortChoiceReturnCode() {

		final String shortChoiceReturnCode = voterCodesServiceImpl.generateShortChoiceReturnCode();

		final String errorMsg = String
				.format("The generated short choice return code did not have %s digits, it was: %s", VoterCodesConstants.NUM_DIGITS_SHORT_CHOICE_CODE,
						shortChoiceReturnCode);

		assertEquals(4, shortChoiceReturnCode.length(), errorMsg);
	}

	@Test
	void generateValidLongReturnCode() throws GeneralCryptoLibException {
		ZpGroupElement preReturnCode = new ZpGroupElement(new BigInteger("2"), new BigInteger("23"), new BigInteger("11"));
		byte[] longReturnCode = voterCodesServiceImpl
				.generateLongReturnCode(electionEventId, verificationCardId, preReturnCode, Collections.emptyList());

		assertNotNull(longReturnCode);
	}

	@ParameterizedTest
	@MethodSource("getInvalidIdsCombinations")
	void generateLongReturnCodeWithInvalidElectionEventId(String electionEventIdArg, String verificationCardIdArg, ZpGroupElement preReturnCodeArg,
			List<String> correctnessIdsArg) {

		assertThrows(IllegalArgumentException.class,
				() -> voterCodesServiceImpl.generateLongReturnCode(electionEventIdArg, verificationCardIdArg, preReturnCodeArg, correctnessIdsArg));
	}

	// Once we use Guava preconditions, one can merge the two next tests in the Parameterized test "getInvalidIdsCombinations".
	@Test
	void generateLongReturnCodeWithNullPreReturnCode() {
		final List<String> correctnessIds = new ArrayList<>();
		correctnessIds.add("1234");

		assertThrows(NullPointerException.class,
				() -> voterCodesServiceImpl.generateLongReturnCode(electionEventId, verificationCardId, null, correctnessIds));
	}

	@Test
	void generateLongReturnCodeWithNullCorrectnessIds() throws Exception {
		ZpGroupElement preReturnCode = new ZpGroupElement(new BigInteger("2"), new BigInteger("23"), new BigInteger("11"));

		assertThrows(NullPointerException.class,
				() -> voterCodesServiceImpl.generateLongReturnCode(electionEventId, verificationCardId, preReturnCode, null));
	}

	@Test
	void generateMapping() throws GeneralCryptoLibException {
		byte[] mData = "01234567".getBytes(StandardCharsets.UTF_8);

		ZpGroupElement preReturnCode = new ZpGroupElement(new BigInteger("2"), new BigInteger("23"), new BigInteger("11"));
		byte[] longReturnCode = voterCodesServiceImpl
				.generateLongReturnCode(electionEventId, verificationCardId, preReturnCode, Collections.emptyList());

		CodesMappingTableEntry codesMappingTableEntry = voterCodesServiceImpl.generateCodesMappingTableEntry(mData, longReturnCode);

		assertNotNull(codesMappingTableEntry);
	}

}
