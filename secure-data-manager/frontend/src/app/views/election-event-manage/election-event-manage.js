/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/*jshint maxparams: 13 */
angular
    .module('election-event-manage', [])
    .controller('election-event-manage', function (
        $scope,
        $rootScope,
        sessionService,
        endpoints,
        $templateCache,
        $http,
        $state,
        $mdDialog,
        $q,
        gettextCatalog,
        settler,
        boardActivation,
        $mdToast,
        toastCustom,
        entitiesCounterService,
    ) {
        'use strict';

        const electionEventIdPattern = '{electionEventId}';

        const SYNCHRONIZE_MSG_ID = 'Synchronize';
        const SYNCHRONIZE_ERROR_MSG_ID = 'Synchronization failed due to connectivity errors';
        const ADMIN_BOARD_ACTIVATION_ERROR_MSG_ID = 'The Administration Board could not be activated';

        // set state to be used in the HTML template
        $scope.state = $state.current.name;
        $rootScope.$on('$stateChangeSuccess', function (event, toState) {
            $scope.state = toState.name;
        });

        /*
            Launch the endpoint responsible of synchronizing the application status
            */
        $scope.synchronizeInTabs = function () {
            sessionService.startSync();
            $scope.errors = {};

			const URLsToSynchronize = [
				endpoints.host() + endpoints.preconfiguration,
				(endpoints.host() + endpoints.synchronizeVoterPortalEEID).replace(
					electionEventIdPattern,
					sessionService.getSelectedElectionEvent().id,
				),
				(endpoints.host() + endpoints.updateComputationStatus).replace(
					electionEventIdPattern,
					sessionService.getSelectedElectionEvent().id,
				),
				(endpoints.host() + endpoints.updateBallotBoxStatus).replace(
					electionEventIdPattern,
					sessionService.getSelectedElectionEvent().id,
				),
			];

			try {
                $q.allSettled(
                    URLsToSynchronize.map(function (URL) {
                        return $http.post(URL);
                    }),
                ).then(function (responses) {
                    sessionService.stopSync();
					const settled = settler.settle(responses);
					if (settled.ok) {
                        entitiesCounterService.getTheMainItemsCount();
                        entitiesCounterService.getElectionEventInfo(
                            sessionService.getSelectedElectionEvent(),
                        );
                        switch ($state.current.name) {
                            case 'ballots':
                                $rootScope.$broadcast('refresh-ballots');
                                break;
                            case 'voting-cards':
                                $rootScope.$broadcast('refresh-voting-card-sets');
                                break;
                            case 'electoral-authorities':
                                $rootScope.$broadcast('refresh-authorities');
                                break;
                            case 'ballot-boxes':
                                $rootScope.$broadcast('refresh-ballot-boxes');
                                break;
                            default:
                                break;
                        }
                    } else {
                        $scope.errors.synchronizeFailed = true;
                        $mdDialog.show(
                            $mdDialog.customAlert({
                                locals: {
                                    title: gettextCatalog.getString(SYNCHRONIZE_MSG_ID),
                                    content: gettextCatalog.getString(SYNCHRONIZE_ERROR_MSG_ID),
                                },
                            }),
                        );
                    }
                });
            } catch (e) {
                sessionService.stopSync();
                $scope.data.message = e;
            }
        };

        /*
             Launch the endpoint responsible of activating the administration board status
             */
        $scope.activateAdminBoard = function () {
            boardActivation.init('adminBoard');
            boardActivation.adminBoardActivate().then(function (response) {
                if (response && response.data && response.data.error != '') {
                    $mdToast.show(
                        toastCustom.topCenter(
                            gettextCatalog.getString(ADMIN_BOARD_ACTIVATION_ERROR_MSG_ID),
                            'error',
                        ),
                    );
                } else {
                    boardActivation.openBoardAdmin();
                }
            });
        };

        /*
             Remove admin board key from memory
             */
        $scope.deactivateAdminBoard = function () {
			const adminBoard = sessionService.getSelectedAdminBoard();
			if (adminBoard) {
                adminBoard.privateKey = undefined;
                sessionService.setSelectedAdminBoard(adminBoard);
            }
        };

        $scope.isAdminAuthorityActivated = function () {
			const adminBoard = sessionService.getSelectedAdminBoard();
			if (!adminBoard) {
                return false;
            }
            return sessionService.getSelectedAdminBoard().privateKey;
        };
    });
