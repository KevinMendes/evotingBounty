/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
(function () {
    'use strict';

    angular
        .module('members', [])
        .controller('members', function (
            $scope,
            $mdDialog,
            sessionService,
            endpoints,
            $http,
            $interval,
            $timeout,
            $mdToast,
            gettextCatalog,
            generateAdminBoardCert,
            toastCustom,
        ) {
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
                    setCaret(2);
                }
            };

            $scope.pin2_keypress = function (ev) {
                if (ev && ev.which === 13) {
                    $scope.validatePin();
                }
            };

            function clearPin() {
                $scope.pin = {
                    value: '',
                };
                $scope.confirmPin = {
                    value: '',
                };
            }

			const delay = 1000;
			let currentStep = 0;
			$scope.initiallySelected = false;
            $scope.initializingAuthority = true;
            $scope.doingWizard = true;
            $scope.selectedMember = {};
            clearPin();
            $scope.members2DoneStatus = {};
            $scope.members2Index = {};
            $scope.members = [];
            $scope.wizardStep = 'introduceCard';
            $scope.numberOfSuccessfullyWrittenSmartCards = 0;
            $scope.urlStatus = '';
            $scope.urlWriteShare = '';
            $scope.password = '';
            $scope.keystoreOK = false;

            $scope.selectedFile = '';
            $scope.twoStepsClosing = false;

            $scope.closeDialog = function () {
                $interval.cancel(sessionService.getMembersPoll());
                $mdDialog.cancel();
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
                    $mdDialog.cancel();
                } else {
                    $scope.twoStepsClosing = true;
                }
            };

            $scope.discard = function () {
                $scope.twoStepsClosing = false;
            };

            //!$scope.selectedAuthority.ready
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
				$scope.listOfMembers.forEach(function (member) {
					$scope.members.push(member);
					$scope.members2DoneStatus[member] = false;
					$scope.members2Index[member] = index;
					index++;
				});

				isNotReady();

				simulateFirstClick();

				if ($scope.sharesType === 'adminBoard') {
					$scope.urlStatus = endpoints.checkAdminBoardShareStatus;
					$scope.urlWriteShare = endpoints.writeAdminBoardShare.replace(
						'{adminBoardId}',
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
				'writing',
				'extractCard',
			];

			const REQUESTS = {
				introduceCard: $scope.urlStatus,
				reading: null,
				introducePIN: null,
				writing: $scope.urlWriteShare,
				extractCard: $scope.urlStatus,
			};

			// jshint maxcomplexity:8
			const executeWizard = function () {
				let postWritingCalled = false;

				if (!_.isEmpty(sessionService.getMembersPoll())) {
					$interval.cancel(sessionService.getMembersPoll());
					sessionService.setMembersPoll({});
				}

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
								setCaret(1, 1000);
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
						// actualState = writing

						if (!postWritingCalled) {
							postWritingCalled = true;
							const shareNumber = $scope.members2Index[$scope.selectedMember];

							requestURL = REQUESTS[STEPS[currentStep]];
							requestURL = requestURL.replace('{shareNum}', shareNumber);

							const body = {
								pin: $scope.pin.value,
							};

							$http.post(endpoints.host() + requestURL, body).then(
								function () {
									currentStep++;
									$scope.wizardStep = STEPS[currentStep];
								},
								function () {
									$mdToast.show(
										toastCustom.topCenter(
											gettextCatalog.getString(
												'An error occurred during share writing',
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

            $scope.validatePin = function () {
                if ($scope.pin.value.length === 6) {
                    if (!isNaN($scope.pin.value)) {
                        if (!isNaN($scope.confirmPin.value)) {
                            if ($scope.pin.value === $scope.confirmPin.value) {
                                $scope.selectedMemberValidPin = true;
                                return true;
                            } else {
                                $mdToast.show(
                                    toastCustom.topCenter(
                                        gettextCatalog.getString('The passwords do not match'),
                                        'error',
                                    ),
                                );
                                return false;
                            }
                        } else {
                            $mdToast.show(
                                toastCustom.topCenter(
                                    gettextCatalog.getString(
                                        'The confirmation password should contain only numeric characters',
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

            $scope.constituteMembersDescription = function () {
                if ($scope.selectedAuthority) {
                    return gettextCatalog
                        .getString(
                            'To constitute the Board please record the smartcard for each member of the',
                        )
                        .concat(' ')
                        .concat($scope.selectedAuthority.defaultTitle);
                }
            };

            $scope.setToDone = function () {
                $scope.members2DoneStatus[$scope.selectedMember] = true;
                $scope.numberOfSuccessfullyWrittenSmartCards++;

                clearPin();

                sessionService.setNumberOfSuccessfullyWrittenSmartCards(
                    $scope.numberOfSuccessfullyWrittenSmartCards,
                );

                if (
                    $scope.numberOfSuccessfullyWrittenSmartCards ===
                    $scope.listOfMembers.length
                ) {
                    $mdDialog.hide($scope.dialogPromise);
                } else {
					const index = $scope.listOfMembers.indexOf($scope.selectedMember);

					if (index < $scope.listOfMembers.length - 1) {
                        if (!$scope.members2DoneStatus[$scope.members[index + 1]]) {
                            $scope.wizardStep = STEPS[0];
                            $scope.select($scope.listOfMembers[index + 1]);
                        } else {
                            findNextMemberToSelect();
                        }
                    } else {
                        findNextMemberToSelect();
                    }
                }
            };

            /* Function that finds the next member of the admin board pendant to be constituted*/

            function findNextMemberToSelect() {
				let i = 0;
				const len = $scope.members.length;
				for (; i < len; i++) {
                    if (!$scope.members2DoneStatus[$scope.members[i]]) {
                        $scope.wizardStep = STEPS[0];
                        $scope.select($scope.listOfMembers[i]);
                        break;
                    }
                }
            }

            /* Only applies if a keystore has to be introduced to constitute the Board */
            $scope.filesChanged = function (elm) {
                $scope.files = elm.files;
                $scope.$apply();
                $scope.selectedFile = elm.files[0].name;
            };

            $scope.confirmSelection = function () {
                $scope.errorInKeystore = false;

				const successCallback = function successCallback() {
					$scope.selectedAuthority.ready = true;

					$scope.keystoreOK = true;
					sessionService.setSelectedAdminBoard($scope.selectedAuthority);
				};

				const errorCallback = function errorCallback(e) {
					console.log(e);
					$scope.errorInKeystore = true;
				};
				const id = $scope.selectedAuthority.id;

				const keystoreData = {
					file: $scope.files[0],
					keystorePassword: $scope.password,
					callbackOK: successCallback,
					callbackKO: errorCallback,
				};
				if (id.length > 0) {
                    try {
						const url =
							endpoints.host() +
							endpoints.constituteAdminBoard.replace('{adminBoardId}', id);

						generateAdminBoardCert.upload(url, keystoreData);
                    } catch (e) {
                        $scope.errorInKeystore = true;
                    }
                }
            };

            $scope.noFileOrPasswordSelected = function () {
                if (!$scope.password || !$scope.files) {
                    return true;
                }
                return false;
            };
        });
})();
