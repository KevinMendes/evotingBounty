/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = function (gettext) {
	'ngInject';

	return {
		restrict: 'A',
		scope: {
			currentStepNum: '=',
			currentStepCompleted: '=',
		},
		templateUrl: 'directives/breadcrumbs.tpl.html',
		link: function (scope) {
			scope.steps = [
				{num: 1, text: gettext('Select answers'), srText: gettext('Choose')},
				{
					num: 2,
					text: gettext('Review and Seal'),
					srText: gettext('Review and Seal'),
				},
				{
					num: 3,
					text: gettext('Verify and Cast'),
					srText: gettext('Verify and Cast'),
				},
				{num: 4, text: gettext('Vote Cast'), srText: gettext('Vote Cast')},
			];
		},
	};
};
