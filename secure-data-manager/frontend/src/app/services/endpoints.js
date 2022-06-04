/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* jshint maxlen: 666 */
/* global process */

angular
	.module('endpoints', [])

	.factory('endpoints', function () {

		function isNode() {
			'use strict';
			return typeof process === 'object' && process + '' === '[object process]';
		}

		function host() {
			'use strict';
			return isNode() ? 'http://localhost:8090/sdm-backend/' : '/sdm-backend/';
		}

		return {
			host: host,
			status: 'status',
			close: 'close',
			sdmConfig: 'sdm-config',
			electionEvents: 'electionevents',
			administrationBoards: 'adminboards',
			constituteAdminBoard: 'adminboards/constitute/{adminBoardId}',
			checkAdminBoardShareStatus: 'adminboards/shares/status',
			writeAdminBoardShare: 'adminboards/{adminBoardId}/shares/{shareNum}',
			activateAdminBoardShare: 'adminboards/{adminBoardId}/activate',
			readAdminBoardShare: 'adminboards/{adminBoardId}/read/{shareNum}',
			reconstructAdminBoardShare: 'adminboards/{adminBoardId}/reconstruct',
			electionEvent: 'electionevents/{electionEventId}',
			requestKeys: 'electionevents/{electionEventId}/keys',
			preconfiguration: 'preconfiguration',
			ballots: 'ballots/electionevent/{electionEventId}',
			ballottexts:
				'ballottexts/electionevent/{electionEventId}/ballottext/{ballotId}',
			ballotSign: 'ballots/electionevent/{electionEventId}/ballot/{ballotId}', // PUT
			votingCardSets: 'votingcardsets/electionevent/{electionEventId}',
			votingCardSetSign:
				'votingcardsets/electionevent/{electionEventId}/votingcardset/{votingCardSetId}', // PUT
			votingCardSet:
				'votingcardsets/electionevent/{electionEventId}/votingcardset/{votingCardSetId}',
			electoralAuthorities:
				'electoralauthorities/electionevent/{electionEventId}',
			electoralAuthoritySign:
				'electoralauthorities/electionevent/{electionEventId}/electoralauthority/' +
				'{electoralAuthorityId}', // PUT
			electoralAuthorityConstitute:
				'electoralauthorities/constitute/{electionEventId}/{electoralAuthorityId}',
			checkElectoralAuthorityShareStatus: 'electoralauthorities/shares/status',
			writeElectoralAuthorityShare:
				'electoralauthorities/{electionEventId}/{electoralAuthorityId}/shares/{shareNum}',
			activateElectoralAuthorityShare:
				'electoralauthorities/{electionEventId}/{electoralAuthorityId}/activate',
			readElectoralAuthorityShare:
				'electoralauthorities/{electionEventId}/{electoralAuthorityId}/read/{shareNum}',
			reconstructElectoralAuthorityShare:
				'electoralauthorities/{electionEventId}/{electoralAuthorityId}/reconstruct',
			updateBallotBoxStatus: 'ballotboxes/electionevent/{electionEventId}/status',
			mixBallotBoxes: 'ballotboxes/electionevent/{electionEventId}/mix',
			ballotboxes: 'ballotboxes/electionevent/{electionEventId}',
			ballotBoxSign:
				'ballotboxes/electionevent/{electionEventId}/ballotbox/{ballotBoxId}', // PUT
			ballotbox:
				'ballotboxes/electionevent/{electionEventId}/ballotbox/{ballotBoxId}',
			synchronizeVoterPortal: 'configurations',
			synchronizeVoterPortalEEID:
				'configurations/electionevent/{electionEventId}',
			updateComputationStatus:
				'choicecodes/electionevent/{electionEventId}/status',
			mixing: 'mixing/electionevent/{electionEventId}/ballotbox/{ballotBoxId}',
			decrypting:
				'decryption/electionevent/{electionEventId}/ballotbox/{ballotBoxId}',
			progress: 'progress/{id}',
			progress_bulk: '{type}/progress/jobs',
			export: 'operation/export/{electionEventId}',
			import: 'operation/import',
			generatePreVotingOutputs:
				'operation/generate-pre-voting-outputs/{electionEventId}',
			generatePostVotingOutputs:
				'operation/generate-post-voting-outputs/{electionEventId}/{bbStatus}',
			languages: 'sdm-config/langs/'
		};
	});
