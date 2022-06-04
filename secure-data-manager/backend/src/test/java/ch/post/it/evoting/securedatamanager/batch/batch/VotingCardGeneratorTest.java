/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch;

import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIPBKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalKeyPair;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPublicKey;
import ch.post.it.evoting.cryptolib.returncode.CodesMappingTableEntry;
import ch.post.it.evoting.cryptolib.returncode.VoterCodesService;
import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.domain.cryptoadapters.CryptoAdapters;
import ch.post.it.evoting.domain.election.EncryptionParameters;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.commons.domain.CreateVotingCardSetCertificatePropertiesContainer;
import ch.post.it.evoting.securedatamanager.commons.domain.VcIdCombinedReturnCodesGenerationValues;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ExtendedAuthInformation;
import ch.post.it.evoting.securedatamanager.config.engine.actions.ExtendedAuthenticationService;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.JobExecutionObjectContext;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersParametersHolder;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCredentialDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VotingCardCredentialDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VotingCardCredentialInputDataPack;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.generators.VerificationCardCredentialDataPackGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.generators.VotingCardCredentialDataPackGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service.StartVotingKeyService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
class VotingCardGeneratorTest {

	private static final String RESOURCES_FOLDER_NAME = "VotingCardGeneratorTest";
	private static final String BALLOT_JSON = "ballot.json";
	private static final String P = "25515082852221325227734875679796454760326467690112538918409444238866830264288928368643860210692030230970372642053699673880830938513755311613746769767735066124931265104230246714327140720231537205767076779634365989939295710998787785801877310580401262530818848712843191597770750843711630250668056368624192328749556025449493888902777252341817892959006585132698115406972938429732386814317498812002229915393331703423250137659204137625584559844531972832055617091033311878843608854983169553055109029654797488332746885443611918764277292979134833642098989040604523427961162591459163821790507259475762650859921432844527734894939";
	private static final String Q = "12757541426110662613867437839898227380163233845056269459204722119433415132144464184321930105346015115485186321026849836940415469256877655806873384883867533062465632552115123357163570360115768602883538389817182994969647855499393892900938655290200631265409424356421595798885375421855815125334028184312096164374778012724746944451388626170908946479503292566349057703486469214866193407158749406001114957696665851711625068829602068812792279922265986416027808545516655939421804427491584776527554514827398744166373442721805959382138646489567416821049494520302261713980581295729581910895253629737881325429960716422263867447469";
	private static final String G = "3";

	@Autowired
	private VotingCardGenerator votingCardGenerator;

	@Autowired
	private ElGamalGenerator elGamalGenerator;

	@Autowired
	private RandomService randomService;

