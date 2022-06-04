/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function ($scope, $modalInstance, labels) {
	'ngInject';

	$scope.labels = labels;

	$scope.ok = function (result) {
		$modalInstance.close(result);
	};
	$scope.cancel = function (reason) {
		$modalInstance.dismiss(reason);
	};
};
