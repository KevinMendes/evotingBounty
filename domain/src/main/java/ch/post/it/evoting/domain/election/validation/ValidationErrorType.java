/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.validation;

/**
 * Types of result for validation rules.
 */
public enum ValidationErrorType {

	SUCCESS,
	FAILED,
	ELECTION_OVER_DATE,
	ELECTION_NOT_STARTED,
	WRONG_BALLOT_CASTING_KEY,
	BCK_ATTEMPTS_EXCEEDED,
	WRONG_VERIFICATION_CODE,
	VERIFICATION_CODE_ATTEMPTS_EXCEEDED,
	AUTH_TOKEN_EXPIRED,
	INVALID_EEID,
	INVALID_SIGNATURE,
	INVALID_TENANT_ID,
	INVALID_VOTING_CARD_ID,
	INVALID_CERTIFICATE,
	INVALID_CERTIFICATE_CHAIN,
	INVALID_CREDENTIAL_ID_IN_CERTIFICATE,
	INVALID_CREDENTIAL_ID_IN_AUTHID,
	INVALID_CERTIFICATE_ROOT_CA,
	INVALID_VOTE_CORRECTNESS,
	INVALID_CLAUSE_VALIDATION,
	INVALID_NUMER_COMPONENTS_EXPONENTIATED_CIPHER_TEXT,
	NO_CLAUSE_FOUND,
	BLOCKED_BALLOT_BOX,
	RESOURCE_NOT_FOUND,
	CONFIRMATION_NOT_REQUIRED,
	INVALID_VERIFICATION_CARD_KEY,
	;
}
