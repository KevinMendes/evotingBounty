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

module.exports = (function () {
	const parseQuestion = function (rawContest, rawQuestion) {
		const question = new OV.Question(rawQuestion);

		question.attrIndex = _.findIndex(rawContest.attributes, {
			id: question.attribute,
		});

		const options = _.filter(rawContest.options, function (option) {
			const attr = _.find(rawContest.attributes, {id: option.attribute});
			return attr && _.includes(attr.related, rawQuestion.attribute);
		});

		_.each(options, function (option) {
			const isBlank = option.attribute === rawQuestion.blankAttribute;
			const opt = new OV.Option(option, isBlank);
			opt.attrIndex = _.findIndex(rawContest.attributes, {
				id: option.attribute,
			});
			question.addOption(opt);
			question.optionsMinChoices = Number(rawQuestion.min);
			question.optionsMaxChoices = Number(rawQuestion.max);
		});

		question.options = _.sortBy(question.options, ['attrIndex']);

		return question;
	};

	const parseContest = function (rawContest) {
		const contest = new OV.Options(rawContest);

		_.each(rawContest.questions, function (rawQuestion) {
			contest.addQuestion(parseQuestion(rawContest, rawQuestion));
		});

		contest.questions = _.sortBy(contest.questions, ['attrIndex']);

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

		_.each(contest.questions, function (question) {
			question.details = parseAttributeTranslations(txt, question.attribute);
			question.text = question.details.questionType_text;

			if (question.blankOption) {
				question.blankOption.details = parseAttributeTranslations(
					txt,
					question.blankOption.attribute,
				);
				question.blankOption.text = question.blankOption.details.text;
			}

			_.each(question.options, function (option) {
				option.details = parseAttributeTranslations(txt, option.attribute);
				option.text = option.details.answerType_text;
			});
		});
	};

	return {
		parse: parseContest,
		setLocale: setLocale,
	};
})();
