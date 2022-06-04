/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

describe('i18n service', () => {

	let $rootScope;
	let $httpBackend;
	let i18n;

	beforeEach(angular.mock.module('app.services'));

	beforeEach(inject((_$rootScope_, $injector, _i18n_) => {

		$rootScope = _$rootScope_;
		$httpBackend = $injector.get('$httpBackend');
		i18n = _i18n_;

	}));

	describe('loadConfig()', () => {

		it('requests for i18n.json', () => {

			$httpBackend.when('GET', 'i18n.json').respond(200, {});

			i18n.loadConfig();

			$httpBackend.flush();

			$rootScope.$apply();

			$httpBackend.expectGET('i18n.json');

		});

		it('resolves with default config if i18n.json request fails', () => {

			$httpBackend.when('GET', 'i18n.json').respond(404);

			const configPromise = i18n.loadConfig();

			$httpBackend.flush();

			$rootScope.$apply();

			configPromise.then(configData => {

				expect(configData).toEqual(i18n.configData);

			});

		});

		it('resolves with the obtained data from the server', () => {

			const responseConfig = require('../../../src/i18n.json');

			$httpBackend.when('GET', 'i18n.json').respond(200, responseConfig);

			const configPromise = i18n.loadConfig();

			$httpBackend.flush();

			$rootScope.$apply();

			configPromise.then(configData => {

				expect(configData).toEqual(responseConfig);

			});

		});

	});

});
