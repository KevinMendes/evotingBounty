/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function (gettextCatalog) {
	'ngInject';

	return {
		restrict: 'EA',
		replace: true,
		scope: {
			alertText: '=',
			alertType: '=',
			alertParams: '=',
		},
		transclude: true,
		templateUrl: 'directives/alert-message.tpl.html',
		link: function (scope) {
			scope.getMessageType = () => {
				const alertTypes = ['error', 'warning', 'success'];
				const desiredAlertType = scope.alertType.toLowerCase();

				return alertTypes.indexOf(desiredAlertType) > -1
					? desiredAlertType
					: 'error';
			};

			scope.getTranslatedMessage = () => {
				return gettextCatalog.getString(scope.alertText, scope.alertParams);
			};

			scope.dissmissAlert = function () {
				if (scope.alertType) {
					scope.alertClosed = true;
				}
			};
		},
	};
};
