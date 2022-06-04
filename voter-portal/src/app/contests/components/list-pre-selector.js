/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

const _ = require('lodash');

module.exports = function (
	$timeout,
	ListService,
	ContestService,
	sessionService,
	$modal,
	gettext,
) {
	'ngInject';

	return {
		restrict: 'EA',
		require: 'ngModel',
		scope: {
			contest: '=',
			selectedList: '=ngModel',
			onSelected: '=',
			onCleared: '=',
		},
		templateUrl: 'contests/components/list-pre-selector.tpl.html',
		link: function (scope, element, attrs, ngModelCtrl) {
			scope.hasListDetails = ListService.hasListDetails;
			scope.getBlankList = ListService.getBlankList;
			scope.noListChosenCopy = gettext('No list chosen');

			// It is safe to focus the select button at link time because every time a
			// list is selected, the directive is reinstantiated (due to always clearing
			// the selected list on selecting another one, or even the same one)
			element[0].getElementsByClassName('btn-select-list')[0].focus();

			scope.openListSelectionModal = function () {
				const {contest} = scope;

				scope.data = {
					searchedText: '',
					searchPartyText: '',
					contest: _.find(sessionService.getElections(), {
						id: contest.id,
					}),
					allLists: ListService.getAllLists(contest),
					lists: ListService.getAllLists(contest),
				};

				$modal
					.open({
						templateUrl: 'views/modals/choose-party.tpl.html',
						controller: 'defaultModal',
						size: 'lg',
						scope: scope,
					})
					.result.then(function (selectedList) {
					if (typeof scope.onSelected !== 'function') {
						return angular.noop();
					}

					scope.onSelected(selectedList);
				});
			};

			scope.clearList = function () {
				if (typeof scope.onCleared !== 'function') {
					return angular.noop();
				}

				scope.onCleared();

				$timeout(() => {
					document
						.getElementById(attrs.id)
						.getElementsByClassName('btn-select-list')[0]
						.focus();
				});
			};

			ngModelCtrl.$formatters.push(function (modelValue) {
				// Validations go here

				return modelValue;
			});
		},
	};
};