	@Test
	void generatesVotingCardOutput() throws Exception {

		final VcIdCombinedReturnCodesGenerationValues vcIdCombinedReturnCodesGenerationValuesMock =
				mock(VcIdCombinedReturnCodesGenerationValues.class);

		final GqGroup group = new GqGroup(new BigInteger(P), new BigInteger(Q), new BigInteger(G));
		final GqElement powerOfPrime = GqElementFactory.fromValue(BigInteger.valueOf(4), group);
		final ZqElement r1 = ZqElement.create(randomService.genRandomInteger(group.getQ()), ZqGroup.sameOrderAs(group));
		final ElGamalMultiRecipientPublicKey publicKey = elGamalGenerator.genRandomPublicKey(1);
		final ElGamalMultiRecipientCiphertext preChoiceReturnCodes = ElGamalMultiRecipientCiphertext.getCiphertext(
				new ElGamalMultiRecipientMessage(singletonList(powerOfPrime)), r1, publicKey);
		final GqElement computedBck = GqElementFactory.fromValue(BigInteger.valueOf(4), group);
		final ZqElement r2 = ZqElement.create(randomService.genRandomInteger(group.getQ()), ZqGroup.sameOrderAs(group));
		final ElGamalMultiRecipientCiphertext castReturnCode = ElGamalMultiRecipientCiphertext.getCiphertext(
				new ElGamalMultiRecipientMessage(singletonList(computedBck)), r2, publicKey);

		when(vcIdCombinedReturnCodesGenerationValuesMock.getVerificationCardId()).thenReturn("1");
		when(vcIdCombinedReturnCodesGenerationValuesMock.getEncryptedPreChoiceReturnCodes()).thenReturn(preChoiceReturnCodes);
		when(vcIdCombinedReturnCodesGenerationValuesMock.getEncryptedPreVoteCastReturnCode()).thenReturn(castReturnCode);

		final GeneratedVotingCardOutput output = votingCardGenerator.process(vcIdCombinedReturnCodesGenerationValuesMock);

		assertNotNull(output);
		assertNotNull(output.getBallotBoxId());
		assertNotNull(output.getBallotId());
		assertNotNull(output.getCredentialId());
		assertNotNull(output.getElectionEventId());
		assertNull(output.getError());
		assertNotNull(output.getExtendedAuthInformation());
		assertNotNull(output.getStartVotingKey());
		assertNotNull(output.getVerificationCardCodesDataPack());
		assertNotNull(output.getVerificationCardId());
		assertNotNull(output.getVoterCredentialDataPack());
		assertNotNull(output.getVotingCardId());
		assertNotNull(output.getVotingCardSetId());
		assertEquals(Constants.NUM_DIGITS_BALLOT_CASTING_KEY, output.getVerificationCardCodesDataPack().getBallotCastingKey().length());
	}

	@Test
	void retrieveBallotCastingKeyTest() throws URISyntaxException {

		final Path basePath = Paths.get(VotingCardGeneratorTest.class.getClassLoader().getResource(RESOURCES_FOLDER_NAME).toURI())
				.resolve("electionEventId");

		assertAll(() -> assertEquals("ballotCastingKey1",
						VotingCardGenerator.retrieveBallotCastingKey("verificationCardSetId", "verificationCardId1", basePath)),
				() -> assertEquals("ballotCastingKey2",
						VotingCardGenerator.retrieveBallotCastingKey("verificationCardSetId", "verificationCardId2", basePath)),
				() -> assertEquals("ballotCastingKey3",
						VotingCardGenerator.retrieveBallotCastingKey("verificationCardSetId", "verificationCardId3", basePath)));
	}

	private static Ballot getBallotFromResources() throws IOException {
		return new ObjectMapper().readValue(
				VotingCardGeneratorTest.class.getClassLoader().getResource(RESOURCES_FOLDER_NAME + File.separator + BALLOT_JSON), Ballot.class);
	}

	@Configuration
	static class PrivateConfiguration {

		@Bean
		ElGamalGenerator elGamalGenerator() {
			return new ElGamalGenerator(new GqGroup(new BigInteger(P), new BigInteger(Q), new BigInteger(G)));
		}

		@Bean
		JobExecutionObjectContext executionObjectContext() throws IOException, URISyntaxException {
			final JobExecutionObjectContext jobExecutionObjectContextMock = mock(JobExecutionObjectContext.class);
			final VotersParametersHolder votersParametersHolderMock = mock(VotersParametersHolder.class);
			final EncryptionParameters encryptionParameters = new EncryptionParameters(P, Q, G);

			when(votersParametersHolderMock.getVotingCardCredentialInputDataPack()).thenReturn(mock(VotingCardCredentialInputDataPack.class));
			when(votersParametersHolderMock.getVotingCardCredentialInputDataPack().getEeid()).thenReturn("1");
			when(votersParametersHolderMock.getBallot()).thenReturn(getBallotFromResources());
			when(votersParametersHolderMock.getEncryptionParameters()).thenReturn(encryptionParameters);
			when(votersParametersHolderMock.getAbsoluteBasePath())
					.thenReturn(Paths.get(VotingCardGeneratorTest.class.getClassLoader().getResource(RESOURCES_FOLDER_NAME).toURI()).resolve("1"));
			when(votersParametersHolderMock.getCreateVotingCardSetCertificateProperties())
					.thenReturn(mock(CreateVotingCardSetCertificatePropertiesContainer.class));

			when(jobExecutionObjectContextMock.get(anyString(), eq(VotersParametersHolder.class))).thenReturn(votersParametersHolderMock);

			return jobExecutionObjectContextMock;
		}

