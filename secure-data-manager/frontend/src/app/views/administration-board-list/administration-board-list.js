/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/*jshint maxparams: 13 */
/*jshint maxlen: 1800 */
(function () {
    'use strict';

    angular
        .module('administration-board-list', ['app.dialogs'])
        .controller('administration-board-list', function (
            $scope,
            $mdDialog,
            sessionService,
            endpoints,
            $http,
            $templateCache,
            $mdToast,
            $timeout,
            $q,
            CustomDialog,
            gettextCatalog,
            toastCustom,
        ) {
            // initialize & populate view
			let alreadySynched = false;
			$scope.alert = '';
            $scope.errors = {};
            $scope.selectedAuthority = null;
            $scope.adminBoardMembers = [];
            $scope.ballotBoxes = [];
            $scope.keystoreInfo = {};
            $scope.showMembersDialogTitle = '';

            function DialogController($scope, $mdDialog) {
                $scope.hide = function () {
                    $mdDialog.hide();
                };
                $scope.cancel = function () {
                    $mdDialog.cancel();
                };
                $scope.answer = function (answer) {
                    $mdDialog.hide(answer);
                };
            }

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

            $scope.showMembers = function (adminBoardId, ev) {
                $scope.showMembersDialogTitle = gettextCatalog.getString(
                    'Administration Board Members',
                );
                $scope.adminBoardMembers = [];
                $scope.administrationBoards.forEach(function (adminBoard) {
                    if (adminBoard.id === adminBoardId) {
                        adminBoard.administrationBoard.forEach(function (adminBoardMember) {
                            $scope.adminBoardMembers.push(adminBoardMember);
                        });
                    }
                });

                $scope.listOfMembers = $scope.adminBoardMembers;
                $mdDialog.show({
                    controller: 'members',
                    template: $templateCache.get(
                        'app/views/dialogs/dialog-show-members.html',
                    ),
                    parent: angular.element(document.body),
                    targetEvent: ev,
                    clickOutsideToClose: true,
                    scope: $scope,
                    escapeToClose: true,
                    preserveScope: true,
                });
            };

            $scope.checkSelected = function () {
                return !$scope.selectedAuthority;
            };

            $scope.checkSelectedText = function () {
				const check = $scope.checkSelected();
				const selectAdminBoardText = gettextCatalog.getString(
					'Please select first an Administration board',
				);

				return check ? selectAdminBoardText : '';
            };

            $scope.uniqueChoice = function (adminBoardsList, adminBoard) {
                if (adminBoard.selected) {
                    adminBoardsList.forEach(function (a) {
                        if (a.id !== adminBoard.id) {
                            a.selected = false;
                        }
                    });
                }

                if (adminBoard.selected) {
                    $scope.selectedAuthority = adminBoard;
                    $scope.selectedAuthority.ready = false;
                } else {
                    $scope.selectedAuthority = null;
                }

                sessionService.setSelectedAdminBoard($scope.selectedAuthority);

                $scope.adminBoardMembers = [];
                $scope.administrationBoards.forEach(function (a) {
                    if (a.id === adminBoard.id) {
                        $scope.adminBoardMembers = a.administrationBoard;
                    }
                });
            };

            $scope.constituteAdminboard = function () {
                if ($scope.selectedAuthority.status !== 'LOCKED') {
                    new CustomDialog()
                        .title(gettextCatalog.getString('Constitute administration board'))
                        .cannotPerform(gettextCatalog.getString('Administration Board'))
                        .show();
                    return;
                }

                $scope.errors = {};

                sessionService.setNumberOfSuccessfullyWrittenSmartCards(0);
                $scope.listOfMembers = $scope.adminBoardMembers;
                $scope.sharesType = 'adminBoard';

                $scope.dialogPromise = $mdDialog.show({
                    controller: DialogController,
                    template: $templateCache.get(
                        'app/views/members-constitute/members-constitute.html',
                    ),
                    parent: angular.element(document.body),
                    clickOutsideToClose: false,
                    scope: $scope,
                    sessionService: sessionService,
                    escapeToClose: false,
                    preserveScope: true,
                });

                $scope.dialogPromise.finally(function () {
                    $scope.listAdminBoards();

                    if (
                        sessionService.getNumberOfSuccessfullyWrittenSmartCards() ===
                        $scope.adminBoardMembers.length
                    ) {
                        $mdToast.show(
                            toastCustom.topCenter(
                                gettextCatalog.getString(
                                    'Administration board successfully constituted',
                                ),
                                'success',
                            ),
                        );

                        sessionService.setNumberOfSuccessfullyWrittenSmartCards(0);
                    }
                    $scope.sharesType = undefined;
                });
            };

            $scope.capitalizeFirstLetter = function (string) {
				const lowerCaseString = string.toLowerCase();
				return (
                    lowerCaseString.charAt(0).toUpperCase() + lowerCaseString.slice(1)
                );
            };
        });
})();
