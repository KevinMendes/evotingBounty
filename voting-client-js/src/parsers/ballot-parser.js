/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global require */
/* global OV */
/* jshint maxlen: 666  */

const _ = require('lodash');
const getText = require('./util.js').getText;
const parseCorrectnessIds = require('./id-correctness.js');

const parsers = {
	listsAndCandidates: require('./lists-and-candidates.js'),
	options: require('./options.js'),
};

const getParser = function (contestType) {
	const parser = parsers[contestType];
	if (parser) {
		return parser;
	} else {
		console.error('Undefined contest type: ' + contestType);
		throw new Error('Undefined contest type: ' + contestType);
	}
};

module.exports = (function () {
	'use strict';

	let i18nTexts = null;

	const parseContest = function (contestJson) {
		const parser = getParser(contestJson.template);
		return parser.parse(contestJson);
	};

	const parseBallot = function (ballotJson) {
		const ballot = new OV.Ballot(ballotJson.id);

		_.each(ballotJson.contests, function (c) {
			ballot.addContest(parseContest(c));
		});

		const correctnessIds = {};
		_.each(ballotJson.contests, function (contest) {
			_.each(contest.options, function (option) {
				if (option.representation) {
					parseCorrectnessIds(correctnessIds, option, contest.attributes);
				}
			});
		});

		ballot.correctnessIds = correctnessIds;
		ballot.writeInAlphabet = ballotJson.writeInAlphabet;

		return ballot;
	};

	const getLocaleText = function (locale) {
		let i18n = _.find(i18nTexts, {
			locale: locale,
		});
		if (!i18n) {
			i18n = _.find(i18nTexts, {
				locale: locale.replace('-', '_'),
			});
		}

		return i18n ? i18n.texts : {};
	};

	const setLocale = function (ballot, locale) {
		const txt = getLocaleText(locale);

		_.each(ballot.contests, function (contest) {
			ballot.title = getText(txt, ballot.id, 'title', null, 'ballot');
			ballot.description = getText(
				txt,
				ballot.id,
				'description',
				null,
				'ballot',
			);

			const parser = getParser(contest.template);
			parser.setLocale(contest, txt);
		});
	};

	const parseTexts = function (ballot, ballotTextsJson) {
		i18nTexts = ballotTextsJson;
		if (ballotTextsJson[0].locale) {
			setLocale(ballot, ballotTextsJson[0].locale);
		} else {
			console.log('Ballot i18n not found');
		}
	};


	return {
		parseBallot: parseBallot,
		parseTexts: parseTexts,
		setLocale: setLocale,
	};
})();
