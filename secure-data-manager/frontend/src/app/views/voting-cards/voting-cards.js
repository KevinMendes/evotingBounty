/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/*jshint maxparams: 13 */
/*jshint maxlen: 1800 */
angular
    .module('voting-cards', [])
    .controller('voting-cards', function (
        $scope,
        $rootScope,
        $mdDialog,
        $mdToast,
        toastCustom,
        sessionService,
        endpoints,
        $http,
        $q,
        settler,
        $timeout,
        votingCardSetService,
        CustomDialog,
        jobqueue,
        gettextCatalog,
        ErrorsDict,
        statusBox,
        boardActivation,
        activeFilters,
        configElectionConstants,
        _,
    ) {
        'use strict';

        const refreshVotingCardSetsState = 'refresh-voting-card-sets';
        const electionEventIdPattern = '{electionEventId}';
        const votingCardSetIdPattern = '{votingCardSetId}';

        const VOTING_CARDS_SELECTION_MSG_ID = 'Voting cards selection';
        const VOTING_CARD_SETS_SIGNING_MSG_ID = 'Voting card sets signing';
        const CUSTOM_FILES_MSG_ID = 'Custom files';


        $scope.isCollapsed = false;

        // manage status filters
        $scope.filterItem = 'votingCards';
        $scope.filterTabs = [
            configElectionConstants.STATUS_LOCKED,
            configElectionConstants.STATUS_PRECOMPUTED,
            configElectionConstants.STATUS_COMPUTED,
            configElectionConstants.STATUS_VCS_DOWNLOADED,
            configElectionConstants.STATUS_GENERATED,
            configElectionConstants.STATUS_SIGNED,
        ];
        $scope.onTabSelected = function (filter) {
            $scope.filterActive = filter.text;
            $scope.tableFilter = filter.code;
            activeFilters.setActiveFilter($scope.filterItem, filter);
            $scope.unselectAll();
        };

        // init status filter
        if (!activeFilters.getActiveFilter($scope.filterItem)) {
            activeFilters.setActiveFilter($scope.filterItem, $scope.filterTabs[0]);
        }
        $scope.filterActive = activeFilters.getActiveFilter($scope.filterItem).text;
        $scope.tableFilter = activeFilters.getActiveFilter($scope.filterItem).code;

        $scope.data = {};
        $scope.progress = function (id) {
            return jobqueue.getJobStatus(id);
        };
        $scope.batches = function (types) {
            return jobqueue.getBatches(types);
        };
        $scope.batchesTotals = function (types) {
            return jobqueue.getBatchesTotals(types);
        };

		const updateView = _.debounce(function () {
			$rootScope.$broadcast(refreshVotingCardSetsState);
		}, 3000);

		const updateProgress = function (alias) {
			updateView();
		};

		/**
		 * Out of the selected voting card sets, gather a list of the ones in a
		 * particular state, and warn if the gathering does not succeed
		 * completely.
		 *
		 * @param {string} status the status the selected voting card sets must
		 *                        be in, in order to be selected
		 *
		 * @returns a promise, successful if there are chosen voting card sets.
		 */
		const findVotingCardSetsWithStatus = function (status) {
			const deferred = $q.defer();

			const chosenVotingCardSets = [];
			const selectedVotingCardSets = [];
			// Put all selected voting cards with the selected status in a list.
			$scope.data.votingCardSets.result.forEach(function (votingCardSet) {
				if (votingCardSet.selected) {
					selectedVotingCardSets.push(votingCardSet);
					if (votingCardSet.status === status) {
						chosenVotingCardSets.push(votingCardSet);
					}
				}
			});

			if (selectedVotingCardSets.length < 1) {
				// No selected voting card sets.
				$mdDialog
					.show(
						$mdDialog.customAlert({
							locals: {
								title: gettextCatalog.getString(VOTING_CARDS_SELECTION_MSG_ID),
								content: gettextCatalog.getString(
									'Please, select some {{status}} voting cards sets',
									{status: status},
								),
							},
						}),
					)
					.then(null, function () {
						deferred.reject(
							gettextCatalog.getString('No selected voting card sets'),
						);
					});
			} else if (chosenVotingCardSets.length < 1) {
				// Some selected voting card sets, but none in the desired status.
				new CustomDialog()
					.title(gettextCatalog.getString(VOTING_CARDS_SELECTION_MSG_ID))
					.cannotPerform(gettextCatalog.getString('Voting Card Set(s)'))
					.show()
					.then(null, function () {
						deferred.reject('No chosen voting card sets');
					});
			} else if (chosenVotingCardSets.length < selectedVotingCardSets.length) {
				// Less chosen voting card sets than selected ones, meaning
				// that not all voting cards sets can be processed. Only those
				// in the desired stated will be processed.
				$mdDialog
					.show(
						$mdDialog.customConfirm({
							locals: {
								title: gettextCatalog.getString(VOTING_CARDS_SELECTION_MSG_ID),
								content: gettextCatalog.getString(
									'This action will only be performed on {{status}} voting cards sets',
									{status: status},
								),
								ok: gettextCatalog.getString('Continue'),
							},
						}),
					)
					.then(function () {
						// Warn, but still provide the chosen voting card sets.
						deferred.resolve(chosenVotingCardSets);
					});
			} else {
				deferred.resolve(chosenVotingCardSets);
			}

			return deferred.promise;
		};

		const showVotincCardSetTransitionError = function () {
			$mdDialog.show(
				$mdDialog.customAlert({
					locals: {
						title: gettextCatalog.getString('Voting Card Set Error'),
						content: gettextCatalog.getString(
							'Current operation failed for some Voting Card Set(s), please review your list',
						),
					},
				}),
			);
		};

		/**
		 * Run the precomputation process.
		 *
		 * @param {array} votingCardSets the voting card sets to transition.
		 * @param {string} newStatus the new status to assign
		 * @param {object} additional options for the call
		 * @return a promise resolved once all voting card sets have been processed.
		 */
		const transitionVotingCardSets = function (
			votingCardSets,
			newStatus,
			options,
		) {
			// Start precomputation for each of the voting card sets.
			votingCardSets.forEach(function (votingCardSet) {
				// Set 'in progress' status.
				votingCardSet.processing = true;
				votingCardSetService
					.changeStatus(votingCardSet, newStatus, options)
					.then(
						function (success) {
							// Remove the processing flag.
							delete votingCardSet.processing;
							// Set the voting card set's new status
							votingCardSet.status = newStatus;
							// Unselect the finished voting card set, which has
							// the desired side effect of updating the view.
							votingCardSet.selected = false;
							// Refresh VCs list after status change to update tab counters
							$rootScope.$broadcast(refreshVotingCardSetsState);
						},
						function (failure) {
							// Remove the processing flag.
							delete votingCardSet.processing;
							// Show error message
							showVotincCardSetTransitionError();
						},
					);
			});
		};

		/**
		 * Checks whether the admin board is activated, allows activating it if needed.
		 *
		 * @returns a promise
		 */
		const checkAdminBoardIsActive = function () {
			const deferred = $q.defer();

			// ... ensure the admin board is activated.
			let privateKeyBase64 = null;

			if (sessionService.getSelectedAdminBoard()) {
				privateKeyBase64 = sessionService.getSelectedAdminBoard().privateKey;
			}

			if (!$scope.isAdminAuthorityActivated() || !privateKeyBase64) {
				// It is not, ask the user to activate it.
				$mdDialog
					.show(
						$mdDialog.customConfirm({
							locals: {
								title: gettextCatalog.getString('Precomputation'),
								content: gettextCatalog.getString(
									'Please, activate the administration board.',
								),
								ok: gettextCatalog.getString('Activate now'),
							},
						}),
					)
					.then(
						function (ok) {
							// OK, proceed with actication.
							boardActivation.init('adminBoard');
							boardActivation.adminBoardActivate().then(function (response) {
								if (response && response.data && response.data.error !== '') {
									$mdToast
										.show(
											toastCustom.topCenter(
												gettextCatalog.getString(
													'The Administration Board could not be activated',
												),
												'error',
											),
										)
										.then(null, function () {
											deferred.reject(response.data.error);
										});
								} else {
									boardActivation.openBoardAdmin().then(
										function (success) {
											// The admin board is now activated.
											deferred.resolve();
										},
										function (error) {
											// The admin board could not be activated.
											deferred.reject(error);
										},
									);
								}
							});
						},
						function (cancel) {
							// Cancelled, do not activate.
							deferred.reject(cancel);
						},
					);
			} else {
				// It is activated.
				deferred.resolve();
			}

			return deferred.promise;
		};

		$scope.listVotingCardSets = function () {
            $scope.errors.votingCardSetsFailed = false;
			const url =
				endpoints.host() +
				endpoints.votingCardSets.replace(
					electionEventIdPattern,
					$scope.selectedElectionEventId,
				);
			$http.get(url).then(
                function (response) {
                    const data = response.data;
                    try {
                        sessionService.setVotingCardSets(data);
                        $scope.data.votingCardSets = data;
                        $scope.statusCount = _.countBy(data.result, 'status');
                    } catch (e) {
                        $scope.data.message = e.message;
                        $scope.errors.votingCardSetsFailed = true;
                        $scope.statusCount = null;
                    }
                },
                function () {
                    $scope.errors.votingCardSetsFailed = true;
                    $scope.statusCount = null;
                },
            );

			const electoralAuthoritiesURL =
				endpoints.host() +
				endpoints.electoralAuthorities.replace(
					electionEventIdPattern,
					$scope.selectedElectionEventId,
				);

			$http.get(electoralAuthoritiesURL).then(function (response) {
                const data = response.data;
                $scope.electoralAuthorities = data.result;
                sessionService.setElectoralAuthorities(data.result);
            });

			const ballotBoxesURL =
				endpoints.host() +
				endpoints.ballotboxes.replace(
					electionEventIdPattern,
					$scope.selectedElectionEventId,
				);
			$http.get(ballotBoxesURL).then(function (response) {
                const data = response.data;
                sessionService.setBallotBoxes(data);
                $scope.ballotBoxes = data;
            });
        };

		const showVotingCardsGenerationError = function () {
			new CustomDialog()
				.title(gettextCatalog.getString('Voting cards generation'))
				.error()
				.show();
		};

        $scope.precomputeVotingCardSets = function (ev) {
            // Get all relevant voting card sets.
            findVotingCardSetsWithStatus('LOCKED').then(function (votingCardSets) {
                // Ensure the admin board is active.
                checkAdminBoardIsActive().then(function (success) {
                    // Launch precomputation.
					const adminBoard = sessionService.getSelectedAdminBoard();
					transitionVotingCardSets(votingCardSets, 'PRECOMPUTED', {
                        privateKeyPEM: adminBoard.privateKey,
                        adminBoardId: adminBoard.id,
                    });
                });
            });
        };

        $scope.computeVotingCardSet = function (ev) {
            findVotingCardSetsWithStatus('PRECOMPUTED').then(function (
                votingCardSets,
            ) {
                // Launch computation.
                transitionVotingCardSets(votingCardSets, 'COMPUTING');
            });
        };

        $scope.downloadVotingCardSet = function (ev) {
            findVotingCardSetsWithStatus('COMPUTED').then(function (votingCardSets) {
                // Start downloading.
                transitionVotingCardSets(votingCardSets, 'VCS_DOWNLOADED');
            });
        };

        $scope.customFilter = function (votingCardSet) {
            //If the VCS has been sent to compute, we keep it in precomputed tab since
            //the "COMPUTING" status does not have its own tab
			const selectedTab = $scope.tableFilter;
			const status = votingCardSet.status;
			return (
                status === selectedTab ||
                (status === 'COMPUTING' && selectedTab === 'PRECOMPUTED')
            );
        };

        $scope.generateVotingCardSet = function (ev) {
            $scope.errors = {};

            findVotingCardSetsWithStatus('VCS_DOWNLOADED').then(function (
                votingCardsToGenerate,
            ) {
                // signal a new batch of actions

                jobqueue.batch(
                    $rootScope.batchSuccessSummary,
                    $rootScope.batchErrorSummary,
                );

                // generate the voting cards

                $scope.generateVotingCardSetError = false;

                // check if a process for this entity is already running

				const removed = _.remove(votingCardsToGenerate, function (votingCardSet) {
					const status = jobqueue.getJobStatus(votingCardSet.id);
					return status && !status.completed && !status.error;
				});

				if (votingCardsToGenerate.length <= 0) {
                    $mdDialog.show(
                        $mdDialog.customAlert({
                            locals: {
                                title: gettextCatalog.getString('Voting cards generation'),
                                content: gettextCatalog.getString(
                                    'All selected voting card sets are in progress.',
                                ),
                            },
                        }),
                    );
                    return;
                }

                if (removed.length > 0) {
                    $mdToast.show(
                        toastCustom.topCenter(
                            gettextCatalog.getString(
                                'Some of the selected voting card sets are in progress.',
                            ),
                            'error',
                        ),
                    );
                }

                $mdToast.show(
                    toastCustom.topCenter(
                        gettextCatalog.getString(
                            'Voting cards submitted for generation...',
                        ),
                        'success',
                    ),
                );

				let errorAlreadyShown = false;

				$q.allSettled(
                    votingCardsToGenerate.map(function (votingCardSet) {
						const url = endpoints.votingCardSet
							.replace(electionEventIdPattern, $scope.selectedElectionEventId)
							.replace(votingCardSetIdPattern, votingCardSet.id);

						return $http.post(endpoints.host() + url).then(
                            function success(data) {
                                // submit job

								const jobId = data.data.result;
								jobqueue.job(
                                    votingCardSet.id,
                                    jobId,
                                    'votingcardsets',
                                    votingCardSet.alias,
                                    updateProgress,
                                );
                            },
                            function error() {
                                $scope.errors.generateVotingCardSetError = true;
                                if (!errorAlreadyShown) {
                                    errorAlreadyShown = true;
                                    showVotingCardsGenerationError();
                                }
                            },
                        );
                    }),
                );
                $scope.unselectAll();
            });
        };

        $scope.$on(refreshVotingCardSetsState, function () {
            $scope.listVotingCardSets();
            $scope.selectAll = false;
        });

        $scope.capitalizeFirstLetter = function (string) {
			const lowerCaseString = string.toLowerCase();
			return lowerCaseString.charAt(0).toUpperCase() + lowerCaseString.slice(1);
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
						title: gettextCatalog.getString(VOTING_CARD_SETS_SIGNING_MSG_ID),
						content: gettextCatalog.getString(
							'Some voting card set(s) could not be signed. Please review the list.',
						),
					},
				}),
			);
		};

		$scope.sign = function () {
			const votingCardSetsToSign = [];
			const votingCardSetsSelected = [];
			$scope.data.votingCardSets.result.forEach(function (votingCardSet) {
                if (votingCardSet.selected && votingCardSet.status === 'GENERATED') {
                    votingCardSetsToSign.push(votingCardSet);
                }
                if (votingCardSet.selected) {
                    votingCardSetsSelected.push(votingCardSet);
                }
            });

            if (votingCardSetsSelected.length <= 0) {
                $mdDialog.show(
                    $mdDialog.customAlert({
                        locals: {
                            title: gettextCatalog.getString(VOTING_CARD_SETS_SIGNING_MSG_ID),
                            content: gettextCatalog.getString(
                                'Please, select some voting card sets to sign from the list.',
                            ),
                        },
                    }),
                );
                return;
            }

            if (votingCardSetsToSign.length <= 0) {
                new CustomDialog()
                    .title(gettextCatalog.getString(VOTING_CARD_SETS_SIGNING_MSG_ID))
                    .cannotPerform(gettextCatalog.getString('Voting Card Set(s)'))
                    .show();
                return;
            }

            if (!$scope.isAdminAuthorityActivated()) {
				const p = $mdDialog.show(
					$mdDialog.customConfirm({
						locals: {
							title: gettextCatalog.getString(VOTING_CARD_SETS_SIGNING_MSG_ID),
							content: gettextCatalog.getString(
								'Please, activate the administration board.',
							),
							ok: gettextCatalog.getString('Activate now'),
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
                                        'Error to open the adminboard',
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
                                title: gettextCatalog.getString('Wrong Administration Board activated'),
                                content: gettextCatalog.getString('The active Administration Board does not belong to the Election Event that you a' +
                                    're trying to operate. Please deactivate it and activate the corresponding Administration Board for the Electio' +
                                    'n Event') + ' ' + electionEvent.defaultTitle + '.'
                            },
                        }),
                    );
                    return;
                }
            }

			const privateKeyBase64 = sessionService.getSelectedAdminBoard().privateKey;

			$q.allSettled(
                votingCardSetsToSign.map(function (vcs) {
					const url = (endpoints.host() + endpoints.votingCardSetSign)
						.replace(electionEventIdPattern, $scope.selectedElectionEventId)
						.replace(votingCardSetIdPattern, vcs.id);

					const body = {
						status: 'SIGNED',
						privateKeyPEM: privateKeyBase64,
					};

					return $http.put(url, body);
                }),
            ).then(function (responses) {
				const settled = settler.settle(responses);
				$scope.vcsResults = settled.fulfilled;

                if (settled.ok) {
                    $mdToast.show(
                        toastCustom.topCenter(
                            gettextCatalog.getString('Voting card set(s) signed!'),
                            'success',
                        ),
                    );
                    $scope.listVotingCardSets();
                    $scope.unselectAll();
                }
                if (settled.error) {
                    showBallotApprovalError();
                }
            });
        };

        $scope.isThereNoVCSSelected = function () {
			let noVCSSelected = true;

			if ($scope.data.votingCardSets) {
                $scope.data.votingCardSets.result.forEach(function (votingCardSet) {
                    if (votingCardSet.selected) {
                        noVCSSelected = false;
                    }
                });
            }

            return noVCSSelected;
        };

        $scope.getSelectVCSText = function () {
			const text = gettextCatalog.getString(
				'Please select first a Voting card set',
			);

			const check = $scope.isThereNoVCSSelected();
			return check ? text : '';
        };

        $scope.unselectAll = function () {
            $scope.selectAll = false;
            $scope.data.votingCardSets.result.forEach(function (votingCardSet) {
                votingCardSet.selected = false;
            });
        };

        $scope.onSelectAll = function (value) {
            $scope.data.votingCardSets.result.forEach(function (votingCardSet) {
                const status = activeFilters.getActiveFilter($scope.filterItem).code;
                if (votingCardSet.status === status) {
                    votingCardSet.selected = value;
                }
            });
        };

        $scope.updateSelectAll = function (value) {
            if (!value) {
                $scope.selectAll = false;
            }
        };

        $scope.generatePreVotingOutputsEnabled = function () {
            return sessionService.isGeneratePreVotingOutputsEnabled();
        };

        $scope.vcPrecomputationEnabled = function () {
            return sessionService.isVcPrecomputationEnabled();
        };

        $scope.vcComputationEnabled = function () {
            return sessionService.isVcComputationEnabled();
        };

        $scope.vcDownloadEnabled = function () {
            return sessionService.isVcDownloadEnabled();
        };

        $scope.generatePreVotingOutputs = function () {
            function internalRequest(privateKeyBase64) {
				const url =
					endpoints.host() +
					endpoints.generatePreVotingOutputs.replace(
						electionEventIdPattern,
						$scope.selectedElectionEventId,
					);
				const body = {
					privateKeyInBase64: privateKeyBase64,
				};

				$http
                    .post(url, body)
                    .then(function (res) {
                        $mdToast.show(
                            toastCustom.topCenter(
                                gettextCatalog.getString('Custom files generated successfully'),
                                'success',
                            ),
                        );
                        $scope.unselectAll();
                    })
                    .catch(function (e) {
                        if (e.data.error == '4005') {
                            $mdDialog.show(
                                $mdDialog.customAlert({
                                    locals: {
                                        title: gettextCatalog.getString(CUSTOM_FILES_MSG_ID),
                                        content: gettextCatalog.getString(ErrorsDict(e.data.error)),
                                    },
                                }),
                            );
                        } else {
                            $mdToast.show(
                                toastCustom.topCenter(
                                    gettextCatalog.getString(CUSTOM_FILES_MSG_ID) +
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
                    });
            }

			let privateKeyBase64 = null;
			if (sessionService.getSelectedAdminBoard()) {
                privateKeyBase64 = sessionService.getSelectedAdminBoard().privateKey;
            }

            if (!$scope.isAdminAuthorityActivated() || !privateKeyBase64) {
                $mdDialog.show(
                    $mdDialog.customAlert({
                        locals: {
                            title: gettextCatalog.getString(CUSTOM_FILES_MSG_ID),
                            content: gettextCatalog.getString(
                                'Please, activate the administration board',
                            ),
                        },
                    }),
                );
                return;
            } else {
				const electionEvent = sessionService.getSelectedElectionEvent();
				if (!sessionService.doesActivatedABBelongToSelectedEE()) {
                    $mdDialog.show(
                        $mdDialog.customAlert({
                            locals: {
                                title: gettextCatalog.getString('Wrong Administration Board activated'),
                                content: gettextCatalog.getString('The active Administration Board does not belong to the Election Event that you a' +
                                    're trying to operate. Please deactivate it and activate the corresponding Administration Board for the Electio' +
                                    'n Event') + ' ' + electionEvent.defaultTitle + '.'
                            },
                        }),
                    );
                    return;
                }
            }

			const p = $mdDialog.show(
				$mdDialog.customConfirm({
					locals: {
						title: gettextCatalog.getString(CUSTOM_FILES_MSG_ID),
						content: gettextCatalog.getString('All VCS will be processed'),
						ok: gettextCatalog.getString('Generate'),
					},
				}),
			);
			p.then(function () {
                internalRequest(privateKeyBase64);
            });
        };

        //initialize && populate view
        // -------------------------------------------------------------
        $scope.alert = '';
        $scope.errors = {};
        $scope.selectedElectionEventId = sessionService.getSelectedElectionEvent().id;
        $scope.electoralAuthorities = [];
        $scope.ballotBoxes = [];
        $scope.listVotingCardSets();
        $scope.statusBox = statusBox;
    })
    .filter('elapsed', function (gettextCatalog) {
        'use strict';
        return function (time) {
            if (!time) {
                return gettextCatalog.getString('Estimating');
            }

			const seconds = Math.floor(time / 1000);
			let minutes = Math.floor(seconds / 60);
			const minutesRest = Math.floor(seconds % 60);
			let hours = Math.floor(minutes / 60);
			const hoursRest = Math.floor(minutes % 60);
			let days = Math.floor(hours / 24);
			const daysRest = Math.floor(hours % 24);

			if (minutes && minutesRest) {
                minutes++;
            }

            if (hours && hoursRest) {
                hours++;
            }

            if (days && daysRest) {
                days++;
            }

            if (days > 1) {
                return days + ' ' + gettextCatalog.getString('days');
            } else if (hours > 1) {
                return hours + ' ' + gettextCatalog.getString('hours');
            } else if (hours === 1) {
                return 1 + ' ' + gettextCatalog.getString('hour');
            } else if (minutes > 1) {
                return minutes + ' ' + gettextCatalog.getString('minutes');
            } else if (minutes === 1) {
                return 1 + ' ' + gettextCatalog.getString('minute');
            } else {
                return seconds + ' ' + gettextCatalog.getString('seconds');
            }
        };
    });
