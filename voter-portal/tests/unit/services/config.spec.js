/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

describe('config service', () => {

	let config;
	let $httpBackend;
	const configFile = 'config.json';

	beforeEach(angular.mock.module('configModule'));

	beforeEach(angular.mock.module((configProvider, $httpBackendProvider) => {

		// Not working due to $http being injected in configProvider
		$httpBackend = $httpBackendProvider.$get();
		config = configProvider;

	}));

	beforeEach(inject(() => {

		$httpBackend.whenGET(configFile).respond(200, {});

	}));

	describe('loadConfig()', () => {

		it('requests for ' + configFile, () => {

			config.loadConfig();

			$httpBackend.expectGET(configFile);

		});

		it('resolves with default config if config.json request fails', (done) => {

			$httpBackend.whenGET(configFile).respond(404, {});

			const configPromise = config.loadConfig();

			configPromise.then(configData => {

				expect(configData).toEqual(config.$get());
				done();

			});

		});

	});

});
