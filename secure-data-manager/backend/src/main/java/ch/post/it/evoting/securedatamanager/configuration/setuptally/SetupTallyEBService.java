/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration.setuptally;

import static ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair.genKeyPair;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;

/**
 * Implements the SetupTallyEB algorithm.
 */
@Service
public class SetupTallyEBService {

	private static final int MU = VotingOptionsConstants.MAXIMUM_NUMBER_OF_WRITE_IN_OPTIONS + 1;

	private final RandomService randomService;
	private final ElGamalService elGamalService;

	public SetupTallyEBService(final RandomService randomService, final ElGamalService elGamalService) {
		this.randomService = randomService;
		this.elGamalService = elGamalService;
	}

	/**
	 * Generates the last key pair (electoral board key pair (EB<sub>pk</sub>, EB<sub>sk</sub>)) and combines the CCM's public keys to yield the
	 * election public key (EL<sub>pk</sub>).
	 *
	 * @param input the {@link SetupTallyEBInput} containing all needed inputs. Non-null.
	 * @return the election public key and the electoral board key pair encapsulated in a {@link SetupTallyEBOutput}.
	 * @throws NullPointerException     if the input parameter is null.
	 * @throws IllegalArgumentException if δ is not smaller or equal to μ.
	 */
	@SuppressWarnings("java:S117")
	public SetupTallyEBOutput setupTallyEB(final SetupTallyEBInput input) {
		checkNotNull(input);

		// Variables.
		final ElGamalMultiRecipientPublicKey EL_pk_1 = input.getCcmElectionPublicKey1();
		final ElGamalMultiRecipientPublicKey EL_pk_2 = input.getCcmElectionPublicKey2();
		final ElGamalMultiRecipientPublicKey EL_pk_3 = input.getCcmElectionPublicKey3();
		final ElGamalMultiRecipientPublicKey EL_pk_4 = input.getCcmElectionPublicKey4();
		final int delta = input.getMaxWriteInsInAllVerificationCardSets() + 1;
		final GqGroup group = input.getGroup();

		// Require.
		checkArgument(delta <= MU, "Requires delta <= mu.");

		// Operation.
		final ElGamalMultiRecipientKeyPair electoralBoardKeyPair = genKeyPair(group, delta, randomService);
		final ElGamalMultiRecipientPublicKey EB_pk = electoralBoardKeyPair.getPublicKey();

		final ElGamalMultiRecipientPublicKey EL_pk_1_prime = new ElGamalMultiRecipientPublicKey(EL_pk_1.getKeyElements().subList(0, delta));
		final ElGamalMultiRecipientPublicKey EL_pk_2_prime = new ElGamalMultiRecipientPublicKey(EL_pk_2.getKeyElements().subList(0, delta));
		final ElGamalMultiRecipientPublicKey EL_pk_3_prime = new ElGamalMultiRecipientPublicKey(EL_pk_3.getKeyElements().subList(0, delta));
		final ElGamalMultiRecipientPublicKey EL_pk_4_prime = new ElGamalMultiRecipientPublicKey(EL_pk_4.getKeyElements().subList(0, delta));

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> EL_pk_prime_vector = GroupVector.of(EL_pk_1_prime, EL_pk_2_prime, EL_pk_3_prime,
				EL_pk_4_prime, EB_pk);

		final ElGamalMultiRecipientPublicKey EL_pk = elGamalService.combinePublicKeys(EL_pk_prime_vector);

		// Output.
		return new SetupTallyEBOutput(EL_pk, electoralBoardKeyPair);
	}

	/**
	 * Regroups the inputs needed by the SetupTallyEB algorithm. The inputs are :
	 * <ul>
	 * <li>EL<sub>pk,1</sub>, EL<sub>pk,2</sub> and EL<sub>pk,3</sub>, the CCM election public keys.</li>
	 * <li>δ - 1, the maximum number of write-ins in all verification card sets.</li>
	 * </ul>
	 */
	public static class SetupTallyEBInput {
		private final ElGamalMultiRecipientPublicKey ccmElectionPublicKey1;
		private final ElGamalMultiRecipientPublicKey ccmElectionPublicKey2;
		private final ElGamalMultiRecipientPublicKey ccmElectionPublicKey3;
		private final ElGamalMultiRecipientPublicKey ccmElectionPublicKey4;
		private final int maxWriteInsInAllVerificationCardSets;

