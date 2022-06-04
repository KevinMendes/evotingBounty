/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/*jshint maxparams: 12 */
angular
	.module('ballot-boxes', ['app.dialogs'])
	.controller('ballot-boxes', function (
		$scope,
		$mdDialog,
		$mdToast,
		toastCustom,
		sessionService,
		endpoints,
		$templateCache,
		$http,
		$q,
		settler,
		CustomDialog,
		jobqueue,
		gettextCatalog,
		$rootScope,
		ErrorsDict,
		statusBox,
		boardActivation,
		activeFilters,
		configElectionConstants,
		_
	) {
		'use strict';

		let privateKeyBase64 = null;
		let electoralAuthorityId;

		const electionEventIdPattern = '{electionEventId}';
		const ballotBoxIdPattern = '{ballotBoxId}';
		const ballotBoxStatusPattern = '{bbStatus}';

		const BALLOT_BOX_SELECT_MSG_ID = 'Please select first a Ballot box';
		const BALLOT_BOX_TEST_MSG_ID = 'Test';
		const BALLOT_BOX_REGULAR_MSG_ID = 'Regular';
		const BALLOT_BOX_DOWNLOAD_MSG_ID = 'Ballot box download';
		const BALLOT_BOX_SELECT_DOWNLOAD_MSG_ID = 'Please, select some ballot box(es) to be download from the list';
		const BALLOT_BOX_DOWNLOADED_MSG_ID = 'Ballot box(es) downloaded!';
		const BALLOT_BOX_MIX_MSG_ID = 'Ballot box mix';
		const BALLOT_BOX_SELECT_MIX_MSG_ID = 'Please, select some ballot box(es) to be mixed from the list';
		const BALLOT_BOX_MIXED_MSG_ID = 'Mixing started!';
		const BALLOT_BOX_MIX_START_ERROR_MSG_ID = 'Failed to start mixing of ballot boxes';
		const BALLOT_BOX_MIX_ERROR_MSG_ID = 'Failed to send ballot box(es) for mixing';
		const BALLOT_BOX_MSG_ID = 'Ballot Box(es)';
		const BALLOT_BOX_DECRYPTING_MSG_ID = 'Ballot Box decrypting';
		const BALLOT_BOX_SELECT_DECRYPTING_MSG_ID = 'Please, select some ballot boxes to decrypt from the list';
		const BALLOT_BOX_SELECT_SINGLE_EA_MSG_ID = 'Please, select ballot boxes for a single electoral authority';
		const BALLOT_BOX_DECRYPTING_COMPLETE_MSG_ID = 'Decrypting complete!';
		const BALLOT_BOX_DECRYPTING_STARTED_MSG_ID = 'Decrypting started';
		const BALLOT_BOX_SIGNING_MSG_ID = 'Ballot Box signing';
		const BALLOT_BOX_SELECT_SIGNING_MSG_ID = 'Please, select a ballot box to sign from the list';
		const BALLOT_BOX_SIGNED_MSG_ID = 'Ballot box(es) signed!';
		const BALLOT_BOX_SAME_STATUS_TO_BE_PROCESSED_MSG_ID = 'All the ballot boxes to be processed must have the same status';
		const BALLOT_BOX_SAME_STATUS_AS_SELECTED_MSG_ID = 'Please note that all ballot boxes with the same status as the selected ones will be processed';
		const BALLOT_BOX_GENERATE_OK_MSG_ID = 'Generate';
		const ADMIN_BOARD_ACTIVATE_MSG_ID = 'Please, activate the administration board.';
		const ADMIN_BOARD_ACTIVATE_OK_MSG_ID = 'Activate now';
		const ADMIN_BOARD_ACTIVATION_ERROR_MSG_ID = 'The Administration Board could not be activated';
		const CUSTOM_FILES_MSG_ID = 'Custom files';
		const CUSTOM_FILES_GENERATED_MSG_ID = 'Custom files generated successfully';
		const CONTACT_SUPPORT_MSG_ID = 'Something went wrong. Contact with Support';
		const ERROR_CODE_MSG_ID = 'Error code';


		$scope.isCollapsed = false;

		// manage status filters
		$scope.filterItem = 'ballotBoxes';
		$scope.filterTabs = [
			configElectionConstants.STATUS_LOCKED,
			configElectionConstants.STATUS_READY,
			configElectionConstants.STATUS_SIGNED,
			configElectionConstants.STATUS_MIXED,
			configElectionConstants.STATUS_BB_DOWNLOADED,
			configElectionConstants.STATUS_DECRYPTED
		];
		$scope.onTabSelected = function (filter) {
			$scope.filterActive = filter.text;
			$scope.tableFilter = filter.code;
			activeFilters.setActiveFilter($scope.filterItem, filter);
			$scope.typeCount = $scope.countTestInFilterActive(filter);
			$scope.unselectAll();
		};

		// manage test filter
		$scope.filterBBTest = 'ballotBoxesTest';
		$scope.filterTestTabs = [
			configElectionConstants.TEST_REGULAR,
			configElectionConstants.TEST_TEST,
		];
		$scope.onTestSelected = function (filter) {
			$scope.filterActiveTest = filter.text;
			$scope.tableFilterTest = filter.code;
			activeFilters.setActiveFilter($scope.filterBBTest, filter);
			$scope.typeCount = $scope.countTestInFilterActive(
				activeFilters.getActiveFilter($scope.filterItem),
			);
			$scope.unselectAll();
		};
		$scope.parseFilterActiveTest = function (filter) {
			return filter === 'Test' ? 'true' : 'false';
		};
		$scope.countTestInFilterActive = function (filter) {
			const testStatus = {};
			testStatus.false = _.filter($scope.ballotBoxes.result, {
				status: filter.code,
				test: 'false',
			}).length;
			testStatus.true = _.filter($scope.ballotBoxes.result, {
				status: filter.code,
				test: 'true',
			}).length;
			return testStatus;
		};

		// init status filter
		if (!activeFilters.getActiveFilter($scope.filterItem)) {
			activeFilters.setActiveFilter($scope.filterItem, $scope.filterTabs[0]);
		}
		$scope.filterActive = activeFilters.getActiveFilter($scope.filterItem).text;
		$scope.tableFilter = activeFilters.getActiveFilter($scope.filterItem).code;

		// init test filter
		if (!activeFilters.getActiveFilter($scope.filterBBTest)) {
			activeFilters.setActiveFilter(
				$scope.filterBBTest,
				$scope.filterTestTabs[0],
			);
		}
		$scope.filterActiveTest = activeFilters.getActiveFilter(
			$scope.filterBBTest,
		).text;
		$scope.tableFilterTest = activeFilters.getActiveFilter(
			$scope.filterBBTest,
		).code;

		$scope.selectBallotBoxText = '';

		$scope.progress = function (id) {
			return jobqueue.getJobStatus(id);
		};
		$scope.batches = function (types) {
			return jobqueue.getBatches(types);
		};
		$scope.batchesTotals = function (types) {
			return jobqueue.getBatchesTotals(types);
		};

		$scope.listBallotBoxes = function () {
			$scope.errors.ballotBoxesFailed = false;
			$scope.errors.getElectoralAuthoritiesFailed = false;

			const ballotBoxesURL =
				endpoints.host() +
				endpoints.ballotboxes.replace(
					electionEventIdPattern,
					$scope.selectedElectionEventId,
				);
			$http.get(ballotBoxesURL).then(
				function (response) {
					const data = response.data;
					try {
						sessionService.setBallotBoxes(data);
						$scope.ballotBoxes = data;
						$scope.statusCount = _.countBy(data.result, 'status');
						$scope.typeCount = $scope.countTestInFilterActive(
							activeFilters.getActiveFilter($scope.filterItem),
						);
					} catch (e) {
						$scope.data.message = e.message;
						$scope.errors.ballotBoxesFailed = true;
						$scope.statusCount = null;
						$scope.typeCount = null;
					}
				},
				function () {
					$scope.errors.ballotBoxesFailed = true;
					$scope.statusCount = null;
					$scope.typeCount = null;
				},
			);

			const electoralAuthoritiesURL =
				endpoints.host() +
				endpoints.electoralAuthorities.replace(
					electionEventIdPattern,
					$scope.selectedElectionEventId,
				);

			$http.get(electoralAuthoritiesURL).then(
				function (response) {
					const data = response.data;
					try {
						$scope.electoralAuthorities = data.result;
						sessionService.setElectoralAuthorities(data.result);
					} catch (e) {
						$scope.data.message = e.message;
						$scope.errors.getElectoralAuthoritiesFailed = true;
					}
				},
				function () {
					$scope.errors.getElectoralAuthoritiesFailed = true;
				},
			);
		};

		// download ballotBox(es)
		// -------------------------------------------------------------

		const showBallotBoxDownloadError = function () {
			new CustomDialog()
				.title(gettextCatalog.getString(BALLOT_BOX_DOWNLOAD_MSG_ID))
				.error()
				.show();
		};

		$scope.downloadBallotBox = function (ev) {
			$scope.errors.ballotBoxDownloadError = false;

			// collect ballot box to download

			const ballotBoxesToDownload = [];
			const ballotBoxesSelected = [];
			$scope.ballotBoxes.result.forEach(function (ballotBox) {
				if (ballotBox.selected && ballotBox.status === 'MIXED') {
					ballotBoxesToDownload.push(ballotBox);
				} else if (
					ballotBox.selected &&
					ballotBox.status === 'SIGNED' &&
					ballotBox.synchronized === 'true' &&
					ballotBox.test === 'true'
				) {
					ballotBoxesToDownload.push(ballotBox);
				}
				if (ballotBox.selected) {
					ballotBoxesSelected.push(ballotBox);
				}
			});

			// no selection?

			if (ballotBoxesSelected.length <= 0) {
				$mdDialog.show(
					$mdDialog.customAlert({
						locals: {
							title: gettextCatalog.getString(BALLOT_BOX_DOWNLOAD_MSG_ID),
							content: gettextCatalog.getString(BALLOT_BOX_SELECT_DOWNLOAD_MSG_ID),
						},
					}),
				);
				return;
			}

			if (ballotBoxesToDownload.length <= 0) {
				new CustomDialog()
					.title(gettextCatalog.getString(BALLOT_BOX_DOWNLOAD_MSG_ID))
					.cannotPerform(gettextCatalog.getString(BALLOT_BOX_MSG_ID))
					.show();
				return;
			}

			// download individual ballot box

			$scope.ballotBoxDownloadResults = [];
			$scope.ballotBoxDownloadError = false;

			$q.allSettled(
				ballotBoxesToDownload.map(function (ballotBox) {
					const url = (endpoints.host() + endpoints.ballotbox)
						.replace(electionEventIdPattern, $scope.selectedElectionEventId)
						.replace(ballotBoxIdPattern, ballotBox.id);
					return $http.post(url);
				})
			).then(function (responses) {
				const settled = settler.settle(responses);
				if (settled.ok) {
					$scope.ballotBoxDownloadResults = settled.fulfilled.map(function (
						response,
					) {
						return response.data.result;
					});
					if (!$scope.checkOkBecauseExtraFastClick(settled.fulfilled)) {
						$mdToast.show(
							toastCustom.topCenter(
								gettextCatalog.getString(BALLOT_BOX_DOWNLOADED_MSG_ID),
								'success',
							),
						);
					}
				}
				if (settled.error) {
					$scope.errors.ballotBoxDownloadError = true;
					showBallotBoxDownloadError();
				}
				$scope.listBallotBoxes();
				$scope.unselectAll();
			});
		};

		$scope.mixBallotBoxes = function () {
			$scope.errors.ballotBoxMixError = false;
			$scope.mixingOnGoing = true;

			// Collect ballot box to mix.
			const ballotBoxesToMix = [];
			const ballotBoxesSelected = [];
			$scope.ballotBoxes.result.forEach(function (ballotBox) {
				if (ballotBox.selected && ballotBox.status === 'SIGNED' && ballotBox.synchronized === 'true') {
					ballotBoxesToMix.push(ballotBox.id);
				}
				if (ballotBox.selected) {
					ballotBoxesSelected.push(ballotBox);
				}
			});

			// no selection?

			if (ballotBoxesSelected.length <= 0) {
				$mdDialog.show(
					$mdDialog.customAlert({
						locals: {
							title: gettextCatalog.getString(BALLOT_BOX_MIX_MSG_ID),
							content: gettextCatalog.getString(BALLOT_BOX_SELECT_MIX_MSG_ID),
						},
					}),
				);
				$scope.mixingOnGoing = false;
				return;
			}

			if (ballotBoxesToMix.length <= 0) {
				new CustomDialog()
					.title(gettextCatalog.getString(BALLOT_BOX_MIX_MSG_ID))
					.cannotPerform(gettextCatalog.getString(BALLOT_BOX_MSG_ID))
					.show();
				$scope.mixingOnGoing = false;
				return;
			}

			// Mix ballot boxes.
			$scope.ballotBoxMixResults = [];
			$scope.ballotBoxMixError = false;

			const url = (endpoints.host() + endpoints.mixBallotBoxes).replace(electionEventIdPattern, $scope.selectedElectionEventId);

			$http.post(url, ballotBoxesToMix).then(function (response) {
				$scope.mixingOnGoing = false;
				$scope.ballotBoxMixResults = response.data;

				// Check if starting the mixing process for some ballot box failed.
				let hasError = false;
				$scope.ballotBoxMixResults.forEach(ballotBoxResult => {
					if ($scope.getMixingStatus(ballotBoxResult.ballotBoxId) === 'ERROR') {
						console.log("Mixing error: " + ballotBoxResult);
						hasError = true;
					}
				});

				if (hasError) {
					new CustomDialog()
						.title(gettextCatalog.getString(BALLOT_BOX_MIX_START_ERROR_MSG_ID))
						.error()
						.show();
				} else {
					// Mixing started successfully for all ballots.
					$mdToast.show(
						toastCustom.topCenter(
							gettextCatalog.getString(BALLOT_BOX_MIXED_MSG_ID),
							'success',
						),
					);
				}

				$scope.listBallotBoxes();
				$scope.unselectAll();
			}, function () {
				$scope.mixingOnGoing = false;
				$mdToast.show(
					toastCustom.topCenter(
						gettextCatalog.getString(BALLOT_BOX_MIX_ERROR_MSG_ID),
						'error',
					),
				);
			});
		};

		$scope.getMixingStatus = function (ballotBoxId) {
			const found = $scope.getMixingResult(ballotBoxId);
			return found ? found.processStatus : "";
		};

		$scope.getMixingErrorMessage = function (ballotBoxId) {
			const found = $scope.getMixingResult(ballotBoxId);
			return found && found.errorMessage ? ": " + found.errorMessage : "";
		};

		$scope.getMixingResult = function (ballotBoxId) {
			if ($scope.ballotBoxMixResults) {
				return $scope.ballotBoxMixResults.find(function (ballotBox) {
					return ballotBox.ballotBoxId === ballotBoxId;
				});
			}
			return undefined;
		};

		$scope.confirmMixing = function (ev) {
			$scope.inputMixingConfirmationCode = '';

			// Generate a simple 5-digit confirmation code.
			$scope.expectedMixingConfirmationCode = (new Date().getMilliseconds().toString() + new Date().getSeconds().toString()).padStart(5, "0");

			$mdDialog.show({
				controller: DialogController,
				template: $templateCache.get('app/views/ballot-boxes/confirm-mixing.html'),
				parent: angular.element(document.body),
				targetEvent: ev,
				clickOutsideToClose: false,
				escapeToClose: false,
				scope: $scope,
				preserveScope: true
			}).then(function () {
				if ($scope.expectedMixingConfirmationCode === $scope.inputMixingConfirmationCode) {
					$scope.mixBallotBoxes();
				}
			}, function () {
				$scope.inputMixingConfirmationCode = '';
			});
		};

		$scope.unselectAll = function () {
			$scope.selectAll = false;
			$scope.ballotBoxes.result.forEach(function (ballotBox) {
				ballotBox.selected = false;
			});
		};

		$scope.onSelectAll = function (value) {
			$scope.ballotBoxes.result.forEach(function (ballotBox) {
				const status = activeFilters.getActiveFilter($scope.filterItem).code;
				const test = activeFilters.getActiveFilter($scope.filterBBTest).code;
				if (ballotBox.status === status && ballotBox.test === test) {
					ballotBox.selected = value;
				}
			});
		};

		$scope.updateSelectAll = function (value) {
			if (!value) {
				$scope.selectAll = false;
			}
		};

		$scope.$on('refresh-ballot-boxes', function () {
			$scope.listBallotBoxes();
			$scope.selectAll = false;
		});

		$scope.decrypt = function () {
			// collect ballot boxes to decrypt
			const boxesToDecrypt = [];
			const boxesSelected = [];

			$scope.ballotBoxes.result.forEach(function (ballotBox) {
				if (ballotBox.selected && ballotBox.status === 'BB_DOWNLOADED') {
					boxesToDecrypt.push(ballotBox);
				}
				if (ballotBox.selected) {
					boxesSelected.push(ballotBox);
				}
			});

			// no selection?

			if (boxesSelected.length <= 0) {
				$mdDialog.show(
					$mdDialog.customAlert({
						locals: {
							title: gettextCatalog.getString(BALLOT_BOX_DECRYPTING_MSG_ID),
							content: gettextCatalog.getString(BALLOT_BOX_SELECT_DECRYPTING_MSG_ID),
						},
					}),
				);
				return;
			}

			if (boxesToDecrypt.length <= 0) {
				new CustomDialog()
					.title(gettextCatalog.getString(BALLOT_BOX_DECRYPTING_MSG_ID))
					.cannotPerform(gettextCatalog.getString(BALLOT_BOX_MSG_ID))
					.show();
				return;
			}

			// all from the same authority?

			electoralAuthorityId = boxesToDecrypt[0].electoralAuthority.id;
			let same = true;
			boxesToDecrypt.forEach(function (ballotBox) {
				same = same && ballotBox.electoralAuthority.id === electoralAuthorityId;
			});
			if (!same) {
				$mdDialog.show(
					$mdDialog.customAlert({
						locals: {
							title: gettextCatalog.getString(BALLOT_BOX_DECRYPTING_MSG_ID),
							content: gettextCatalog.getString(BALLOT_BOX_SELECT_SINGLE_EA_MSG_ID),
						},
					}),
				);
				return;
			}

			privateKeyBase64 = null;

			if (sessionService.getSelectedAdminBoard()) {
				privateKeyBase64 = sessionService.getSelectedAdminBoard().privateKey;
			}

			if (!$scope.isAdminAuthorityActivated() || !privateKeyBase64) {
				const p = $mdDialog.show(
					$mdDialog.customConfirm({
						locals: {
							title: gettextCatalog.getString(BALLOT_BOX_DECRYPTING_MSG_ID),
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
									mix();
								});
							}
						});
					},
					function (error) {
						//Not possible to open the $mdDialog
					},
				);

			} else {
				mix();
			}

			function mix() {
				$scope.selectedAuthority = boardActivation.getSelectedAuthority();

				$q.allSettled(
					boxesToDecrypt.map(function (ballotBox) {
						const url =
							endpoints.host() +
							endpoints.mixing
								.replace(electionEventIdPattern, $scope.selectedElectionEventId)
								.replace(ballotBoxIdPattern, ballotBox.id);
						return $http.post(url);
					}),
				).then(function (responses) {
					const settled = settler.settle(responses);
					if (settled.ok) {
						if (!$scope.checkOkBecauseExtraFastClick(settled.fulfilled)) {
							$mdToast.show(
								toastCustom.topCenter(
									gettextCatalog.getString(BALLOT_BOX_DECRYPTING_COMPLETE_MSG_ID),
									'success',
								),
							);
						}
					}
					if (settled.error) {
						new CustomDialog()
							.title(gettextCatalog.getString(BALLOT_BOX_DECRYPTING_MSG_ID))
							.error()
							.show();
					}
					$scope.listBallotBoxes();
				});

				$mdToast.show(
					toastCustom.topCenter(
						gettextCatalog.getString(BALLOT_BOX_DECRYPTING_STARTED_MSG_ID),
						'success',
					),
				);
				$scope.unselectAll();
			}
		};

		function capitalizeWord(string) {
			const lowerCaseString = string.toLowerCase();
			return lowerCaseString.charAt(0).toUpperCase() + lowerCaseString.slice(1);
		}

		$scope.capitalizeFirstLetter = function (string) {
			const words = string.split('_');
			const out = [];
			angular.forEach(words, function (word) {
				out.push(capitalizeWord(word));
			});
			return out.join(' ');
		};

		$scope.isAdminAuthorityActivated = function () {
			const adminBoard = sessionService.getSelectedAdminBoard();
			if (!adminBoard) {
				return false;
			}
			return adminBoard.privateKey;
		};

		const showSigningError = function () {
			new CustomDialog()
				.title(gettextCatalog.getString(BALLOT_BOX_SIGNING_MSG_ID))
				.error()
				.show();
		};

		$scope.sign = function () {
			const ballotBoxesToSign = [];
			const ballotBoxesSelected = [];
			$scope.ballotBoxes.result.forEach(function (bb) {
				if (bb.selected && bb.status === 'READY') {
					ballotBoxesToSign.push(bb);
				}
				if (bb.selected) {
					ballotBoxesSelected.push(bb);
				}
			});

			if (ballotBoxesSelected.length <= 0) {
				$mdDialog.show(
					$mdDialog.customAlert({
						locals: {
							title: gettextCatalog.getString(BALLOT_BOX_SIGNING_MSG_ID),
							content: gettextCatalog.getString(BALLOT_BOX_SELECT_SIGNING_MSG_ID),
						},
					}),
				);
				return;
			}

			if (ballotBoxesToSign.length <= 0) {
				new CustomDialog()
					.title(gettextCatalog.getString(BALLOT_BOX_SIGNING_MSG_ID))
					.cannotPerform(gettextCatalog.getString(BALLOT_BOX_MSG_ID))
					.show();
				return;
			}

			if (!$scope.isAdminAuthorityActivated()) {
				const p = $mdDialog.show(
					$mdDialog.customConfirm({
						locals: {
							title: gettextCatalog.getString(BALLOT_BOX_SIGNING_MSG_ID),
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
			}

			privateKeyBase64 = sessionService.getSelectedAdminBoard().privateKey;

			$q.allSettled(
				ballotBoxesToSign.map(function (bb) {
					const url = (endpoints.host() + endpoints.ballotBoxSign)
						.replace(electionEventIdPattern, $scope.selectedElectionEventId)
						.replace(ballotBoxIdPattern, bb.id);

					const body = {
						privateKeyPEM: privateKeyBase64,
					};

					return $http.put(url, body);
				}),
			).then(function (responses) {
				const settled = settler.settle(responses);
				$scope.ballotBoxesResults = settled.fulfilled;

				if (settled.ok) {
					$mdToast.show(
						toastCustom.topCenter(
							gettextCatalog.getString(BALLOT_BOX_SIGNED_MSG_ID),
							'success',
						),
					);
					$scope.listBallotBoxes();
				}
				if (settled.error) {
					showSigningError();
				}
			});

			$scope.unselectAll();
		};

		$scope.isThereNoBallotBoxSelected = function () {
			let generatedBallotBoxSelected = false;

			if ($scope.ballotBoxes) {
				$scope.ballotBoxes.result.forEach(function (bb) {
					if (bb.selected) {
						generatedBallotBoxSelected = true;
					}
				});
			}

			return !generatedBallotBoxSelected;
		};

		$scope.getTextByBallotBoxSelected = function () {
			const selectBallotBoxText = gettextCatalog.getString(BALLOT_BOX_SELECT_MSG_ID);
			const check = $scope.isThereNoBallotBoxSelected();
			return check ? selectBallotBoxText : '';
		};

		/**
		 * Check if the responses might be ok because a extra fast double click in  an action
		 * @param responses
		 * @returns {boolean}
		 */
		$scope.checkOkBecauseExtraFastClick = function (responses) {
			responses.forEach(function (r) {
				if (r.idle) {
					return true;
				}
			});
			return false;
		};

		/*
				ballot test get descriptiontext
			 */
		$scope.getBallotBoxTypeDesc = function (val) {
			const ballotBoxTestDesc = gettextCatalog.getString(BALLOT_BOX_TEST_MSG_ID);
			const ballotBoxRegularDesc = gettextCatalog.getString(BALLOT_BOX_REGULAR_MSG_ID);
			return val === 'true' ? ballotBoxTestDesc : ballotBoxRegularDesc;
		};

		$scope.generatePostVotingOutputsEnabled = function () {
			return sessionService.isGeneratePostVotingOutputsEnabled();
		};

		$scope.generatePostVotingOutputs = function () {
			const boxesSelected = _.filter($scope.ballotBoxes.result, 'selected');

			function internalRequest(privateKeyBase64) {
				const url =
					endpoints.host() +
					endpoints.generatePostVotingOutputs
						.replace(electionEventIdPattern, $scope.selectedElectionEventId)
						.replace(ballotBoxStatusPattern, boxesSelected[0].status.toLowerCase());
				const body = {
					privateKeyInBase64: privateKeyBase64,
				};

				$http
					.post(url, body)
					.then(function (res) {
						$mdToast.show(
							toastCustom.topCenter(
								gettextCatalog.getString(CUSTOM_FILES_GENERATED_MSG_ID),
								'success',
							),
						);
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
									gettextCatalog.getString(CONTACT_SUPPORT_MSG_ID) +
									'. ' +
									gettextCatalog.getString(ERROR_CODE_MSG_ID) +
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

			privateKeyBase64 = null;
			if (sessionService.getSelectedAdminBoard()) {
				privateKeyBase64 = sessionService.getSelectedAdminBoard().privateKey;
			}

			if (!$scope.isAdminAuthorityActivated() || !privateKeyBase64) {
				const adminBoardDialog = $mdDialog.show(
					$mdDialog.customConfirm({
						locals: {
							title: gettextCatalog.getString(CUSTOM_FILES_MSG_ID),
							content: gettextCatalog.getString(ADMIN_BOARD_ACTIVATE_MSG_ID),
							ok: gettextCatalog.getString(ADMIN_BOARD_ACTIVATE_OK_MSG_ID),
						},
					}),
				);
				adminBoardDialog.then(
					function (success) {
						boardActivation.init('adminBoard');
						boardActivation.adminBoardActivate().then(function (response) {
							if (response && response.data && response.data.error != '') {
								$mdToast.show(
									toastCustom.topCenter(gettextCatalog.getString(ADMIN_BOARD_ACTIVATION_ERROR_MSG_ID), 'error'),
								);
							} else {
								boardActivation.openBoardAdmin().then(function (success) {
									$scope.generatePostVotingOutputs();
								});
							}
						});
					},
					function (error) {
						//Not possible to open the $mdDialog
					},
				);
				return;
			}

			const sameStatusBB = boxesSelected.every((val, i, arr) => val.status === arr[0].status);

			if (!sameStatusBB) {
				new CustomDialog()
					.title(
						gettextCatalog.getString(BALLOT_BOX_SAME_STATUS_TO_BE_PROCESSED_MSG_ID),
					)
					.show();
				return;
			}

			const statusSelected = boxesSelected[0].status;

			const boxesWithStatus = _.filter($scope.ballotBoxes.result, {
				status: statusSelected,
			});

			if (boxesWithStatus.length > boxesSelected.length) {
				const ballotBoxesDialog = $mdDialog.show(
					$mdDialog.customConfirm({
						locals: {
							title: gettextCatalog.getString(CUSTOM_FILES_MSG_ID),
							content: gettextCatalog.getString(BALLOT_BOX_SAME_STATUS_AS_SELECTED_MSG_ID),
							ok: gettextCatalog.getString(BALLOT_BOX_GENERATE_OK_MSG_ID)
						},
					}),
				);
				ballotBoxesDialog.then(function () {
					internalRequest(privateKeyBase64);
				});
			} else {
				internalRequest(privateKeyBase64);
			}
		};

		$scope.isThereNoBBSelectedWithAppropiateStatus = function () {
			if ($scope.ballotBoxes) {
				const boxesSelected = _.filter($scope.ballotBoxes.result, 'selected');
				if (boxesSelected.length === 0) {
					return true;
				}
				for (let i = 0; i < boxesSelected.length; ++i) {
					if (
						boxesSelected[i].status === 'LOCKED' ||
						boxesSelected[i].status === 'READY' ||
						boxesSelected[i].status === 'SIGNED' ||
						boxesSelected[i].status === 'CLOSED'
					) {
						return true;
					}
				}
			}

			return false;
		};

//initialize && populate view
// -------------------------------------------------------------

		$scope.alert = '';
		$scope.errors = {};
		$scope.selectedElectionEventId = sessionService.getSelectedElectionEvent().id;
		$scope.electoralAuthorities = [];
		$scope.listBallotBoxes();
		$scope.statusBox = statusBox;
	})
;
