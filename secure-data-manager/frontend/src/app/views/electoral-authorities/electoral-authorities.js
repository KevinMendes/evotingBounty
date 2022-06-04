/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
    .module('electoral-authorities', [])
    .controller('electoral-authorities', function (
        $scope,
        $rootScope,
        $http,
        endpoints,
        sessionService,
        $mdDialog,
        $mdToast,
        $templateCache,
        CustomDialog,
        gettextCatalog,
        toastCustom,
        boardActivation,
    ) {
        'use strict';

        const electionEventIdPattern = '{electionEventId}';
        const electoralAuthorityIdPattern = '{electoralAuthorityId}';

        const ELECTORAL_AUTH_SIGNING_MSG_ID = 'Electoral authority signing';
        const ELECTORAL_AUTH_SIGNED_MSG_ID = 'Electoral authority signed!';
        const ELECTORAL_AUTH_MSG_ID = 'Electoral Authority';
        const ELECTORAL_AUTH_SELECT_FIRST_MSG_ID = 'Please select first a constituted Electoral authority';
        const ELECTORAL_AUTH_CONSTITUTE_MSG_ID = 'Constitute electoral authority';
		const ELECTORAL_AUTH_CONSTITUTE_SUCCESS_MSG_ID = 'Electoral Authority successfully constituted';
		const ELECTORAL_AUTH_MEMBERS_MSG_ID = 'Electoral Authority Members';
        const ADMIN_BOARD_ACTIVATE_MSG_ID = 'Please, activate the administration board.';
		const ADMIN_BOARD_ACTIVATE_OK_MSG_ID = 'Activate now';
		const ADMIN_BOARD_ACTIVATION_ERROR_MSG_ID = 'The Administration Board could not be activated';
		const ADMIN_BOARD_WRONG_ACTIVATED_MSG_ID = 'Wrong Administration Board activated';
		const ADMIN_BOARD_WRONG_ACTIVATED_EXPLANATION_MSG_ID = 'The active Administration Board does not belong to the Election Event that you are' +
			' trying to operate. Please deactivate it and activate the corresponding Administration Board for the Election Event';


        // initialize & populate view
        $scope.alert = '';
        $scope.errors = {};
        $scope.selectedElectionEventId = sessionService.getSelectedElectionEvent().id;
        $scope.selectedAuthority = null;
        $scope.noElectoralAuthoritySelected = true;
        $scope.electoralAuthorities = [];
        $scope.showMembersDialogTitle = '';
        $scope.votingCardSets = [];
        $scope.getSelecEAText = function () {
            return gettextCatalog.getString(ELECTORAL_AUTH_SELECT_FIRST_MSG_ID);
        };

        function DialogController($scope, $mdDialog) {
            $scope.hide = function () {
                $mdDialog.hide();
            };
            $scope.closeDialog = function () {
                $mdDialog.cancel();
            };
            $scope.answer = function (answer) {
                $mdDialog.hide(answer);
            };
        }

        // Called by the election-event-manage whenever it's necessary
        $scope.$on('refresh-authorities', function () {
            $scope.listElectoralAuthorities();
        });

        $scope.listElectoralAuthorities = function () {
            $scope.errors.getElectoralAuthoritiesFailed = false;
            $scope.selectedAuthority = null;

			const url =
				endpoints.host() +
				endpoints.electoralAuthorities.replace(
					electionEventIdPattern,
					$scope.selectedElectionEventId,
				);

			$http.get(url).then(
                function (response) {
                    const data = response.data;
                    $rootScope.safeApply(function () {
                        $scope.electoralAuthorities = data.result;
                        sessionService.setElectoralAuthorities(data.result);
                    });
                },
                function () {
                    $scope.errors.getElectoralAuthoritiesFailed = true;
                },
            );

			const urlVotingCardSets =
				endpoints.host() +
				endpoints.votingCardSets.replace(
					electionEventIdPattern,
					$scope.selectedElectionEventId,
				);
			$http.get(urlVotingCardSets).then(function (response) {
                const data = response.data;
                $scope.votingCardSets = data.result;
            });
        };

        $scope.uniqueChoice = function (authorities, authority) {
            if (authority.selected) {
                authorities.forEach(function (a) {
                    if (a.id !== authority.id) {
                        a.selected = false;
                    }
                });
            }

            if (authority.selected) {
                $scope.selectedAuthority = authority;
                $scope.selectedAuthority.ready = false;
            } else {
                $scope.selectedAuthority = null;
            }

            sessionService.setSelectedElectoralAuthority($scope.selectedAuthority);

            $scope.electoralAuthorityMembers = [];
            $scope.electoralAuthorities.forEach(function (electoralAuthority) {
                if (electoralAuthority.id === authority.id) {
                    $scope.electoralAuthorityMembers = electoralAuthority.electoralBoard;
                }
            });
        };

        $scope.constitute = function () {
            if ($scope.selectedAuthority.status !== 'LOCKED') {
                new CustomDialog()
                    .title(gettextCatalog.getString(ELECTORAL_AUTH_CONSTITUTE_MSG_ID))
                    .cannotPerform(gettextCatalog.getString(ELECTORAL_AUTH_MSG_ID))
                    .show();
                return;
            }

			const successCallback = function successCallback() {
				$scope.selectedAuthority.ready = true;
				sessionService.setSelectedElectoralAuthority($scope.selectedAuthority);

				$scope.listElectoralAuthorities();

				$mdToast.show(
					toastCustom.topCenter(
						gettextCatalog.getString(ELECTORAL_AUTH_CONSTITUTE_SUCCESS_MSG_ID),
						'success',
					),
				);
			};

			const errorCallback = function errorCallback() {
				new CustomDialog()
					.title(gettextCatalog.getString(ELECTORAL_AUTH_CONSTITUTE_MSG_ID))
					.error()
					.show();
			};

			const url = (endpoints.host() + endpoints.electoralAuthorityConstitute)
				.replace(electionEventIdPattern, $scope.selectedElectionEventId)
				.replace(electoralAuthorityIdPattern, $scope.selectedAuthority.id);

			$http.post(url).then(successCallback, errorCallback);
        };

        $scope.showMembers = function (electoralAuthorityId, ev) {
            $scope.electoralAuthorityMembers = [];
            $scope.electoralAuthorities.forEach(function (electoralAuthority) {
                if (electoralAuthority.id === electoralAuthorityId) {
                    electoralAuthority.electoralBoard.forEach(function (
                        electoralAuthorityMember,
                    ) {
                        $scope.electoralAuthorityMembers.push(electoralAuthorityMember);
                    });
                }
            });
            $scope.showMembersDialogTitle = gettextCatalog.getString(ELECTORAL_AUTH_MEMBERS_MSG_ID);
            $scope.listOfMembers = $scope.electoralAuthorityMembers;
            $mdDialog.show({
                controller: DialogController,
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

        $scope.isThereNoElectoralAuthoritySelected = function () {
            return !$scope.selectedAuthority;
        };

        $scope.getTextIsEASelected = function () {
			const check = $scope.isThereNoElectoralAuthoritySelected();
			return check ? $scope.getSelecEAText() : '';
        };

        $scope.isAdminAuthorityActivated = function () {
			const adminBoard = sessionService.getSelectedAdminBoard();
			if (!adminBoard) {
                return false;
            }
            return sessionService.getSelectedAdminBoard().privateKey;
        };

		const showSigningError = function () {
			new CustomDialog()
				.title(ELECTORAL_AUTH_SIGNING_MSG_ID)
				.error()
				.show();
		};

		$scope.sign = function () {
            if ($scope.selectedAuthority.status !== 'READY') {
                new CustomDialog()
                    .title(gettextCatalog.getString(ELECTORAL_AUTH_SIGNING_MSG_ID))
                    .cannotPerform(gettextCatalog.getString(ELECTORAL_AUTH_MSG_ID))
                    .show();
                return;
            }

            if (!$scope.isAdminAuthorityActivated()) {
				const p = $mdDialog.show(
					$mdDialog.customConfirm({
						locals: {
							title: gettextCatalog.getString(ELECTORAL_AUTH_SIGNING_MSG_ID),
							content: gettextCatalog.getString(ADMIN_BOARD_ACTIVATE_MSG_ID),
							ok: gettextCatalog.getString(ADMIN_BOARD_ACTIVATE_OK_MSG_ID),
						},
					}),
				);
				p.then(
                    function (success) {
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
                                boardActivation.openBoardAdmin().then(function (success) {
                                    $scope.sign();
                                });
                            }
                        });
                    },
                    function (error) {
                        //Not possible to open the $mdDialog
                    },
                );
                return;
            } else {
				const electionEvent = sessionService.getSelectedElectionEvent();
				if (!sessionService.doesActivatedABBelongToSelectedEE()) {
                    $mdDialog.show(
                        $mdDialog.customAlert({
                            locals: {
                                title: gettextCatalog.getString(ADMIN_BOARD_WRONG_ACTIVATED_MSG_ID),
                                content: gettextCatalog.getString(ADMIN_BOARD_WRONG_ACTIVATED_EXPLANATION_MSG_ID) + ' ' + electionEvent.defaultTitle + '.'
                            },
                        }),
                    );
                    return;
                }
            }

			const privateKeyBase64 = sessionService.getSelectedAdminBoard().privateKey;

			const url = (endpoints.host() + endpoints.electoralAuthoritySign)
				.replace(electionEventIdPattern, $scope.selectedElectionEventId)
				.replace(electoralAuthorityIdPattern, $scope.selectedAuthority.id);

			const body = {privateKeyPEM: privateKeyBase64};

			$http.put(url, body).then(
                function () {
                    $rootScope.safeApply(function () {
                        $mdToast.show(
                            toastCustom.topCenter(
                                gettextCatalog.getString(ELECTORAL_AUTH_SIGNED_MSG_ID),
                                'success',
                            ),
                        );
                        $scope.listElectoralAuthorities();
                    });
                },
                function () {
                    showSigningError();
                },
            );
        };

        $scope.unselectAll = function () {
            $scope.electoralAuthorities.forEach(function (authority) {
                authority.selected = false;
            });
        };

        $scope.capitalizeFirstLetter = function (string) {
			const lowerCaseString = string.toLowerCase();
			return lowerCaseString.charAt(0).toUpperCase() + lowerCaseString.slice(1);
        };

        $scope.listElectoralAuthorities();
    });
