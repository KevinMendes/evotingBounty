/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

module.exports = function () {
	// default config:

	const configData = {
		identification: 'svk', // dob/yob/svk
	};

	// load from json:

	this.loadConfig = function () {
		const $http = angular.injector(['ng']).get('$http');
		const $q = angular.injector(['ng']).get('$q');
		const deferred = $q.defer();

		$http
			.get('config.json')
			.then(function (res) {
				const data = res.data;

				angular.copy(_.merge(configData, data), configData);
				deferred.resolve(configData);
			})
			.catch(function () {
				console.log('using default config');
				deferred.resolve(configData);
			});

		return deferred.promise;
	};

	// provide it:

	this.$get = function () {
		return configData;
	};
};
