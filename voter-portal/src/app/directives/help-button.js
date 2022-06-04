/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function ($modal, config) {
	'ngInject';

	return {
		restrict: 'EA',
		replace: true,
		scope: {
			helpButton: '@',
		},
		transclude: true,
		templateUrl: 'directives/help-button.tpl.html',
		controller: function ($scope) {
			'ngInject';

			$scope.showFAQ = () => {
				$modal.open({
					templateUrl: 'views/modals/help.tpl.html',
					controller: 'faqModal',
					resolve: {
						topic: function () {
							return $scope.helpButton;
						},
					},
				});
			};
		},
	};
};
