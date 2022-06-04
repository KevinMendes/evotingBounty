/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global require */
/* global OV */
/* jshint maxlen: 666  */

const _ = require('lodash');
const Utils = require('./util.js');

const getText = Utils.getText;
const parseAttributeTranslations = Utils.parseAttributeTranslations;

const LISTS_QUESTION = 'lists';
const CANDIDATES_QUESTION = 'candidates';

module.exports = (function () {
	const addCandidates = function (contest, rawContest, list) {
		if (list.isBlank) {
			let hasCandidatesWriteIns = false;

			const candidateQuestion = _.find(rawContest.questions, function (question) {
				const attr = _.find(rawContest.attributes, {
					alias: CANDIDATES_QUESTION,
				});
				return attr && question.attribute === attr.id;
			});

			if (candidateQuestion) {
				// Find blank or writeins options for the candidate question
				const candidateQuestionOptions = _.filter(rawContest.options, function (o) {
					return (
						o.attribute === candidateQuestion.blankAttribute ||
						o.attribute === candidateQuestion.writeInAttribute
					);
				});

				// For each option, create candidate and add it to the blank list
				_.each(candidateQuestionOptions, function (option) {
					const isBlankCandidate =
						option.attribute === candidateQuestion.blankAttribute
							? OV.Candidate.IS_BLANK
							: 0;
					const isWriteIn =
						option.attribute === candidateQuestion.writeInAttribute
							? OV.Candidate.IS_WRITEIN
							: 0;

					if (!hasCandidatesWriteIns && isWriteIn > 0) {
						hasCandidatesWriteIns = true;
					}

					const candidate = new OV.Candidate(
						option,
						isBlankCandidate | isWriteIn,
					);
					list.addCandidate(candidate);
				});

				contest.candidatesQuestion = {
					id: candidateQuestion.id,
					minChoices: Number(candidateQuestion.min),
					maxChoices: Number(candidateQuestion.max),
					hasWriteIns: hasCandidatesWriteIns,
					fusions: candidateQuestion.fusions || [],
					cumul: Number(candidateQuestion.accumulation),
				};
			}
		} else {
			const options = _.filter(rawContest.options, function (option) {
				const attr = _.find(rawContest.attributes, {id: option.attribute});
				return attr && _.includes(attr.related, list.attribute);
			});

			// If accumulation is allowed, then an option will have as many representations as
			// the number of allowed accumulation, but from the user point of view, is still only one option.
			// If no accumulation, then each option has only one representation => grouping options based on
			// attribute will give us a way to create "unique options".
			const groupedOptions = _.groupBy(options, 'attribute');

			_.each(Object.keys(groupedOptions), function (optionsAttribute) {
				const similarOptions = groupedOptions[optionsAttribute];
				// When grouping, at least one option will be available so is safe to make it the "primary" one
				const option = similarOptions[0];
				const attr = _.find(rawContest.attributes, {id: option.attribute});

				option.alias = attr.alias;

				// Following arrays will be used by VP to create unique { id, prime } tuples when sending accumulated votes
				option.allIds = similarOptions.map(function (o) {
					return o.id;
				});
				option.allRepresentations = similarOptions.map(function (o) {
					return o.representation;
				});

				const candidate = new OV.Candidate(option);
				candidate.attrIndex = _.findIndex(rawContest.attributes, {
					id: candidate.attribute,
				});

				list.addCandidate(candidate);
			});

			list.candidates = _.sortBy(list.candidates, ['attrIndex']);
		}
	};

	// There is a list question => LISTS_AND_CANDIDATES
	const parseListQuestion = function (contest, rawContest, listQuestion) {
		const options = _.filter(rawContest.options, function (option) {
			const attr = _.find(rawContest.attributes, {id: option.attribute});
			return attr && _.includes(attr.related, listQuestion.attribute);
		});

		let hasBlankList = false;
		let hasListWriteIns = false;

		_.each(options, function (option) {
			const isBlankList =
				option.attribute === listQuestion.blankAttribute ? OV.List.IS_BLANK : 0;
			const isWriteInOption =
				option.attribute === listQuestion.writeInAttribute
					? OV.List.IS_WRITEIN
					: 0;

			if (!hasBlankList && isBlankList > 0) {
				hasBlankList = true;
			}

			if (!hasListWriteIns && isWriteInOption > 0) {
				hasListWriteIns = true;
			}

			const list = new OV.List(
				option,
				option.attribute,
				isBlankList | isWriteInOption,
			);
			list.attrIndex = _.findIndex(rawContest.attributes, {
				id: option.attribute,
			});
			addCandidates(contest, rawContest, list);
			contest.addList(list);
		});

		contest.lists = _.sortBy(contest.lists, ['attrIndex']);

		contest.listQuestion = {
			id: listQuestion.id,
			minChoices: Number(listQuestion.min),
			maxChoices: Number(listQuestion.max),
			cumul: Number(listQuestion.accumulation),
		};

		// No blank List provided, so we have to create one
		if (!hasBlankList) {
			const blankList = new OV.List({}, null, OV.List.IS_BLANK);
			addCandidates(contest, rawContest, blankList);
			contest.addList(blankList, true);
		}
	};

	// There is no list question => CANDIDATES_ONLY
	const parseLists = function (contest, rawContest) {
		const lists = _.filter(rawContest.attributes, function (attr) {
			return (
				attr.related.length === 0 &&
				attr.id !== rawContest.questions[0].attribute
			);
		});

		_.each(lists, function (rawList) {
			const list = new OV.List(rawList, rawList.id);
			addCandidates(contest, rawContest, list);
			contest.addList(list);
		});

		// No blank List provided, so we have to create one
		const blankList = new OV.List({}, null, OV.List.IS_BLANK);
		addCandidates(contest, rawContest, blankList);
		contest.addList(blankList, true);
	};

	const parseContest = function (rawContest) {
		const contest = new OV.ListsAndCandidates(rawContest);

		const listQuestion = _.find(rawContest.questions, function (question) {
			const attr = _.find(rawContest.attributes, {alias: LISTS_QUESTION});
			return attr && question.attribute === attr.id;
		});

		if (listQuestion) {
			parseListQuestion(contest, rawContest, listQuestion);
		} else {
			parseLists(contest, rawContest);
		}

		return contest;
	};

	const setLocale = function (contest, txt) {
		contest.title = getText(txt, contest.id, 'title', null, 'contest');
		contest.description = getText(
			txt,
			contest.id,
			'description',
			null,
			'contest',
		);
		contest.howToVote = getText(txt, contest.id, 'howToVote', null, 'contest');

		// Get any other translations available for the contest
		contest.details = parseAttributeTranslations(txt, contest.id);

		_.each(contest.lists, function (list) {
			if (list.attribute) {
				// Get all translations available for the list
				list.details = parseAttributeTranslations(txt, list.attribute);
			}

			_.each(list.candidates, function (candidate) {
				// Get all translations available for the list
				candidate.details = parseAttributeTranslations(
					txt,
					candidate.attribute,
				);
			});
		});
	};

	return {
		parse: parseContest,
		setLocale: setLocale,
	};
})();