		@Bean
		VoterCodesService codesGenerator() throws GeneralCryptoLibException {
			final VoterCodesService voterCodesServiceMock = mock(VoterCodesService.class);

			when(voterCodesServiceMock.generateShortVoteCastReturnCode()).thenReturn("1");
			when(voterCodesServiceMock.generateShortChoiceReturnCode()).thenReturn("1");
			when(voterCodesServiceMock.generateLongReturnCode(any(), any(), any(), any())).thenReturn(new byte[] {});
			when(voterCodesServiceMock.generateCodesMappingTableEntry(any(), any()))
					.thenReturn(new CodesMappingTableEntry(new byte[] { 0 }, new byte[] { 1 }))
					.thenReturn(new CodesMappingTableEntry(new byte[] { 1 }, new byte[] { 1 }));

			return voterCodesServiceMock;
		}

		@Bean
		ExtendedAuthenticationService extendedAuthenticationService() {
			final ExtendedAuthenticationService extendedAuthenticationServiceMock = mock(ExtendedAuthenticationService.class);

			when(extendedAuthenticationServiceMock.create(any(), any())).thenReturn(mock(ExtendedAuthInformation.class));

			return extendedAuthenticationServiceMock;
		}

		@Bean
		StartVotingKeyService startVotingKeyService() throws GeneralCryptoLibException {
			final StartVotingKeyService startVotingKeyServiceMock = mock(StartVotingKeyService.class);

			when(startVotingKeyServiceMock.generateStartVotingKey()).thenReturn("1");

			return startVotingKeyServiceMock;
		}

		@Bean
		VotingCardCredentialDataPackGenerator votingCardCredentialDataPackGenerator() throws GeneralCryptoLibException {
			final VotingCardCredentialDataPackGenerator votingCardCredentialDataPackGeneratorMock = mock(VotingCardCredentialDataPackGenerator.class);

			when(votingCardCredentialDataPackGeneratorMock.generate(any(), any(), any(), any(), any(), any(), any(), any()))
					.thenReturn(mock(VotingCardCredentialDataPack.class));

			return votingCardCredentialDataPackGeneratorMock;
		}

		@Bean("verificationCardCredentialDataPackGeneratorWithJobScope")
		VerificationCardCredentialDataPackGenerator verificationCardCredentialDataPackGenerator() throws GeneralCryptoLibException {

			final VerificationCardCredentialDataPackGenerator verificationCardCredentialDataPackGeneratorMock =
					mock(VerificationCardCredentialDataPackGenerator.class);

			final GqGroup gqGroup = new GqGroup(new BigInteger(P), new BigInteger(Q), new BigInteger(G));
			final ElGamalMultiRecipientPrivateKey cryptoPrimitivesPrivateKey = elGamalGenerator().genRandomPrivateKey(1);
			final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.from(cryptoPrimitivesPrivateKey, gqGroup.getGenerator());
			final ElGamalPublicKey publicKey = CryptoAdapters.convert(keyPair.getPublicKey());
			final ElGamalPrivateKey privateKey = CryptoAdapters.convert(keyPair.getPrivateKey(), gqGroup);
			final ElGamalKeyPair elGamalKeyPair = new ElGamalKeyPair(privateKey, publicKey);
			final VerificationCardCredentialDataPack verificationCardCredentialDataPack = mock(VerificationCardCredentialDataPack.class);

			when(verificationCardCredentialDataPack.getVerificationCardKeyPair()).thenReturn(elGamalKeyPair);

			return verificationCardCredentialDataPackGeneratorMock;
		}

		@Bean
		AsymmetricServiceAPI asymmetricServiceAPI() {
			return new AsymmetricService();
		}

