/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global require */

angular
	.module('app', [
		'lodash',
		'routes',
		'ngMaterial',
		'ngPromiseExtras',
		'settler',
		'partials',
		'gettext',
		'ngFileUpload',
		'pathConverter',

		//  'menu',

		// filters
		'filters',

		// directives
		'svSidebar',
		'ovToolbar',
		'ovFolderInputDirective',
		'app.errors.dict',
		'datatableData',
		'ui.bootstrap.collapse',
		'tabBar',

		//services
		'app.sessionService',
		'endpoints',
		'databaseService',
		'app.dialogs',
		'app.progressbar',
		'i18n',
		'generateAdminBoardCert',
		'jobqueue',
		'statusBox',
		'ballotprinter',
		'toastCustom',
		'dialogsCustom',
		'boardActivation',
		'votingCardSet',
		'entitiesCounterService',
		'configElectionConstants',
		'activeFilters',

		// controllers
		'splash',
		'home-manage',
		'election-event-list',
		'administration-board-list',
		'election-event-manage',
		'ballots',
		'ballot-boxes',
		'voting-cards',
		'electoral-authorities',
		'members',
		'reconstructMembers',
	])

	.config(function ($compileProvider, $provide) {
		'use strict';
		$provide.constant('$MD_THEME_CSS', '');
	})

	.run(function (
		$rootScope,
		$state,
		databaseService,
		sessionService,
		i18n,
		gettextCatalog,
		$mdDialog,
		$mdToast,
		toastCustom,
		$http,
		endpoints,
	) {
		'use strict';

		// safe apply

		$rootScope.safeApply = function (fn) {
			const phase = this.$root.$$phase;
			if (phase === '$apply' || phase === '$digest') {
				if (fn && typeof fn === 'function') {
					fn();
				}
			} else {
				this.$apply(fn);
			}
		};

		// ----------------------------------------------------------------------------------
		// language management

		$rootScope.getLanguageUrl = function (code) {
			return endpoints.host() + endpoints.languages + code;
		}

		$rootScope.switchLanguage = function (lang) {
			$rootScope.lang = lang.code;

			$rootScope.langAttr = $rootScope.transformLanguageCode(lang.code, true);

			gettextCatalog.setCurrentLanguage(lang.code);
			if (lang.code !== 'xx') {
				// 'xx' is the default (pseudo english) base lang
				//gettextCatalog.loadRemote('assets/lang/' + lang.code + '.json');
				const langUrl = $rootScope.getLanguageUrl(lang.code);
				gettextCatalog.loadRemote(langUrl);
			}
		};

		const setLanguages = function (defaultLanguage, languages) {
			$rootScope.i18n = {
				languages: languages,
				lang: {
					code: defaultLanguage,
				},
			};
		};
		$rootScope.transformLanguageCode = function (code, countryFormat) {
			// specify countryFormat to true if you want to add the country code. e.g. 'en-US'
			// set to false if you just need the lang code. e.g. 'en'
			try {
				let langCode;
				if (countryFormat) {
					langCode = code.replace('_', '-');
				} else {
					langCode = code.split('_')[0];
				}
				return langCode;
			} catch (e) {
				return '';
			}
		};

		//-----------------------------------------------------------------------------------
		// user config

		$rootScope.getConfig = function () {
			$http.get(endpoints.host() + endpoints.sdmConfig).then(function (response) {
				const data = response.data;
				setLanguages(data.config.i18n.default, data.config.i18n.languages);
				$rootScope.switchLanguage($rootScope.i18n.lang);

				sessionService.setGeneratePreVotingOutputsEnabled(
					data.config.generatePreVotingOutputsEnabled,
				);
				sessionService.setGeneratePostVotingOutputsEnabled(
					data.config.generatePostVotingOutputsEnabled,
				);
				sessionService.setVcPrecomputationEnabled(
					data.config.vcPrecomputationEnabled,
				);
				sessionService.setVcComputationEnabled(
					data.config.vcComputationEnabled,
				);
				sessionService.setVcDownloadEnabled(data.config.vcDownloadEnabled);
				sessionService.setImportExportEnabled(data.config.importExportEnabled);

				// we go to the default page once the promise is a success
				// this is to prevent view blinking
				$state.go('election-event-list');
			});
		};

		// ----------------------------------------------------------------------------------
		// (spring-)batch summary success and error callbacks

		$rootScope.batchErrorSummary = function (batchType, aliases) {
			let title;
			switch (batchType) {
				case 'votingcardsets':
					title = gettextCatalog.getString(
						'Some Voting Cards have not been generated successfully. Please contact support.',
					);
					break;
				case 'decryption':
					title = gettextCatalog.getString('Some Ballot Boxes have not been decrypted successfully. Please contact support.');
					break;
				default:
					title = `${gettextCatalog.getString('Some errors were encountered during the process. Please contact support.')} (${batchType})`;
			}
			$mdDialog.show(
				$mdDialog.customAlert({
					locals: {
						title: title,
						content: aliases.join(', '),
					},
				}),
			);
		};

		$rootScope.batchSuccessSummary = function (batchType, count) {
			let msg;
			switch (batchType) {
				case 'votingcardsets':
					msg = gettextCatalog.getString('Voting Cards generated!');
					break;
				case 'decryption':
					msg = gettextCatalog.getString('Ballot boxes decrypted!');
					break;
				default:
					msg = 'unknown entities processed'; // shouldn't happen
			}
			$mdToast.show(
				toastCustom.topCenter(msg, 'success'),
			);
		};

		// ----------------------------------------------------------------------------------
		// startup

		if (typeof require !== 'undefined') {

			const gui = require('nw.gui');
			const win = gui.Window.get();
			const fs = require('fs');
			const argv = gui.App.argv;
			let i, frontendOnly = false;

			// Check cmd line arguments
			if (argv && argv.length) {
				for (i = 0; i < argv.length; i++) {
					switch (argv[i]) {
						case '--expireAB':
							sessionService.setKeyExpiration('ab', argv[i + 1]);
							break;
						case '--expireEA':
							sessionService.setKeyExpiration('eb', argv[i + 1]);
							break;
						case '--frontend-only':
							frontendOnly = true;
							break;
					}
				}
			}

			// Simple close application method without backend servers and database handling
			const simpleApplicationClose = function () {
				win.on('close', function () {
					const obj = this;
					obj.hide();
					obj.close(true);
				});
			}

			// Get the SDM home directory
			const getSdmHomePath = function () {
				// Default sdm home path (nw js execution path)
				const nwPath = process.execPath;
				const path = require('path');
				let nwDir = path.dirname(nwPath);
				nwDir = nwDir.replace(/\\/g, '/');
				if (fs.existsSync(`${nwDir}/sdm`)) {
					return nwDir;
				} else {
					simpleApplicationClose();
					throw new Error(`The sdm home path is not valid: ${nwDir}`);
				}
			};

			const sdmHomePath = getSdmHomePath();
			console.info(`SDM home path : ${sdmHomePath}`);

			if (!frontendOnly) {
				const execFile = require('child_process').execFile;
				const process = require('process');

				const isWindows = function () {
					return process.platform === "win32";
				}

				// Backend server startup
				const backendServer = `${sdmHomePath}/backend-server.${isWindows() ? 'bat' : 'sh'}`;
				execFile(backendServer, ['startup'], {cwd: sdmHomePath}, (error, stdout, stderr) => {
					if (error) {
						simpleApplicationClose();
						throw error;
					}
					console.debug(stdout);
				});

				// Backend server shutdown
				const backendServersShutdown = function (callback) {
					console.log('Shutting down backend server...');
					execFile(backendServer, ['shutdown'], {cwd: sdmHomePath}, (error, stdout, stderr) => {
						if (error) {
							throw error;
						}
						console.debug(stdout);
						callback();
					});
				};

				// Close database service
				const databaseClose = function (callback) {
					console.info('Closing database...');
					databaseService.close(function () {
						callback();
					});
				};

				// Application shutdown
				const applicationShutdown = function (callback) {
					databaseClose(function () {
						backendServersShutdown(function () {
							callback();
						});
					});
				};

				// Catch window close
				win.on('close', function () {
					const obj = this;
					obj.hide();
					applicationShutdown(function () {
						console.info('Closing application...');
						obj.close(true);
					});
				});
			} else {
				// When in frontend only mode (development), use the local langs folder.
				$rootScope.getLanguageUrl = function (code) {
					const supportedLanguages = ['fr_CH', 'de_CH', 'it_CH'];
					return `${sdmHomePath}/sdm/langs/${supportedLanguages.includes(code) ? code : 'default'}.json`;
				}
			}
		}

		function formatDate(date) {
			function pad(num, size) {
				const s = '000000000' + num;
				return s.substr(s.length - size);
			}

			return `${pad(date.getUTCHours(), 2)}:${pad(date.getUTCMinutes(), 2)}:${pad(date.getUTCSeconds(), 2)}`;
		}

		$rootScope.log = function (data) {
			const ts = formatDate(new Date());
			console.log(`${ts};${data}`);
		};

		$rootScope.isSync = function () {
			return sessionService.isSync();
		};
	});
