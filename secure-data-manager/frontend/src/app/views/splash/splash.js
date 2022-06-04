/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
(function () {
	'use strict';

	angular
		.module('splash', ['ui.router', 'endpoints'])
		.controller('splash', function (
			$scope,
			$rootScope,
			endpoints,
			$http,
			$state,
			$interval,
			sessionService,
		) {
			// admin boards initialization
			const listAdminBoards = function () {
				$http.get(endpoints.host() + endpoints.administrationBoards).then(
					function success(response) {
						const data = response.data;
						try {
							sessionService.setAdminBoards(data.result);
						} catch (e) {
							console.log(e);
						}
					},
					function error() {
						$scope.errors.administrationBoardsFailed = true;
					},
				);
			};

			const delay = 2000;
			const poll = $interval(function () {
				$http.get(endpoints.host() + endpoints.status).then(function (response) {
					const data = response.data;
					console.info(`Host is reachable, host status : ${data.status}`);
					if (data.status === 'OPEN') {
						$interval.cancel(poll);
						listAdminBoards();
						$rootScope.getConfig();
					}
					sessionService.setHostMode(data.hostMode);
				}, function () {
					console.warn(`Host not yet reachable : ${endpoints.host() + endpoints.status}`);
				});
			}, delay);
		});
})();
