/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

/*jshint maxparams: 12 */

function DialogController($scope, $mdDialog) {
    'use strict';
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

angular
    .module('ballots', [])
    .controller('ballots', function (
        $scope,
        $rootScope,
        $mdDialog,
        $mdToast,
        sessionService,
        endpoints,
        $http,
        $q,
        _,
        settler,
        CustomDialog,
        gettextCatalog,
        ballotprinter,
        toastCustom,
        boardActivation,
    ) {
        'use strict';

		const electionEventIdPattern = '{electionEventId}';
		const ballotIdPattern = '{ballotId}';

		const BALLOT_MSG_ID = 'Ballot';
		const BALLOT_SELECT_FIRST_MSG_ID = 'Please select first a Ballot';
		const BALLOT_VIEW_MSG_ID = 'View ballot(s)';
		const BALLOT_SELECT_TO_VIEW_MSG_ID = 'Please, select some ballots to view from the list.';
		const BALLOT_SIGNING_MSG_ID = 'Ballot signing';
		const BALLOT_SIGNING_ERROR_MSG_ID = 'Some ballot(s) could not be signed. Please review the list';
		const BALLOT_SELECT_TO_SIGN_MSG_ID = 'Please, select some ballots to sign from the list';
		const BALLOT_SIGNED_MSG_ID = 'Ballot(s) signed!';
		const ADMIN_BOARD_ACTIVATE_MSG_ID = 'Please, activate the administration board';
		const ADMIN_BOARD_ACTIVATE_OK_MSG_ID = 'Activate now';
		const ADMIN_BOARD_ACTIVATION_ERROR_MSG_ID = 'The Administration Board could not be activated';
		const ADMIN_BOARD_WRONG_ACTIVATED_MSG_ID = 'Wrong Administration Board activated';
		const ADMIN_BOARD_WRONG_ACTIVATED_EXPLANATION_MSG_ID = 'The active Administration Board does not belong to the Election Event that you are' +
			' trying to operate. Please deactivate it and activate the corresponding Administration Board for the Election Event';

        $scope.selectBallotText = function () {
            return gettextCatalog.getString(BALLOT_SELECT_FIRST_MSG_ID);
        };
        $scope.listBallots = function () {
            $scope.errors.getBallotsFailed = false;

			const url =
				endpoints.host() +
				endpoints.ballots.replace(
					electionEventIdPattern,
					$scope.selectedElectionEventId,
				);
			$http.get(url).then(
                function (response) {
                    const data = response.data;
                    $rootScope.safeApply(function () {
                        $scope.ballots = data;
                    });
                },
                function () {
                    $scope.errors.getBallotsFailed = true;
                },
            );
        };

		const showBallots = function (ev) {
			$mdDialog
				.show({
					controller: DialogController,
					templateUrl: 'app/views/dialogs/dialog-view-ballot.html',
					parent: angular.element(document.body),
					targetEvent: ev,
					scope: $scope,
					clickOutsideToClose: true,
					escapeToClose: true,
					preserveScope: true,
				})
				.then(function () {
					//
				});
		};

		$scope.viewBallot = function (ballotId, ev) {
            // get ballot to display

            $scope.ballotsToView = [];
            $scope.ballots.result.forEach(function (ballot) {
                if (ballot.id === ballotId) {
                    $scope.ballotsToView.push(ballot);
                }
            });

            // no selection?

            if ($scope.ballotsToView.length <= 0) {
                $mdDialog.show(
                    $mdDialog.customAlert({
                        locals: {
                            title: gettextCatalog.getString(BALLOT_VIEW_MSG_ID),
                            content: gettextCatalog.getString(BALLOT_SELECT_TO_VIEW_MSG_ID),
                        },
                    }),
                );
                return;
            }

            // collect corresponding ballot texts

            $q.allSettled(
                $scope.ballotsToView.map(function (ballot) {
					const url = (endpoints.host() + endpoints.ballottexts)
						.replace(electionEventIdPattern, $scope.selectedElectionEventId)
						.replace(ballotIdPattern, ballot.id);
					return $http.get(url);
                }),
            ).then(function (responses) {
				const settled = settler.settle(responses);
				$scope.ballotTextsToView = settled.fulfilled.map(function (response) {
                    return response.data.result;
                });

                // show the ballots

                ballotprinter.print($scope.ballotsToView);
                showBallots(ev);
            });
        };

        $scope.getBallotTexts = function (ballotid) {
            return _.find($scope.ballotTextsToView, function (text) {
                return ballotid === text[0]['ballot']['id'];
            });
        };

        $scope.isAdminAuthorityActivated = function () {
			const adminBoard = sessionService.getSelectedAdminBoard();
			if (!adminBoard) {
                return false;
            }
            return sessionService.getSelectedAdminBoard().privateKey;
        };

		const showBallotApprovalError = function () {
			$mdDialog.show(
				$mdDialog.customAlert({
					locals: {
						title: gettextCatalog.getString(BALLOT_SIGNING_MSG_ID),
						content: gettextCatalog.getString(BALLOT_SIGNING_ERROR_MSG_ID),
					},
				}),
			);
		};

		$scope.sign = function (ev) {
            $scope.errors.ballotApprovalError = false;

			const ballotsToSign = [];
			const ballotsSelected = [];
			$scope.ballots.result.forEach(function (ballot) {
                if (ballot.selected && ballot.status === 'LOCKED') {
                    ballotsToSign.push(ballot);
                }
                if (ballot.selected) {
                    ballotsSelected.push(ballot);
                }
            });

            if (ballotsSelected.length <= 0) {
                $mdDialog.show(
                    $mdDialog.customAlert({
                        locals: {
                            title: gettextCatalog.getString(BALLOT_SIGNING_MSG_ID),
                            content: gettextCatalog.getString(BALLOT_SELECT_TO_SIGN_MSG_ID),
                        },
                    }),
                );
                return;
            }

            if (ballotsToSign.length <= 0) {
                new CustomDialog()
                    .title(gettextCatalog.getString(BALLOT_SIGNING_MSG_ID))
                    .cannotPerform(gettextCatalog.getString(BALLOT_MSG_ID))
                    .show();
                return;
            }

            if (!$scope.isAdminAuthorityActivated()) {
				const p = $mdDialog.show(
					$mdDialog.customConfirm({
						locals: {
							title: gettextCatalog.getString(BALLOT_SIGNING_MSG_ID),
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
                                content: gettextCatalog.getString(ADMIN_BOARD_WRONG_ACTIVATED_EXPLANATION_MSG_ID)
									+ ' ' + electionEvent.defaultTitle + '.'
                            },
                        }),
                    );
                    return;
                }
            }

            $scope.ballotApprovalResults = [];
            $scope.ballotApprovalError = false;
			const privateKeyBase64 = sessionService.getSelectedAdminBoard().privateKey;

			$q.allSettled(
                ballotsToSign.map(function (ballot) {
					const url = (endpoints.host() + endpoints.ballotSign)
						.replace(electionEventIdPattern, $scope.selectedElectionEventId)
						.replace(ballotIdPattern, ballot.id);

					const body = {
						privateKeyPEM: privateKeyBase64,
					};

					return $http.put(url, body);
                }),
            ).then(function (responses) {
				const settled = settler.settle(responses);
				$scope.ballotApprovalResults = settled.fulfilled;

                if (settled.ok) {
                    $mdToast.show(
                        toastCustom.topCenter(
                            gettextCatalog.getString(BALLOT_SIGNED_MSG_ID),
                            'success',
                        ),
                    );
                    $scope.listBallots();
                }
                if (settled.error) {
                    showBallotApprovalError();
                }
            });

            $scope.unselectAll();
        };

        $scope.unselectAll = function () {
            $scope.selectAll = false;
            $scope.ballots.result.forEach(function (ballot) {
                ballot.selected = false;
            });
        };

        $scope.onSelectAll = function (value) {
            $scope.ballots.result.forEach(function (ballot) {
                ballot.selected = value;
            });
        };

        $scope.updateSelectAll = function (value) {
            if (!value) {
                $scope.selectAll = false;
            }
        };

        $scope.$on('refresh-ballots', function () {
            $scope.listBallots();
            $scope.selectAll = false;
        });

        $scope.capitalizeFirstLetter = function (string) {
			const lowerCaseString = string.toLowerCase();
			return lowerCaseString.charAt(0).toUpperCase() + lowerCaseString.slice(1);
        };

        $scope.isThereNoBallotSelected = function () {
			let noBallotSelected = true;

			if ($scope.ballots) {
                $scope.ballots.result.forEach(function (ballot) {
                    if (ballot.selected) {
                        noBallotSelected = false;
                    }
                });
            }

            return noBallotSelected;
        };

        $scope.getTextIsBallotSelected = function () {
			const check = $scope.isThereNoBallotSelected();
			return check ? $scope.selectBallotText() : '';
        };

        //initialize && populate view
        // -------------------------------------------------------------
        $scope.alert = '';
        $scope.errors = {};
        $scope.selectedElectionEventId = sessionService.getSelectedElectionEvent().id;
        $scope.listBallots();
    });
