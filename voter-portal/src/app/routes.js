/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = angular
	.module('routes', ['ui.router', 'configModule'])

	.config(function (
		$urlRouterProvider,
		$stateProvider,
		configProvider,
		$locationProvider,
	) {
		'ngInject';

		$locationProvider.html5Mode(false);
		$locationProvider.hashPrefix('');

		$stateProvider
			.state('body', {
				abstract: true,
				templateUrl: 'layout/body.html',
			})
			.state('body.layout', { // NOSONAR Rule javascript:S1192 - False positive
				abstract: true,
				views: {
					'header@body': {
						templateUrl: 'layout/header.html',
					},
				},
			})
			.state('legal-terms', {
				parent: 'body.layout', // NOSONAR Rule javascript:S1192 - False positive
				url: '/legal-terms/:eeid?lang',
				views: {
					'view@body': {
						templateUrl: 'views/legal/legalTerms.tpl.html',
						controller: 'legal',
					},
				},
				data: {
					secure: false,
				},
			})
			.state('choose', {
				parent: 'body.layout', // NOSONAR Rule javascript:S1192 - False positive
				url: '/choose',
				views: {
					'view@body': {
						templateUrl: 'views/choose/choose.tpl.html',
						controller: 'choose',
					},
				},
				resolve: {
					isAllowed: ($rootScope, $q) => {
						'ngInject';

						// Prevent navigation to this state if the voting state is not in the list
						if (
							$rootScope.voteState &&
							['voting'].indexOf($rootScope.voteState) === -1
						) {
							return $q.reject();
						}

						return $q.resolve();
					},
				},
				data: {
					secure: true,
				},
			})
			.state('review', {
				parent: 'body.layout', // NOSONAR Rule javascript:S1192 - False positive
				url: '/review-send',
				views: {
					'view@body': {
						templateUrl: 'views/review/review.tpl.html',
						controller: 'review',
					},
				},
				data: {
					secure: true,
				},
				resolve: {
					isAllowed: ($rootScope, $q) => {
						'ngInject';

						// Prevent navigation to this state if the voting state is not in the list
						if (
							$rootScope.voteState &&
							['voting'].indexOf($rootScope.voteState) === -1
						) {
							return $q.reject();
						}

						// Prevent navigating to this state if any of the contests is invalid
						const allContestsAreValid = $rootScope.ballot.contests.map(c =>
							c.validate(c),
						);

						if (allContestsAreValid.indexOf(false) !== -1) {
							return $q.reject();
						}

						return $q.resolve();
					},
				},
			})
			.state('validate', {
				parent: 'body.layout', // NOSONAR Rule javascript:S1192 - False positive
				url: '/verify-cast',
				views: {
					'view@body': {
						templateUrl: 'views/validate/validate.tpl.html',
						controller: 'validate',
					},
				},
				resolve: {
					isAllowed: ($rootScope, $q) => {
						'ngInject';

						// Prevent navigation to this state if the voting state is not in the list
						if (
							$rootScope.voteState &&
							['sent'].indexOf($rootScope.voteState) === -1
						) {
							return $q.reject();
						}

						return $q.resolve();
					},
				},
				data: {
					secure: true,
				},
			})
			.state('confirmation', {
				parent: 'body.layout', // NOSONAR Rule javascript:S1192 - False positive
				url: '/vote-cast',
				views: {
					'view@body': {
						templateUrl: 'views/confirm/confirmation.tpl.html',
						controller: 'confirm',
					},
				},
				resolve: {
					isAllowed: ($rootScope, $q) => {
						'ngInject';

						// Prevent navigation to this state if the voting state is not in the list
						if (
							$rootScope.voteState &&
							['cast'].indexOf($rootScope.voteState) === -1
						) {
							return $q.reject();
						}

						return $q.resolve();
					},
				},
				data: {
					secure: true,
				},
			});

		$urlRouterProvider.otherwise('/legal-terms/');

		// set up configurable routes

		configProvider.loadConfig().then(function (configData) {
			let identificationTemplateUrl;
			let identificationController;

			switch (configData.identification) {
				case 'svk':
					identificationTemplateUrl = 'views/identification/ident-svk.tpl.html';
					identificationController = 'identificationSVK';
					break;
				case 'yob':
				case 'dob':
					identificationTemplateUrl = 'views/identification/ident-dob.tpl.html';
					identificationController = 'identificationDOB';
					break;
				default:
					console.error('FATAL: no identification mode configured!');
			}

			$stateProvider.state('identification', {
				parent: 'body.layout', // NOSONAR Rule javascript:S1192 - False positive
				url: '/start-voting',
				views: {
					'view@body': {
						templateUrl: identificationTemplateUrl,
						controller: identificationController,
					},
				},
				data: {
					secure: false,
				},
			});
		});
	}).name;
