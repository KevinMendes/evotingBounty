/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
(function () {
    'use strict';

    angular
        .module('app.progressbar', [])
        .factory('InProgress', function ($interval, endpoints, $http, $timeout) {
			const progressValue = {};
			const intervals = {};
			const InProgress = {
				contains: function (id) {
					return progressValue[id];
				},
				init: function (id) {
					intervals[id] = $interval(function () {
						const url = endpoints.progress.replace('{id}', id);
						$http.get(endpoints.host() + url).then(function (progress) {
							InProgress.update(id, progress);
						});
					}, 1000);

					progressValue[id] = {
						percentage: 0,
						remainingTime: -1,
					};
				},
				update: function (id, progress) {
					progressValue[id] = progress.data;
				},
				finish: function (id) {
					$interval.cancel(intervals[id]);
					if (progressValue[id]) {
						progressValue[id].percentage = 100;
						progressValue[id].remainingTime = 0;

						//remove interval
						$timeout(function () {
							InProgress.clean(id);
						}, 1000);
					}
				},
				clean: function (id) {
					delete progressValue[id];
				},
				get: function (id) {
					if (
						progressValue[id] &&
						progressValue[id].percentage === 100 &&
						progressValue[id].remainingTime === 0
					) {
						InProgress.clean(id);
						return progressValue[id];
					} else {
						return progressValue[id];
					}
				},
			};
			return InProgress;
        })
        .directive('progressBar', function (InProgress) {
            return {
                restrict: 'AE',
                transclude: true,
                templateUrl: 'app/services/progress-bar.html',
                scope: {
                    id: '=',
                },
                link: function (scope) {
                    scope.InProgress = InProgress;
                },
            };
        });
})();
