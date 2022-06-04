/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
(function () {
    'use strict';

    angular
        .module('configElectionConstants', [])

        .factory('configElectionConstants', function (gettextCatalog) {
            return {
                STATUS_NEW: {
                    code: 'NEW',
                    text: gettextCatalog.getString('New'),
                },
                STATUS_LOCKED: {
                    code: 'LOCKED',
                    text: gettextCatalog.getString('Locked'),
                },
                STATUS_READY: {
                    code: 'READY',
                    text: gettextCatalog.getString('Ready'),
                },
                STATUS_SIGNED: {
                    code: 'SIGNED',
                    text: gettextCatalog.getString('Signed'),
                },
                STATUS_CLOSED: {
                    code: 'CLOSED',
                    text: gettextCatalog.getString('Closed'),
                },
                STATUS_GENERATED: {
                    code: 'GENERATED',
                    text: gettextCatalog.getString('Generated'),
                },
                STATUS_PRECOMPUTED: {
                    code: 'PRECOMPUTED',
                    text: gettextCatalog.getString('Pre-computed'),
                },
                STATUS_COMPUTED: {
                    code: 'COMPUTED',
                    text: gettextCatalog.getString('Computed'),
                },
                STATUS_BB_DOWNLOADED: {
                    code: 'BB_DOWNLOADED',
                    text: gettextCatalog.getString('Downloaded'),
                },
                STATUS_VCS_DOWNLOADED: {
                    code: 'VCS_DOWNLOADED',
                    text: gettextCatalog.getString('Downloaded'),
                },
                STATUS_MIXED: {
                    code: 'MIXED',
                    text: gettextCatalog.getString('Mixed'),
                },
                STATUS_DECRYPTED: {
                    code: 'DECRYPTED',
                    text: gettextCatalog.getString('Decrypted'),
                },
                TEST_REGULAR: {
                    code: 'false',
                    text: gettextCatalog.getString('Regular'),
                },
                TEST_TEST: {
                    code: 'true',
                    text: gettextCatalog.getString('Test'),
                },
                TO_STATE: {
                    BALLOTS: 'ballots',
                    VOTING_CARDS: 'voting-cards',
                    ELECTORAL_AUTHORITIES: 'electoral-authorities',
                    BALLOT_BOXES: 'ballot-boxes',
                },
            };
        });
})();