		@Bean
		VotingCardGenerationJobExecutionContext jobContext() {
			final VotingCardGenerationJobExecutionContext context = mock(VotingCardGenerationJobExecutionContext.class);
			when(context.getVerificationCardSetId()).thenReturn("1");
			when(context.getVotingCardSetId()).thenReturn("1");
			return context;
		}

		@Bean
		CryptoAPIPBKDFDeriver deriver() {
			return mock(CryptoAPIPBKDFDeriver.class);
		}

		@Bean
		PrimitivesServiceAPI primitivesServiceAPI() throws GeneralCryptoLibException {

			final CryptoAPIPBKDFDeriver cryptoAPIPBKDFDeriverMock = mock(CryptoAPIPBKDFDeriver.class);
			when(cryptoAPIPBKDFDeriverMock.deriveKey(any(), any())).thenReturn("1"::getBytes);

			final PrimitivesServiceAPI primitivesServiceAPIMock = mock(PrimitivesServiceAPI.class);
			when(primitivesServiceAPIMock.getPBKDFDeriver()).thenReturn(cryptoAPIPBKDFDeriverMock);

			return primitivesServiceAPIMock;
		}

		@Bean
		JobExecution jobExecution() {
			final JobExecution jobExecutionMock = mock(JobExecution.class);
			final ExecutionContext executionContextMock = mock(ExecutionContext.class);

			when(executionContextMock.get(Constants.VERIFICATION_CARD_SET_ID)).thenReturn("1");
			when(executionContextMock.get(Constants.VOTING_CARD_SET_ID)).thenReturn("1");
			when(executionContextMock.get(Constants.NUMBER_VOTING_CARDS)).thenReturn(1);
			when(executionContextMock.get(Constants.SALT_KEYSTORE_SYM_ENC_KEY)).thenReturn("11");
			when(executionContextMock.get(Constants.SALT_CREDENTIAL_ID)).thenReturn("11");
			when(executionContextMock.get(Constants.BALLOT_ID)).thenReturn("1");
			when(executionContextMock.get(Constants.BALLOT_BOX_ID)).thenReturn("1");
			when(executionContextMock.get(Constants.VOTING_CARD_SET_ID)).thenReturn("1");
			when(executionContextMock.get(Constants.JOB_INSTANCE_ID)).thenReturn("1");
			when(executionContextMock.get(Constants.ELECTION_EVENT_ID)).thenReturn("1");

			when(jobExecutionMock.getExecutionContext()).thenReturn(executionContextMock);

			return jobExecutionMock;
		}

		@Bean
		VotingCardGenerator votingCardGenerator(final VoterCodesService voterCodesService,
				final ExtendedAuthenticationService extendedAuthenticationService,
				final StartVotingKeyService startVotingKeyService,
				final VotingCardCredentialDataPackGenerator votingCardCredentialDataPackGenerator,
				@Qualifier("verificationCardCredentialDataPackGeneratorWithJobScope")
				final VerificationCardCredentialDataPackGenerator verificationCardCredentialDataPackGeneratorWithJobScope,
				final PathResolver pathResolver,
				final PrimitivesServiceAPI primitivesService,
				final JobExecutionObjectContext objectContext,
				final JobExecution jobExecution) {
			return new VotingCardGenerator(voterCodesService, extendedAuthenticationService, startVotingKeyService,
					votingCardCredentialDataPackGenerator, verificationCardCredentialDataPackGeneratorWithJobScope, pathResolver, primitivesService,
					objectContext, jobExecution);
		}

		@Bean
		PathResolver pathResolver() throws URISyntaxException {
			final PathResolver pathResolverMock = mock(PathResolver.class);

			final Path setupKeyPath = Paths.get(VotingCardGeneratorTest.class.getResource("/setupSecretKey.json").toURI());
			when(pathResolverMock.resolve(Constants.CONFIG_FILES_BASE_DIR, "1", Constants.CONFIG_DIR_NAME_OFFLINE,
					Constants.SETUP_SECRET_KEY_FILE_NAME)).thenReturn(setupKeyPath);

			return pathResolverMock;
		}

		@Bean
		RandomService randomService() {
			return new RandomService();
		}
	}
}
