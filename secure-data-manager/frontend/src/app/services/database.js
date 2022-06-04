/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
angular
	.module('databaseService', [
		// 'conf.globals'
	])

	.factory('databaseService', function ($http, endpoints, $interval) {
		'use strict';

		const close = function (callback) {
			$http.post(endpoints.host() + endpoints.close).then(function () {
				const delay = 2000;
				const poll = $interval(function () {
					$http.get(endpoints.host() + endpoints.status).then(function (response) {
						const data = response.data;
						console.info('Status: ' + data.status);
						if (data.status === 'CLOSED') {
							$interval.cancel(poll);
							callback();
						}
					});
				}, delay);
			}, function () {
				console.error('Error during closing database: host is not reachable.');
				callback();
			});
		};

		return {
			close: close,
		};
	});
