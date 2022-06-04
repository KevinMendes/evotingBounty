/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('votingCardSet', [])
    .service('votingCardSetService', function ($http, endpoints, $q) {
        'use strict';

        return {
            /**
             * Change the status of a voting card set.
             *
             * @param {object} votingCardSet the voting card set to modify
             * @param {string} status the status to set
             * @param {object} options other properties needed for the status change, such as the the
             *                         PEM of the private key used for signing
             * @returns a promise, successful if the status was changed
             */
            changeStatus: function (votingCardSet, status, options) {
                // Build the voting card set endpoint URL.
				const url =
					endpoints.host() +
					endpoints.votingCardSet
						.replace('{electionEventId}', votingCardSet.electionEvent.id)
						.replace('{votingCardSetId}', votingCardSet.id);
				// Set up a request body with the new status.
				let requestBody = {status: status};
				// Add the optional parameters to the request body.
                if (options) {
                    requestBody = Object.assign(requestBody, options);
                }
                // Return a promise that will call the voting card set endpoint
                // to change the status.
                return $http.put(url, requestBody).then(function (response) {
                    return response.data;
                });
            },
        };
    });
