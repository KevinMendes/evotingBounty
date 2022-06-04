/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global OV */

describe('Options contest parser', function () {
	'use strict';

	const optionsParser = OV.OptionsParser;
	const contest = require('./mocks/options.json');
	const contestWithBlank = require('./mocks/optionsWithBlank.json');

	it('should parse the options configuration', function () {
		const parsed = optionsParser.parse(contest);

		expect(parsed.template).toBe('options');
	});

	it('should parse the questions min and max restrictions', function () {
		const parsed = optionsParser.parse(contest);

		expect(parsed.questions[0].optionsMinChoices).toBe(1);
		expect(parsed.questions[0].optionsMaxChoices).toBe(1);
	});

	it('should parse the options inside the questions', function () {
		const parsed = optionsParser.parse(contest);

		expect(parsed.questions[0].options.length).toBe(2);
		expect(
			parsed.questions[0].options.filter(function (o) {
				return o.prime === '100003';
			}).length,
		).toBe(1);
		expect(
			parsed.questions[0].options.filter(function (o) {
				return o.prime === '100019';
			}).length,
		).toBe(1);
	});

	it('should not parse a blank option as min == max', function () {
		contest.questions[0].min = 1;
		contest.questions[0].max = 1;
		const parsed = optionsParser.parse(contest);

		expect(parsed.questions[0].blankOption).toBe(null);
	});

	it('should parse the blank option as min < max (blank vote allowed)', function () {
		const parsed = optionsParser.parse(contestWithBlank);
		expect(parsed.questions[0].blankOption.prime).toBe('10009');
	});
});
