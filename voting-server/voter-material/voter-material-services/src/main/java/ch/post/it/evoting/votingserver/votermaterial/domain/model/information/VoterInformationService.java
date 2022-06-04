/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.domain.model.information;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

@Stateless
public class VoterInformationService {

	private static final char SQL_ESCAPE_CHARACTER = '~';

	@Inject
	private Logger logger;

	@Inject
	private VoterInformationRepository voterInformationRepository;

	/**
	 * Search voter information.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param idSearchTerm    the voting Card id
	 * @param pageNumber      the page number
	 * @param pageSize        the page size
	 * @return the list
	 * @throws ResourceNotFoundException    the resource not found exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public List<VoterInformation> searchVoterInformation(final String tenantId, final String electionEventId, String idSearchTerm,
			final int pageNumber, final int pageSize) throws ResourceNotFoundException, UnsupportedEncodingException {

		logger.info("Searching the voting cards for tenant: {} election event: {} and id search term: {}.", tenantId, electionEventId, idSearchTerm);

		String idSearchTermDecoded = decodeAnySpecialCharactersBeforeSearch(idSearchTerm);

		logger.info("Term after decode: {}.", idSearchTermDecoded);

		// search the voterInformation for given search terms.

		List<VoterInformation> voterInformation = voterInformationRepository
				.findByTenantIdElectionEventIdAndSearchTerms(tenantId, electionEventId, idSearchTermDecoded, pageNumber, pageSize);

		logger.info("Credential data for tenant: {} election event: {} found.", tenantId, electionEventId);

		return voterInformation;
	}

	/**
	 * Gets the count of voting cards.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param idSearchTerm    the id search term
	 * @return the count of voting cards
	 * @throws ResourceNotFoundException    the resource not found exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public long getCountOfVotingCardsForSearchTerms(final String tenantId, final String electionEventId, String idSearchTerm)
			throws ResourceNotFoundException, UnsupportedEncodingException {

		String idSearchTermDecoded = decodeAnySpecialCharactersBeforeSearch(idSearchTerm);

		logger.info("Counting the voting cards for tenant: {} election event: {} and id search term: {}.", tenantId, electionEventId,
				idSearchTermDecoded);

		return voterInformationRepository.countByTenantIdElectionEventIdAndSearchTerms(tenantId, electionEventId, idSearchTermDecoded);
	}

	/**
	 * Decode any special characters before search.
	 *
	 * @param term1 the term1
	 * @return the string
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	private String decodeAnySpecialCharactersBeforeSearch(final String term1) throws UnsupportedEncodingException {
		return URLDecoder.decode(term1, "UTF-8").replace("~", SQL_ESCAPE_CHARACTER + "~").replace("_", SQL_ESCAPE_CHARACTER + "_")
				.replace("percentage", SQL_ESCAPE_CHARACTER + "%");
	}
}
