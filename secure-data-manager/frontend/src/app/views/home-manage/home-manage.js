/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/*jshint maxparams: 13 */
angular
    .module('home-manage', [])
    .controller('home-manage', function (
        $rootScope,
        $scope,
        $state,
        $mdDialog,
        $mdToast,
        toastCustom,
        sessionService,
        $q,
        endpoints,
        $http,
        settler,
        gettextCatalog,
        ErrorsDict,
        entitiesCounterService,
        pathConverter,
    ) {
        'use strict';

        const refreshElectionEventsState = 'refresh-election-events';
        $scope.data = {};
        $scope.administrationBoards = {};

        // set state to be used in the HTML template
        $scope.state = $state.current.name;
        $rootScope.$on('$stateChangeSuccess', function (event, toState) {
            $scope.state = toState.name;
        });

        /*
             Launch the endpoint responsible of synchronizing the application status
             */
        $scope.synchronize = function () {
            sessionService.startSync();
            $scope.errors = {};

			const URLsToSynchronize = [
				endpoints.host() + endpoints.preconfiguration,
				endpoints.host() + endpoints.synchronizeVoterPortal,
			];

			try {
                $q.allSettled(
                    URLsToSynchronize.map(function (URL) {
                        return $http.post(URL);
                    }),
                ).then(function (responses) {
                    sessionService.stopSync();
                    $rootScope.$broadcast(refreshElectionEventsState);
					const settled = settler.settle(responses);
					if (settled.ok) {
                        $scope.listAdminBoards();
                        entitiesCounterService.getTheMainItemsCount();
                    }
                    if (settled.error) {
                        $scope.errors.synchronizeFailed = true;
                        $mdDialog.show(
                            $mdDialog.customAlert({
                                locals: {
                                    title: gettextCatalog.getString('Synchronize'),
                                    content: gettextCatalog.getString(
                                        'Synchronization failed due to connectivity errors',
                                    ),
                                },
                            }),
                        );
                    }
                });
            } catch (e) {
                $rootScope.$broadcast(refreshElectionEventsState);
                sessionService.stopSync();
                $scope.data.message = e;
            }
        };

        $scope.import = function (path) {
            path = pathConverter.toFolderPath(path);

			const url = endpoints.host() + endpoints.import;
			const body = {
				path: path,
			};

			$scope.title = gettextCatalog.getString('Importing...');

            $mdDialog.show({
                scope: $scope,
                preserveScope: true,
                templateUrl: 'app/views/dialogs/dialog-custom-progress-template.html',
                escapeToClose: false,
                parent: angular.element(document.body),
            });

            $http
                .post(url, body)
                .then(function (res) {
                    $mdDialog.hide();
                    $mdToast.show(
                        toastCustom.topCenter(
                            gettextCatalog.getString('Data imported successfully'),
                            'success',
                        ),
                    );
                })
                .catch(function (e) {
                    $mdDialog.hide();
                    if (e.data.error == '4005') {
                        $mdDialog.show(
                            $mdDialog.customAlert({
                                locals: {
                                    title: gettextCatalog.getString('Data has not been imported'),
                                    content: gettextCatalog.getString(ErrorsDict(e.data.error)),
                                },
                            }),
                        );
                    } else {
                        $mdToast.show(
                            toastCustom.topCenter(
                                gettextCatalog.getString('Import') +
                                ': ' +
                                gettextCatalog.getString(
                                    'Something went wrong. Contact with Support',
                                ) +
                                '. ' +
                                gettextCatalog.getString('Error code') +
                                ': ' +
                                e.data.error +
                                ', ' +
                                gettextCatalog.getString(ErrorsDict(e.data.error)),
                                'error',
                            ),
                        );
                    }
                })
                .finally(function () {
                    $rootScope.$broadcast(refreshElectionEventsState);
                    $scope.listAdminBoards();
                    entitiesCounterService.getTheMainItemsCount();
                });
        };

        $scope.listAdminBoards = function () {
            $scope.errors = {};
            $scope.selectedAuthority = null;

            $http.get(endpoints.host() + endpoints.administrationBoards).then(
                function (response) {
                    const data = response.data;
                    try {
                        sessionService.setAdminBoards(data.result);
                        $scope.administrationBoards = data.result;
                        $scope.checkAutoSynch();
                    } catch (e) {
                        console.log(e);
                        $scope.data.message = e.message;
                        $scope.errors.administrationBoardsFailed = true;
                    }
                },
                function () {
                    $scope.errors.administrationBoardsFailed = true;
                },
            );
        };

		let alreadySynched = false;

		$scope.checkAutoSynch = function () {
            if (sessionService.isSync()) {
                alreadySynched = true;
            }

            // perform autosynch if needed

            if (
                !alreadySynched &&
                (!$scope.administrationBoards ||
                    $scope.administrationBoards.length === 0)
            ) {
                alreadySynched = true;
                $scope.synchronize();
            }
        };

        $scope.listAdminBoards();
    });
