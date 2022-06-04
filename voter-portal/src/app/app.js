/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

/**
 * Polyfills
 */
require('core-js/es/array');
require('core-js/features/object/assign');

angular
	.module('configModule', [])
	.provider('config', require('./services/config'));

angular
	.module('app', [
		// config
		require('./routes'),

		// global services
		require('./services/services.module'),

		// global directives
		require('./directives/directives.module'),
		require('./directives/modal/modal.module'),

		// contests
		require('./contests/contests.module'),

		// html templates
		'app.template',

		// controllers
		require('./views/legal/legal'),
		require('./views/identification/identification.module'),
		require('./views/choose/choose'),
		require('./views/review/review'),
		require('./views/validate/validate'),
		require('./views/confirm/confirm'),
		require('./views/faq/faqModal'),

		// external modules
		'ng-showdown',
		'gettext',
		'ui.bootstrap.dropdown',
	])

	.run(function (
		$rootScope,
		$interval,
		$window,
		$modal,
		$state,
		sessionService,
		gettextCatalog,
		i18n,
		config,
		ContestService,
		$modalStack,
	) {
		'ngInject';

		function disableF5(e) {
			if ((e.which || e.keyCode) === 116) {
				e.preventDefault();
			}
		}

		angular.element(document).on('keydown', disableF5);

		window.onbeforeunload = function () {
			return gettextCatalog.getString(
				'Are you sure you want to navigate away?',
			);
		};

		$rootScope.$on('$locationChangeStart', function (event, nextUrl) {
			if ($modalStack.getTop()) {
				event.preventDefault();
				$modalStack.dismissAll();

				return;
			}

			// prevent backward when voter is authenticated
			if (!_.isEmpty(sessionService.getAuthenticationToken())) {
				// If navigating to login while logged in, warn the user that the voting
				// process will be canceled if continues
				if (
					nextUrl.indexOf('start-voting') !== -1 &&
					$state.current.name !== 'confirmation'
				) {
					event.preventDefault();
					$rootScope.cancelVote();
				}
			}
		});

		$rootScope.$on('$stateChangeStart', function (event, toState) {
			// prevent access to any page without previously authenticate
			if (_.isEmpty(sessionService.getAuthenticationToken())) {
				if (toState && toState.data && toState.data.secure) {
					event.preventDefault();
					$state.go('legal-terms');
				}
			}
		});

		$rootScope.$on('$stateChangeSuccess', function (
			event,
			toState,
			toParams,
			fromState,
		) {
			toState.data.previous = fromState;
			if (toState.name !== 'choose') {
				const interval = $interval(function () {
					if (document.readyState === 'complete') {
						$window.scrollTo(0, 0);
						$interval.cancel(interval);
					}
				}, 100);
			}
		});

		$rootScope.setAccepted = function () {
			$rootScope.accepted = true;
		};

		// ----------------------------------------------------------------------------------
		// language management

		$rootScope.switchLanguage = function (lang) {
			$rootScope.lang = lang.code;
			gettextCatalog.setCurrentLanguage(lang.code);
			if (lang.code !== 'xx') {
				// 'xx' is the default (pseudo english) base lang

				gettextCatalog.loadRemote('assets/lang/' + lang.code + '.json');
			}

			if ($rootScope.ballot) {
				// PO lang code format uses '_' e.g. 'en_US'
				// but Back-end contest come with '-' e.g. 'en-US'
				// so we need to reformat when asking for it to the ovApi
				ovApi.translateBallot($rootScope.ballot, lang.code.replace('_', '-'));

				// This is needed for the translations
				$rootScope.ballot.contests.forEach(ContestService.mapContestDetails);
			}
		};

		$rootScope.i18n = {
			languages: i18n.languages,
			lang: {
				code: i18n.default,
			},
		};

		$rootScope.getLanguageName = function (code) {
			if (!code) {
				return '';
			}

			try {
				return _.find($rootScope.i18n.languages, {code: code}).name;
			} catch (e) {
				return '';
			}
		};
		$rootScope.transformLanguageCode = function (code, countryFormat) {
			if (!code) {
				return '';
			}

			// specify countryFormat to true if you want to add the country code. e.g. 'en-US'
			// don't if you just need the lang code. e.g. 'en'
			try {
				let langCode;

				if (countryFormat) {
					langCode = _.find($rootScope.i18n.languages, {
						code: code,
					}).code.replace('_', '-');
				} else {
					langCode = _.find($rootScope.i18n.languages, {
						code: code,
					}).code.split('_')[0];
				}

				return langCode;
			} catch (e) {
				return '';
			}
		};

		i18n.loadConfig().then(function (loadedConfig) {
			// preload languages to avoid FUOC:

			try {
				loadedConfig.languages.forEach(function (lang) {
					gettextCatalog.loadRemote('assets/lang/' + lang.code + '.json');
				});
			} catch (e) {
				console.error('Bad i18n config: ', e);
			}

			let initialLang = null;

			$rootScope.i18n = {
				languages: loadedConfig.languages,
				lang: {
					code: loadedConfig.default,
				},
			};

			if ($rootScope.requestedLang) {
				initialLang = _.find($rootScope.i18n.languages, {
					code: $rootScope.requestedLang,
				});
			}
			if (initialLang) {
				$rootScope.switchLanguage(initialLang);
				$rootScope.i18n.lang = initialLang;
			} else {
				$rootScope.switchLanguage($rootScope.i18n.lang);
			}
		});

		// ----------------------------------------------------------------------------------

		let cancelVoteConfirmationModal;

		// abandon/quit
		$rootScope.cancelVote = function () {
			if ($rootScope.voteState === 'cast') {
				$rootScope.voteState = 'quit';
				$state.go('identification');

				return;
			}

			if (cancelVoteConfirmationModal) {
				return;
			}

			cancelVoteConfirmationModal = $modal.open({
				templateUrl:
					$rootScope.voteState === 'sent'
						? 'views/modals/abandonValidate.tpl.html'
						: 'views/modals/abandon.tpl.html',
				controller: 'defaultModal',
			});

			cancelVoteConfirmationModal.result.then(
				function () {
					if ($rootScope.voteState === 'voting') {
						$rootScope.voteState = 'cancel';
					} else if ($rootScope.voteState === 'sent') {
						$rootScope.voteState = 'leave';
					} else if ($rootScope.voteState === 'cast') {
						$rootScope.voteState = 'quit';
					}
					$state.go('identification');

					cancelVoteConfirmationModal = null;
				},
				function () {
					cancelVoteConfirmationModal = null;
				},
			);
		};

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

		$rootScope.accepted = false;

		// defaults

		$rootScope.settings = {
			eventId: '',
			tenantId: '100',
			debug: false,
		};
	});
