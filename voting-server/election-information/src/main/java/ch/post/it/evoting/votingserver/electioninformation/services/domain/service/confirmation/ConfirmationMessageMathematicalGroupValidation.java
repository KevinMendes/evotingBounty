/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.confirmation;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ConfirmationMessageMathematicalGroupValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformationRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.confirmation.ConfirmationMessageValidation;

/**
 * This class implements the confirmation message is member of a mathematical group.
 */
public class ConfirmationMessageMathematicalGroupValidation implements ConfirmationMessageValidation {

	private static final String ENCRYPTION_PARAMETERS = "encryptionParameters";

	private static final String ENCRYPTION_PARAMETER_P = "p";

	private static final String ENCRYPTION_PARAMETER_Q = "q";

	private static final String ENCRYPTION_PARAMETER_G = "g";
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmationMessageMathematicalGroupValidation.class);
	@Inject
	private BallotBoxInformationRepository ballotBoxInformationRepository;

	/**
	 * This method implements the validation of confirmation message is member of a mathematical group.
	 *
	 * @param tenantId                - the tenant identifier.
	 * @param electionEventId         - the election event identifier.
	 * @param votingCardId            - the voting card identifier.
	 * @param confirmationInformation - the confirmation information to be validated.
	 * @param authenticationToken     - the authentication token.
	 * @return A ValidationError describing if the rule is satisfied or not.
	 */
	@Override
	public ValidationError execute(String tenantId, String electionEventId, String votingCardId, ConfirmationInformation confirmationInformation,
			AuthenticationToken authenticationToken) {
		ValidationError result = new ValidationError();

		BigInteger p;
		BigInteger q;
		BigInteger g;
		try {
			// encryption parameters
			String ballotBoxId = authenticationToken.getVoterInformation().getBallotBoxId();
			BallotBoxInformation ballotBoxInformation = ballotBoxInformationRepository
					.findByTenantIdElectionEventIdBallotBoxId(tenantId, electionEventId, ballotBoxId);
			JsonObject json = JsonUtils.getJsonObject(ballotBoxInformation.getJson());
			JsonObject encryptionParameters = json.getJsonObject(ENCRYPTION_PARAMETERS);
			p = new BigInteger(encryptionParameters.getString(ENCRYPTION_PARAMETER_P));
			q = new BigInteger(encryptionParameters.getString(ENCRYPTION_PARAMETER_Q));
			g = new BigInteger(encryptionParameters.getString(ENCRYPTION_PARAMETER_G));
		} catch (ResourceNotFoundException | NumberFormatException e) {
			throw new ConfirmationMessageMathematicalGroupValidationException("Error trying to validate confirmation message.", e);
		}

		try {
			// confirmation key
			BigInteger value = new BigInteger(
					new String(Base64.getDecoder().decode(confirmationInformation.getConfirmationMessage().getConfirmationKey()),
							StandardCharsets.UTF_8));

			if (BigInteger.ZERO.compareTo(value) == 0) {
				result.setValidationErrorType(ValidationErrorType.WRONG_BALLOT_CASTING_KEY);
			} else {
				// element
				ZpGroupElement element = new ZpGroupElement(value, p, q);

				// group
				ZpSubgroup group = new ZpSubgroup(g, p, q);

				// is group member?
				if (group.isGroupMember(element)) {
					result.setValidationErrorType(ValidationErrorType.SUCCESS);
				}
			}
		} catch (GeneralCryptoLibException | NumberFormatException e) {
			LOGGER.error("Error trying to validate confirmation message.", e);
			result.setErrorArgs(new String[] { ExceptionUtils.getRootCauseMessage(e) });
		}
		return result;
	}
}
