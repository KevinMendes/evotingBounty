/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/*jshint maxlen: 1800 */
(function () {
    'use strict';

    angular
        .module('reconstructMembers', [])
        .controller('reconstructMembers', function (
            $scope,
            $mdDialog,
            sessionService,
            endpoints,
            $http,
            $interval,
            $timeout,
            $mdToast,
            toastCustom,
            gettextCatalog,
            boardActivation,
        ) {
        	const adminBoardIdPattern = '{adminBoardId}';

            function setCaret(x, delay) {
                if (!delay) {
                    delay = 0;
                }
                $timeout(function () {
                    try {
                        document.getElementById('pin' + x).focus();
                    } catch (e) {
                        // ignore
                    }
                }, delay);
            }

            $scope.pin1_keypress = function (ev) {
                if (ev && ev.which === 13) {
                    $scope.validatePin();
                }
            };

            function clearPin() {
                $scope.pin = {value: ''};
                $scope.confirmPin = {value: ''};
            }

            const delay = 1000;
			let currentStep = 0;
			$scope.initiallySelected = false;
            $scope.selectedMember = {};
            clearPin();
            $scope.members = {};
            $scope.wizardStep = 'introduceCard';
            $scope.doingWizard = true;
            $scope.initializingAuthority = true;
            $scope.numberOfSuccessfullyWrittenSmartCards = 0;
            $scope.urlStatus = '';
            $scope.urlWriteShare = '';
            $scope.twoStepsClosing = false;

            $scope.activateBoardMessage = function () {
                return gettextCatalog
                    .getString(
                        'To activate the Board please insert the smart card for each member of the',
                    )
                    .concat(' ')
                    .concat($scope.selectedAuthority.defaultTitle);
            };

			const isNotReady = function () {
				if (!$scope.selectedAuthority) {
					$scope.initializingAuthority = false;
					return $scope.initializingAuthority;
				}

				$scope.initializingAuthority = !$scope.selectedAuthority.ready;
				return $scope.initializingAuthority;
			};

			const simulateFirstClick = function () {
				const initialPoll = $interval(function () {
					if (!isNotReady() && !$scope.initiallySelected) {
						$scope.initiallySelected = true;
						$scope.select($scope.listOfMembers[0]);
						$interval.cancel(initialPoll);
					}
				}, 500);
			};

			const initialize = function () {
				let index = 0;
				$scope.listOfMembers = boardActivation.getListOfMembers();
				$scope.sharesType = boardActivation.getSharesType();
				$scope.selectedAuthority = boardActivation.getSelectedAuthority();
				$scope.selectedElectionEventId = sessionService.getSelectedElectionEvent().id;

				$scope.listOfMembers.forEach(function (member) {
					$scope.members[member] = {
						id: member,
						status: false,
						index: index,
						share: undefined,
					};
					index++;
				});

				isNotReady();

				simulateFirstClick();

				if ($scope.sharesType === 'adminBoard') {
					$scope.urlStatus = endpoints.checkAdminBoardShareStatus;
					$scope.urlActivateAuthority = endpoints.activateAdminBoardShare.replace(
						adminBoardIdPattern,
						$scope.selectedAuthority.id,
					);
					$scope.urlReadShare = endpoints.readAdminBoardShare.replace(
						adminBoardIdPattern,
						$scope.selectedAuthority.id,
					);
					$scope.urlReconstructAuthority = endpoints.reconstructAdminBoardShare.replace(
						adminBoardIdPattern,
						$scope.selectedAuthority.id,
					);
				}
			};

			// Initializing the status of each member
            initialize();

			const STEPS = [
				'introduceCard',
				'reading',
				'introducePIN',
				'recovering',
				'extractCard',
			];

			const REQUESTS = {
				introduceCard: $scope.urlStatus,
				reading: null,
				introducePIN: null,
				recovering: $scope.urlReadShare,
				extractCard: $scope.urlStatus,
			};

			// jshint maxcomplexity:10
			const executeWizard = function () {
				let postRecoveringCalled = false;

				const poll = $interval(function () {
					$scope.doingWizard = true;
					sessionService.setMembersPoll(poll);

					let requestURL;

					if ($scope.wizardStep === STEPS[0]) {
						// actualState = introduceCard

						requestURL = REQUESTS[STEPS[currentStep]];

						$http.get(endpoints.host() + requestURL).then(function (response) {
							const data = response.data;

							if (data === 'INSERTED') {
								// only change state when it's inserted
								currentStep++;
								$scope.wizardStep = STEPS[currentStep];
								setCaret('1', 1000);
							}
						});
					} else if ($scope.wizardStep === STEPS[1]) {
						// actualState = reading

						// mocked state - changing state immediately
						currentStep++;
						$scope.wizardStep = STEPS[currentStep];
					} else if ($scope.wizardStep === STEPS[2]) {
						// actualState = introducePIN

						if ($scope.selectedMemberValidPin) {
							currentStep++;
							$scope.wizardStep = STEPS[currentStep];
						}
					} else if ($scope.wizardStep === STEPS[3]) {
						// actualState = recovering

						if (!postRecoveringCalled) {
							postRecoveringCalled = true;
							const shareNumber = $scope.members[$scope.selectedMember].index;

							requestURL = REQUESTS[STEPS[currentStep]];
							requestURL = requestURL.replace('{shareNum}', shareNumber);

							const body = {
								pin: $scope.pin.value,
								publicKeyPEM: boardActivation.getBoardIssuerPublicKey(),
							};

							$http.post(endpoints.host() + requestURL, body).then(
								function (response) {
									const data = response.data;
									$scope.members[$scope.selectedMember].share =
										data.serializedShare;
									currentStep++;
									$scope.wizardStep = STEPS[currentStep];
								},
								function () {
									$mdToast.show(
										toastCustom.topCenter(
											gettextCatalog.getString(
												'An error occurred during share recovering',
											),
											'error',
										),
									);
									$interval.cancel(poll);
									sessionService.setMembersPoll({});
									$scope.select($scope.selectedMember);
								},
							);
						}
					} else if ($scope.wizardStep === STEPS[STEPS.length - 1]) {
						// actualState = extractCard

						requestURL = REQUESTS[STEPS[currentStep]];

						$http.get(endpoints.host() + requestURL).then(function (response) {
							const data = response.data;

							if (data === 'EMPTY') {
								// only change state when the reader is empty

								$scope.setToDone();
								$interval.cancel(poll);
								sessionService.setMembersPoll({});
							}
						});
					}
				}, delay);
			};

			$scope.select = function (member) {
                if ($scope.wizardStep !== STEPS[STEPS.length - 1]) {
                    clearPin();
                    $scope.selectedMember = member;
                    $scope.selectedMemberValidPin = false;
                    currentStep = 0;
                    $scope.wizardStep = STEPS[0];

                    /*check if the user clicked recently one member and other in short time to avoid problems*/
                    if ($scope.doingWizard) {
                        $scope.doingWizard = false;
                        if (!_.isEmpty(sessionService.getMembersPoll())) {
                            $interval.cancel(sessionService.getMembersPoll());
                            sessionService.setMembersPoll({});
                        }
                        executeWizard();
                        setCaret('1', 2000);
                    }
                } else {
                    $mdToast.show(
                        toastCustom.topCenter(
                            gettextCatalog.getString(
                                'You need to extract the smartcard after clicking a new member',
                            ),
                            'error',
                        ),
                    );
                    return false;
                }
            };

            $scope.cancel = function () {
                if (
                    $scope.sharesType === 'adminBoard' &&
                    $scope.initializingAuthority &&
                    !$scope.keystoreOK
                ) {
                    $scope.twoStepsClosing = false;
                    $interval.cancel(sessionService.getMembersPoll());
                    $mdDialog.cancel();
                    return;
                }
                if ($scope.twoStepsClosing) {
                    $interval.cancel(sessionService.getMembersPoll());
                    if ($scope.sharesType === 'adminBoard') {
                        sessionService.setSelectedAdminBoard($scope.selectedAuthority);
                        sessionService.expireAfter('ab', $scope.selectedAuthority);
                    }

                    $mdDialog.cancel();
                } else {
                    $scope.twoStepsClosing = true;
                }
            };

            $scope.discard = function () {
                $scope.twoStepsClosing = false;
            };

            $scope.validatePin = function () {
                if ($scope.pin.value.length === 6) {
                    if (!isNaN($scope.pin.value)) {
                        $scope.selectedMemberValidPin = true;
                        return true;
                    } else {
                        $mdToast.show(
                            toastCustom.topCenter(
                                gettextCatalog.getString(
                                    'The password should contain only numeric characters',
                                ),
                                'error',
                            ),
                        );
                        return false;
                    }
                } else {
                    $mdToast.show(
                        toastCustom.topCenter(
                            gettextCatalog.getString(
                                'The password should contain 6 numeric characters',
                            ),
                            'error',
                        ),
                    );
                    return false;
                }
            };

            $scope.setToDone = function () {
                $scope.members[$scope.selectedMember].status = true;
                $scope.numberOfSuccessfullyWrittenSmartCards++;

                clearPin();

                sessionService.setNumberOfSuccessfullyWrittenSmartCards(
                    $scope.numberOfSuccessfullyWrittenSmartCards,
                );

				const index = $scope.listOfMembers.indexOf($scope.selectedMember);

				if (index < $scope.listOfMembers.length - 1) {
                    if (!$scope.members[$scope.listOfMembers[index + 1]].status) {
                        $scope.wizardStep = STEPS[0];
                        $scope.select($scope.listOfMembers[index + 1]);
                    } else {
                        findNextMemberToSelect();
                    }
                } else {
                    findNextMemberToSelect();
                }
            };

            /* Function that finds the next member of the admin board pendant to be constituted*/

            function findNextMemberToSelect() {
				let i = 0;
				const len = $scope.listOfMembers.length;
				for (; i < len; i++) {
                    if (!$scope.members[$scope.listOfMembers[i]].status) {
                        $scope.wizardStep = STEPS[0];
                        $scope.select($scope.listOfMembers[i]);
                        break;
                    }
                }
            }

            $scope.setToFailed = function () {
                $scope.members[$scope.selectedMember].status = false;
                clearPin();
            };

            $scope.activateBoard = function () {
                // Stop all shares reading
                if (sessionService.hasValidMembersPoll()) {
                    $interval.cancel(sessionService.getMembersPoll());
                    sessionService.setMembersPoll({});
                }

				const requestURL = $scope.urlReconstructAuthority;

				const serializedShares = [];
				$scope.listOfMembers.forEach(function (member) {
					const share = $scope.members[member].share;
					if (share) {
                        serializedShares.push(share);
                    }
                });
				const body = {
					serializedShares: serializedShares,
					serializedPublicKey: boardActivation.getBoardSubjectPublicKey(),
				};

				$http.post(endpoints.host() + requestURL, body).then(
                    function (response) {
                        const data = response.data;
                        $scope.selectedAuthority.privateKey = data.serializedPrivateKey;
                        sessionService.expireAfter('eb', $scope.selectedAuthority);
                        $mdDialog.hide($scope.dialogPromise);
                    },
                    function () {
                        $mdToast.show(
                            toastCustom.topCenter(
                                gettextCatalog.getString(
                                    'An error occured during key activation',
                                ),
                                'error',
                            ),
                        );
                    },
                );
            };
        });
})();
