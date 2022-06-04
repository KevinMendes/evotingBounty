/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function (
	$scope,
	$rootScope,
	$state,
	$modal,
	sessionService,
	gettextCatalog,
	$controller,
	detailedProgressService,
	gettext,
) {
	'ngInject';

	// base identification controller:

	$controller('identificationBase', {
		$scope: $scope,
		$rootScope: $rootScope,
		$state: $state,
		$modal: $modal,
		sessionService: sessionService,
		gettextCatalog: gettextCatalog,
	});

	detailedProgressService.init([
		{
			running: gettext(
				'Authentication and retrieving Ballot Information in progress',
			),
			success: gettext('Authentication and retrieving Ballot Information done'),
		},
	]);

	$scope.startVoting = function () {
		sessionService.setState(''); // restart state: quit, leave, cancel
		$scope.errors = {};

		$scope.data.startVotingKey = $scope.data.svk.toLowerCase();

		// key present?

		if ($scope.authForm.$invalid) {
			const input = document.getElementById('fc_start_voting_code');

			input.focus();

			$scope.authForm.$setPristine();
			$scope.authForm.$setUntouched();

			return;
		}

		// ok, go voting
		$scope.doStartVoting($scope.data.startVotingKey);
	};
};
