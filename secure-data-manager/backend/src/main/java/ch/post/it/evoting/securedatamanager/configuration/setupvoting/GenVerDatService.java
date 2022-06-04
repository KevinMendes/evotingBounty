/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.stringToInteger;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.CryptoPrimitives;
import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.VerificationCardSet;
import ch.post.it.evoting.securedatamanager.services.application.service.BallotService;
import ch.post.it.evoting.securedatamanager.services.application.service.BaseVotingCardSetService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;

/**
 * Implements the GenVerDat algorithm described in the cryptographic protocol.
 */
@Service
public class GenVerDatService extends BaseVotingCardSetService {

	private final HashService hashService;
	private final BallotService ballotService;
	private final CryptoPrimitives cryptoPrimitives;
	private final BallotBoxRepository ballotBoxRepository;

	public GenVerDatService(
			@Qualifier("cryptoPrimitivesHashService")
			final HashService hashService,
			final BallotService ballotService,
			final CryptoPrimitives cryptoPrimitives,
			final BallotBoxRepository ballotBoxRepository) {
		this.hashService = hashService;
		this.ballotService = ballotService;
		this.cryptoPrimitives = cryptoPrimitives;
		this.ballotBoxRepository = ballotBoxRepository;
	}

	/**
	 * Initialize the control components' computation of the return codes.
	 *
	 * @param eligibleVoters       N<sub>E</sub>, the number of eligible voters. Must be strictly greater than 0.
	 * @param encodedVotingOptions p&#771;, the encoded voting options as prime numbers. Must no contain any null.
	 * @param verificationCardSet  the corresponding verification card set.
	 * @return the generated verification data as a {@link GenVerDatOutput}.
	 * @throws NullPointerException     if {@code encodedVotingOptions} is null.
	 * @throws IllegalArgumentException if
	 *                                  <ul>
	 *                                      <li>{@code encodedVotingOptions} contains any null</li>
	 *                                      <li>{@code eligibleVoters} is not strictly greater than 0</li>
	 *                                      <li>The number of voting options is greater than the secret key length</li>
	 *                                  </ul>
	 */
	@SuppressWarnings("java:S117")
	public GenVerDatOutput genVerDat(final int eligibleVoters, final List<GqElement> encodedVotingOptions,
			final VerificationCardSet verificationCardSet, final ElGamalMultiRecipientPublicKey setupPublicKey) {
		checkNotNull(encodedVotingOptions);
		checkNotNull(verificationCardSet);
		checkArgument(eligibleVoters > 0, "The number of eligible voters must be strictly greater than 0.");
		checkArgument(encodedVotingOptions.stream().allMatch(Objects::nonNull), "The encoded voting options must not contain any null elements.");

		final int N_E = eligibleVoters;
		final String ee = verificationCardSet.getElectionEventId();
		final int l_ID = Constants.BASE16_ID_LENGTH;
		final ImmutableList<GqElement> p_tilde = ImmutableList.copyOf(encodedVotingOptions);
		final ElGamalMultiRecipientPublicKey pk_setup = setupPublicKey;
		final int n = p_tilde.size();
		final int omega = pk_setup.size();
		checkArgument(n <= omega, "The number of voting options must be smaller than or equal to the setup secret key length.");

		final GqGroup gqGroup = pk_setup.getGroup();
		final ZqGroup zqGroup = ZqGroup.sameOrderAs(gqGroup);
		final RandomService randomService = new RandomService();
		final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(getBallot(verificationCardSet));

		// Output variables.
		final List<String> vc = new ArrayList<>();
		final List<ElGamalMultiRecipientKeyPair> verificationCardKeyPairs = new ArrayList<>();
		final List<String> BCK = new ArrayList<>();
		final List<ElGamalMultiRecipientCiphertext> c_pCC = new ArrayList<>();
		final List<ElGamalMultiRecipientCiphertext> c_ck = new ArrayList<>();

		// Algorithm.
		List<String> L_pCC = new ArrayList<>();
		for (int i = 0; i < N_E; i++) {
			final String vc_id = cryptoPrimitives.genRandomBase16String(l_ID).toLowerCase();
			final ElGamalMultiRecipientKeyPair verificationCardKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, 1, randomService);

			// Compute hpCC_id.
			final List<GqElement> hpCC_id_elements = new ArrayList<>();
			final ZqElement k_id = verificationCardKeyPair.getPrivateKey().get(0);
			for (int k = 0; k < n; k++) {
				final GqElement p_k = p_tilde.get(k);
				final GqElement pCC_id_k = p_k.exponentiate(k_id);
				final GqElement hpCC_id_k = hashService.hashAndSquare(pCC_id_k.getValue(), gqGroup);
				final String ci = combinedCorrectnessInformation.getCorrectnessIdForVotingOptionIndex(k);
				final byte[] lpCC_id_k = hashService.recursiveHash(hpCC_id_k, HashableString.from(vc_id), HashableString.from(ee),
						HashableString.from(ci));
				L_pCC.add(Base64.getEncoder().encodeToString(lpCC_id_k));

				hpCC_id_elements.add(hpCC_id_k);
			}
			final ElGamalMultiRecipientMessage hpCC_id = new ElGamalMultiRecipientMessage(hpCC_id_elements);

			// Compute c_pCC_id.
			final ZqElement hpCC_id_exponent = ZqElement.create(cryptoPrimitives.genRandomInteger(gqGroup.getQ()), zqGroup);
			final ElGamalMultiRecipientCiphertext c_pCC_id = ElGamalMultiRecipientCiphertext.getCiphertext(hpCC_id, hpCC_id_exponent, pk_setup);

			// Generate BCK_id.
			final int l_BCK = 9;
			String BCK_id;

			do {
				BCK_id = cryptoPrimitives.genUniqueDecimalStrings(l_BCK, 1).get(0);
			} while (stringToInteger(BCK_id).equals(BigInteger.ZERO));

			// Compute c_ck_id.
			final GqElement hBCK_id = hashService.hashAndSquare(stringToInteger(BCK_id), gqGroup);
			final GqElement CK_id = hBCK_id.exponentiate(k_id);

			final ElGamalMultiRecipientMessage hCK_id = new ElGamalMultiRecipientMessage(
					Collections.singletonList(hashService.hashAndSquare(CK_id.getValue(), gqGroup)));

			final ZqElement hCKExponent = ZqElement.create(cryptoPrimitives.genRandomInteger(gqGroup.getQ()), zqGroup);
			final ElGamalMultiRecipientCiphertext c_ck_id = ElGamalMultiRecipientCiphertext.getCiphertext(hCK_id, hCKExponent, pk_setup);

			// Outputs.
			vc.add(vc_id);
			verificationCardKeyPairs.add(verificationCardKeyPair);
			BCK.add(BCK_id);
			c_pCC.add(c_pCC_id);
			c_ck.add(c_ck_id);
		}
		L_pCC = order(L_pCC);

		return new GenVerDatOutput.Builder()
				.setVerificationCardIds(vc)
				.setVerificationCardKeyPairs(verificationCardKeyPairs)
				.setPartialChoiceReturnCodesAllowList(L_pCC)
				.setBallotCastingKeys(BCK)
				.setEncryptedHashedPartialChoiceReturnCodes(GroupVector.from(c_pCC))
				.setEncryptedHashedConfirmationKeys(GroupVector.from(c_ck))
				.build();
	}

	/**
	 * Orders a list of strings lexicographically.
	 *
	 * @param toOrder the list to order.
	 * @return the lexicographically ordered list of strings.
	 */
	@VisibleForTesting
	protected List<String> order(final List<String> toOrder) {
		Collections.sort(toOrder);

		return toOrder;
	}

	private Ballot getBallot(final VerificationCardSet precomputeContext) {
		final String ballotId = ballotBoxRepository.getBallotId(precomputeContext.getBallotBoxId());
		return ballotService.getBallot(precomputeContext.getElectionEventId(), ballotId);
	}

}

