/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.rule;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class VoteIdsRuleImplTest {

	@InjectMocks
	@Spy
	private final VoteIdsRule voteIdsRule = new VoteIdsRule();
	@Rule
	public ExpectedException expected = ExpectedException.none();
	@Mock
	private Logger LOGGER;

	@Test
	public void validVoteAndToken() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString =
				"{\"id\":\"f4fqq5pbp+CQwy68GJEX0A==\",\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"f86b9967f5f14a01b9f70c6277c38329\","
						+ "\"votingCardId\":\"14d96e761e571a08769dc4337a2ce98b\",\"ballotId\":\"0edd4256df7d4b17a148f218080abea9\",\"credentialId\":\"0edc375a02584cb09f35a9b956bc768f\",\"verificationCardId\":\"4f55fa5d99484d22b983004206b12444\",\"ballotBoxId\":\"b972e500fe554ed7b24e54814e9b3c85\",\"votingCardSetId\":\"8ba6e4decda64a5eb43c28657df7be3d\",\"verificationCardSetId\":\"4e509620593647aab6df2aa175c833e7\"},\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		doNothing().when(voteIdsRule).validateAuthToken(any());

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.SUCCESS);
	}

	@Test
	public void invalidAuthTokenJson() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString = "{\"voterInformation\"{\"tenantId\":\"100\",\"electionEventId\":\"f86b9967f5f14a\n01b9f70c6277c38329\","
				+ "\"votingCardId\":\"14d96e761e571a08769dc4337a2ce98b\",\"ballotId\":\"0edd4256df7d4b17a148f218080abea9\",\"credentialId\":\"0edc375a02584cb09f35a9b956bc768f\",\"verificationCardId\":\"4f55fa5d99484d22b983004206b12444\",\"ballotBoxId\":\"b972e500fe554ed7b24e54814e9b3c85\",\"votingCardSetId\":\"8ba6e4decda64a5eb43c28657df7be3d\",\"verificationCardSetId\":\"4e509620593647aab6df2aa175c833e7\"},\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidAuthToken() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString = "{\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"f86b9967f5f14a\n01b9f70c6277c38329\","
				+ "\"votingCardId\":\"14d96e761e571a08769dc4337a2ce98b\",\"ballotId\":\"0edd4256df7d4b17a148f218080abea9\",\"credentialId\":\"0edc375a02584cb09f35a9b956bc768f\",\"verificationCardId\":\"4f55fa5d99484d22b983004206b12444\",\"ballotBoxId\":\"b972e500fe554ed7b24e54814e9b3c85\",\"votingCardSetId\":\"8ba6e4decda64a5eb43c28657df7be3d\",\"verificationCardSetId\":\"4e509620593647aab6df2aa175c833e7\"},\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidAuthToken2() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString =
				"{\"id\":\"f4fqq5pbp+CQwy68GJEX0A==\"\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"f86b9967f5f14a01b9f70c6277c38329\","
						+ "\"votingCardId\":\"14d96e761e571a08769dc4337a2ce98b\",\"ballotId\":\"0edd4256df7d4b17a148f218080abea9\",\"credentialId\":\"0edc375a02584cb09f35a9b956bc768f\",\"verificationCardId\":\"4f55fa5d99484d22b983004206b12444\",\"ballotBoxId\":\"b972e500fe554ed7b24e54814e9b3c85\",\"votingCardSetId\":\"8ba6e4decda64a5eb43c28657df7be3d\",\"verificationCardSetId\":\"4e509620593647aab6df2aa175c833e7\"},\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidNullVote() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = null;

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidNullAuthToken() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString = null;
		vote.setAuthenticationToken(authTokenString);

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidNullVoterInformation() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString = "{\"id\":\"f4fqq5pbp+CQwy68GJEX0A==\",\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		doNothing().when(voteIdsRule).validateAuthToken(any());

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidTenantId() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("XXXX");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString =
				"{\"id\":\"f4fqq5pbp+CQwy68GJEX0A==\",\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"f86b9967f5f14a01b9f70c6277c38329\","
						+ "\"votingCardId\":\"14d96e761e571a08769dc4337a2ce98b\",\"ballotId\":\"0edd4256df7d4b17a148f218080abea9\",\"credentialId\":\"0edc375a02584cb09f35a9b956bc768f\",\"verificationCardId\":\"4f55fa5d99484d22b983004206b12444\",\"ballotBoxId\":\"b972e500fe554ed7b24e54814e9b3c85\",\"votingCardSetId\":\"8ba6e4decda64a5eb43c28657df7be3d\",\"verificationCardSetId\":\"4e509620593647aab6df2aa175c833e7\"},\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		doNothing().when(voteIdsRule).validateAuthToken(any());

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidNullElectionEventIdInVote() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId(null);
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString =
				"{\"id\":\"f4fqq5pbp+CQwy68GJEX0A==\",\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"f86b9967f5f14a01b9f70c6277c38329\","
						+ "\"votingCardId\":\"14d96e761e571a08769dc4337a2ce98b\",\"ballotId\":\"0edd4256df7d4b17a148f218080abea9\",\"credentialId\":\"0edc375a02584cb09f35a9b956bc768f\",\"verificationCardId\":\"4f55fa5d99484d22b983004206b12444\",\"ballotBoxId\":\"b972e500fe554ed7b24e54814e9b3c85\",\"votingCardSetId\":\"8ba6e4decda64a5eb43c28657df7be3d\",\"verificationCardSetId\":\"4e509620593647aab6df2aa175c833e7\"},\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		doNothing().when(voteIdsRule).validateAuthToken(any());

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidNullElectionEventIdInAuthToken() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString = "{\"id\":\"f4fqq5pbp+CQwy68GJEX0A==\",\"voterInformation\":{\"tenantId\":\"100\","
				+ "\"votingCardId\":\"14d96e761e571a08769dc4337a2ce98b\",\"ballotId\":\"0edd4256df7d4b17a148f218080abea9\",\"credentialId\":\"0edc375a02584cb09f35a9b956bc768f\",\"verificationCardId\":\"4f55fa5d99484d22b983004206b12444\",\"ballotBoxId\":\"b972e500fe554ed7b24e54814e9b3c85\",\"votingCardSetId\":\"8ba6e4decda64a5eb43c28657df7be3d\",\"verificationCardSetId\":\"4e509620593647aab6df2aa175c833e7\"},\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		doNothing().when(voteIdsRule).validateAuthToken(any());

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidEmptyElectionEventIdInAuthToken() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString = "{\"id\":\"f4fqq5pbp+CQwy68GJEX0A==\",\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"\","
				+ "\"votingCardId\":\"14d96e761e571a08769dc4337a2ce98b\",\"ballotId\":\"0edd4256df7d4b17a148f218080abea9\",\"credentialId\":\"0edc375a02584cb09f35a9b956bc768f\",\"verificationCardId\":\"4f55fa5d99484d22b983004206b12444\",\"ballotBoxId\":\"b972e500fe554ed7b24e54814e9b3c85\",\"votingCardSetId\":\"8ba6e4decda64a5eb43c28657df7be3d\",\"verificationCardSetId\":\"4e509620593647aab6df2aa175c833e7\"},\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		doNothing().when(voteIdsRule).validateAuthToken(any());

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidVotingCardId() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString =
				"{\"id\":\"f4fqq5pbp+CQwy68GJEX0A==\",\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"f86b9967f5f14a01b9f70c6277c38329\","
						+ "\"votingCardId\":\"14d96e761e571a08769dc4337a2ce98c\",\"ballotId\":\"0edd4256df7d4b17a148f218080abea9\",\"credentialId\":\"0edc375a02584cb09f35a9b956bc768f\",\"verificationCardId\":\"4f55fa5d99484d22b983004206b12444\",\"ballotBoxId\":\"b972e500fe554ed7b24e54814e9b3c85\",\"votingCardSetId\":\"8ba6e4decda64a5eb43c28657df7be3d\",\"verificationCardSetId\":\"4e509620593647aab6df2aa175c833e7\"},\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		doNothing().when(voteIdsRule).validateAuthToken(any());

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidBallotBoxId() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c86");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString =
				"{\"id\":\"f4fqq5pbp+CQwy68GJEX0A==\",\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"f86b9967f5f14a01b9f70c6277c38329\","
						+ "\"votingCardId\":\"14d96e761e571a08769dc4337a2ce98b\",\"ballotId\":\"0edd4256df7d4b17a148f218080abea9\",\"credentialId\":\"0edc375a02584cb09f35a9b956bc768f\",\"verificationCardId\":\"4f55fa5d99484d22b983004206b12444\",\"ballotBoxId\":\"b972e500fe554ed7b24e54814e9b3c85\",\"votingCardSetId\":\"8ba6e4decda64a5eb43c28657df7be3d\",\"verificationCardSetId\":\"4e509620593647aab6df2aa175c833e7\"},\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		doNothing().when(voteIdsRule).validateAuthToken(any());

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidBallotId() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0ed");
		vote.setCredentialId("0edc375a02584cb09f35a9b956bc768f");

		String authTokenString =
				"{\"id\":\"f4fqq5pbp+CQwy68GJEX0A==\",\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"f86b9967f5f14a01b9f70c6277c38329\","
						+ "\"votingCardId\":\"14d96e761e571a08769dc4337a2ce98b\",\"ballotId\":\"0edd4256df7d4b17a148f218080abea9\",\"credentialId\":\"0edc375a02584cb09f35a9b956bc768f\",\"verificationCardId\":\"4f55fa5d99484d22b983004206b12444\",\"ballotBoxId\":\"b972e500fe554ed7b24e54814e9b3c85\",\"votingCardSetId\":\"8ba6e4decda64a5eb43c28657df7be3d\",\"verificationCardSetId\":\"4e509620593647aab6df2aa175c833e7\"},\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		doNothing().when(voteIdsRule).validateAuthToken(any());

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}

	@Test
	public void invalidCredentialId() throws ApplicationException, SyntaxErrorException, SemanticErrorException {
		Vote vote = new Vote();
		vote.setTenantId("100");
		vote.setElectionEventId("f86b9967f5f14a01b9f70c6277c38329");
		vote.setVotingCardId("14d96e761e571a08769dc4337a2ce98b");
		vote.setBallotBoxId("b972e500fe554ed7b24e54814e9b3c85");
		vote.setBallotId("0edd4256df7d4b17a148f218080abea9");
		vote.setCredentialId("b09f35a9b956bc768f");

		String authTokenString =
				"{\"id\":\"f4fqq5pbp+CQwy68GJEX0A==\",\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"f86b9967f5f14a01b9f70c6277c38329\","
						+ "\"votingCardId\":\"14d96e761e571a08769dc4337a2ce98b\",\"ballotId\":\"0edd4256df7d4b17a148f218080abea9\",\"credentialId\":\"0edc375a02584cb09f35a9b956bc768f\",\"verificationCardId\":\"4f55fa5d99484d22b983004206b12444\",\"ballotBoxId\":\"b972e500fe554ed7b24e54814e9b3c85\",\"votingCardSetId\":\"8ba6e4decda64a5eb43c28657df7be3d\",\"verificationCardSetId\":\"4e509620593647aab6df2aa175c833e7\"},\"timestamp\":\"1452850289632\",\"signature\":\"WKIQj8tibxtOoFtVe6/bBFXSv9ZdYhAF6bPymCkqUBsN25RYo0DIy9jWz2G6yt91c2UJkWwXzZCoJDE53l2YvcuUfeeAs66FV2q4Et1sNLa+wiNN4LL1cNx1wc7+9pgRUL15ItO8fN9XM1y6fF61aGBOng9KOvKX7YbkHPzl3/+7vkhF/5oMS3nj871yPUpx4Yxwg7RpSKvFXdAeW3iRBeYbN2BXmc8euWmF80YD5KJUd3aGJ/Z5O/oGWCP2YHzF1OVZzAe2hs8TY1k88D0pc+s8VUAohG3Qf59iuVD4ov1KurrRsJmP9F/KtDeh9qIqMRpJAYSKPZ+c2MQUcOfLkw==\"}";
		vote.setAuthenticationToken(authTokenString);

		doNothing().when(voteIdsRule).validateAuthToken(any());

		ValidationError result = voteIdsRule.execute(vote);
		assertEquals(result.getValidationErrorType(), ValidationErrorType.FAILED);
	}
}
