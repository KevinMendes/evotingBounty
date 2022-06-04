/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.tally.mixonline;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.controlcomponents.VerificationCardEntity;
import ch.post.it.evoting.controlcomponents.voting.EncryptedVerifiableVoteEntity;
import ch.post.it.evoting.controlcomponents.voting.EncryptedVerifiableVoteService;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.voting.sendvote.EncryptedVerifiableVote;

/**
 * Provides functionalities to retrieve the MixnetInitialPayload of a ballot box.
 */
@Service
public class MixnetInitialPayloadService {

	private final EncryptedVerifiableVoteService encryptedVerifiableVoteService;

	public MixnetInitialPayloadService(final EncryptedVerifiableVoteService encryptedVerifiableVoteService) {
		this.encryptedVerifiableVoteService = encryptedVerifiableVoteService;
	}

	/**
	 * Gets the {@link MixnetInitialPayload} of the given election event and ballot box.
	 *
	 * @param electionEventId                       ee, the identifier of the election event. Must be a valid UUID.
	 * @param ballotBoxId                           bb, the identifier of the ballot box. Must be a valid UUID.
	 * @param numberOfAllowedWriteInsPlusOne        delta_hat, the number of allowed write-ins plus one. Must be strictly positive.
	 * @param controlComponentsListOfConfirmedVotes L<sub>confirmedVotes,j</sub>, the control component's list of confirmed votes. Must be non-null.
	 * @param electionPublicKey                     EL<sub>pk</sub>, the election public key. Must be non-null.
	 * @return a MixnetInitialPayload
	 */
	@SuppressWarnings("java:S117")
	public MixnetInitialPayload getMixnetInitialPayload(final String electionEventId, final String ballotBoxId,
			final int numberOfAllowedWriteInsPlusOne, final List<EncryptedVerifiableVoteEntity> controlComponentsListOfConfirmedVotes,
			final ElGamalMultiRecipientPublicKey electionPublicKey) {

		final String ee = validateUUID(electionEventId);
		final String bb = validateUUID(ballotBoxId);
		checkArgument(numberOfAllowedWriteInsPlusOne >= 1, "The number of allowed write-ins + 1 must be at least 1.");
		final int delta_hat = numberOfAllowedWriteInsPlusOne;
		final int delta = electionPublicKey.size();
		checkArgument(delta_hat <= delta, "The election public key must have at least as many elements as the number of allowed write-ins + 1.");

		final List<EncryptedVerifiableVoteEntity> L_confirmedVotes_j = checkNotNull(controlComponentsListOfConfirmedVotes);
		final int N_C = L_confirmedVotes_j.size();
		final ElGamalMultiRecipientPublicKey EL_pk = checkNotNull(electionPublicKey);
		final GqGroup gqGroup = EL_pk.getGroup();
		final VerificationCardEntity verificationCardEntity = controlComponentsListOfConfirmedVotes.get(0).getVerificationCardEntity();
		final String verificationCardId = verificationCardEntity.getVerificationCardId();
		final String verificationCardSetId = verificationCardEntity.getVerificationCardSetEntity().getVerificationCardSetId();

		// Operation
		final Stream<EncryptedVerifiableVoteEntity> L_orderedVotes_j = order(L_confirmedVotes_j);
		final List<ElGamalMultiRecipientCiphertext> c_init_j = L_orderedVotes_j
				.map(entity -> new ContextIds(ee, verificationCardSetId, verificationCardId))
				.map(contextIds -> encryptedVerifiableVoteService.getEncryptedVerifiableVote(verificationCardId))
				.map(EncryptedVerifiableVote::getEncryptedVote)
				.collect(Collectors.toList());

		if (N_C < 2) {
			final ElGamalMultiRecipientMessage oneMessage = ElGamalMultiRecipientMessage.ones(gqGroup, delta_hat);
			final ZqElement oneExponent = ZqElement.create(1, ZqGroup.sameOrderAs(gqGroup));
			final ElGamalMultiRecipientCiphertext E_trivial = ElGamalMultiRecipientCiphertext.getCiphertext(oneMessage, oneExponent, EL_pk);
			c_init_j.add(E_trivial);
			c_init_j.add(E_trivial);
		}

		return new MixnetInitialPayload(ee, bb, gqGroup, c_init_j, EL_pk);
	}

	private Stream<EncryptedVerifiableVoteEntity> order(final List<EncryptedVerifiableVoteEntity> listOfConfirmedVotes) {
		return listOfConfirmedVotes.stream()
				.sorted(Comparator.comparing(
						encryptedVerifiableVoteEntity -> encryptedVerifiableVoteEntity.getVerificationCardEntity().getVerificationCardId()));
	}
}
