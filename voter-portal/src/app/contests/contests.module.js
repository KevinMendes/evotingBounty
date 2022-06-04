/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

module.exports = angular
	.module('app.contests', [
		'gettext',
		'app.template',
		'ui.router',
		'app.ui.modal',
		'app.services',
	])

	.constant('contestTypes', {
		LISTS_AND_CANDIDATES: 'listsAndCandidates',
		CANDIDATES_ONLY: 'candidatesOnly',
		OPTIONS: 'options',
	})

	.factory('ContestService', require('./services/contest.service'))
	.factory('ListService', require('./services/list.service'))
	.factory('CandidateService', require('./services/candidate.service'))

	.factory(
		'ListsAndCandidatesService',
		require('./services/lists-and-candidates.service'),
	)
	.factory(
		'CandidatesOnlyService',
		require('./services/candidates-only.service'),
	)
	.factory('OptionsService', require('./services/options.service'))

	.directive('autoScroll', require('./components/auto-scroll'))
	.directive('contestChooser', require('./components/contest-chooser'))
	.directive('candidateFinder', require('./components/candidate-finder'))
	.directive(
		'candidatePreSelector',
		require('./components/candidate-pre-selector'),
	)
	.directive('listFinder', require('./components/list-finder'))
	.directive('listPreSelector', require('./components/list-pre-selector'))
	.directive('listSelector', require('./components/list-selector'))
	.directive('candidateSelector', require('./components/candidate-selector'))
	.directive('optionSelector', require('./components/option-selector'))

	.directive('listsAndCandidates', require('./lists-and-candidates'))
	.directive('candidatesOnly', require('./candidates-only'))
	.directive('options', require('./options')).name;