		/**
		 * Constructor for a SetupTallyEBInput.
		 *
		 * @param ccmElectionPublicKeys                (EL<sub>pk,1</sub>, EL<sub>pk,2</sub>, EL<sub>pk,3</sub>, EL<sub>pk,4</sub>), the CCM election
		 *                                             public keys as a {@link GroupVector}. Non-null.
		 * @param maxWriteInsInAllVerificationCardSets δ - 1, the maximum number of write-ins in all verification card sets. Non-negative.
		 * @throws NullPointerException     if the CCM election public keys input parameter is null.
		 * @throws IllegalArgumentException if
		 *                                  <ul>
		 *                                      <li>there are not exactly 4 CCM election public keys provided in the ccmElectionPublicKeys vector.</li>
		 *                                      <li>EL<sub>pk,1</sub>, EL<sub>pk,2</sub> and EL<sub>pk,3</sub> are not of size μ.</li>
		 *                                      <li>δ is strictly negative.</li>
		 *                                  </ul>
		 */
		public SetupTallyEBInput(final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccmElectionPublicKeys,
				final int maxWriteInsInAllVerificationCardSets) {
			checkNotNull(ccmElectionPublicKeys);
			checkArgument(ccmElectionPublicKeys.size() == 4, "There must be exactly 3 CCM election public keys.");
			checkArgument(ccmElectionPublicKeys.get(0).size() == MU, "The CCM election public keys must all be of size mu.");
			checkArgument(maxWriteInsInAllVerificationCardSets >= 0,
					"The maximum number of write-ins in all verification card sets must be a non-negative number.");

			this.ccmElectionPublicKey1 = ccmElectionPublicKeys.get(0);
			this.ccmElectionPublicKey2 = ccmElectionPublicKeys.get(1);
			this.ccmElectionPublicKey3 = ccmElectionPublicKeys.get(2);
			this.ccmElectionPublicKey4 = ccmElectionPublicKeys.get(3);
			this.maxWriteInsInAllVerificationCardSets = maxWriteInsInAllVerificationCardSets;
		}

		public ElGamalMultiRecipientPublicKey getCcmElectionPublicKey1() {
			return this.ccmElectionPublicKey1;
		}

		public ElGamalMultiRecipientPublicKey getCcmElectionPublicKey2() {
			return this.ccmElectionPublicKey2;
		}

		public ElGamalMultiRecipientPublicKey getCcmElectionPublicKey3() {
			return this.ccmElectionPublicKey3;
		}

		public ElGamalMultiRecipientPublicKey getCcmElectionPublicKey4() {
			return ccmElectionPublicKey4;
		}

		public int getMaxWriteInsInAllVerificationCardSets() {
			return this.maxWriteInsInAllVerificationCardSets;
		}

		public GqGroup getGroup() {
			return this.ccmElectionPublicKey1.getGroup();
		}
	}

	/**
	 * Regroups the outputs of the SetupTallyEB algorithm. The outputs are :
	 * <ul>
	 * <li>EL<sub>pk</sub>, the election public key.</li>
	 * <li>(EB<sub>pk</sub>, EB<sub>sk</sub>), the electoral board key pair.</li>
	 * </ul>
	 */
	public static class SetupTallyEBOutput {
		private final ElGamalMultiRecipientPublicKey electionPublicKey;
		private final ElGamalMultiRecipientKeyPair electoralBoardKeyPair;

		/**
		 * Constructor for a SetupTallyEBOutput.
		 *
		 * @param electionPublicKey     EL<sub>pk</sub>, the election public key. Non-null.
		 * @param electoralBoardKeyPair (EB<sub>pk</sub>, EB<sub>sk</sub>), the electoral board key pair. Non-null.
		 * @throws NullPointerException     if any of the input parameters is null.
		 * @throws IllegalArgumentException if
		 *                                  <ul>
		 *                                      <li>the election public key has not the same size as the electoral board keys.</li>
		 *                                      <li>the election public key has not the same group as the electoral board keys.</li>
		 *                                  </ul>
		 */
		SetupTallyEBOutput(final ElGamalMultiRecipientPublicKey electionPublicKey, final ElGamalMultiRecipientKeyPair electoralBoardKeyPair) {
			checkNotNull(electionPublicKey);
			checkNotNull(electoralBoardKeyPair);
			checkArgument(electionPublicKey.size() == electoralBoardKeyPair.size(),
					"The election public key and the electoral board keys must be of same size.");
			checkArgument(electionPublicKey.getGroup().equals(electoralBoardKeyPair.getGroup()),
					"The election public key and the electoral board keys must have the same group.");

			this.electionPublicKey = electionPublicKey;
			this.electoralBoardKeyPair = electoralBoardKeyPair;
		}

		public ElGamalMultiRecipientPublicKey getElectionPublicKey() {
			return this.electionPublicKey;
		}

		public ElGamalMultiRecipientKeyPair getElectoralBoardKeyPair() {
			return this.electoralBoardKeyPair;
		}
	}

}
