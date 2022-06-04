/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global OV */

describe('Lists And Candidates contest parser', function () {
	'use strict';

	const lacParser = OV.ListsAndCandidatesParser;
	const contest = require('./mocks/listsAndCandidates.json');
	const candidatesOnly = require('./mocks/candidatesOnly.json');
	const candidatesWithWriteIns = require('./mocks/candidatesWithWriteIns.json');

	it('should parse the contest configuration', function () {
		const parsed = lacParser.parse(contest);

		expect(parsed.template).toBe('listsAndCandidates');
		expect(parsed.allowFullBlank).toBe(true);
	});

	it('should parse the lists min and max restrictions', function () {
		const parsed = lacParser.parse(contest);

		// If it fails, it is probably due to not being able to find the question.
		expect(parsed.listQuestion.minChoices).toBe(0);
		expect(parsed.listQuestion.maxChoices).toBe(1);
	});

	it('should parse the candidates min and max restrictions', function () {
		const parsed = lacParser.parse(contest);

		expect(parsed.candidatesQuestion.minChoices).toBe(0);
		expect(parsed.candidatesQuestion.maxChoices).toBe(2);
	});

	it('should parse the lists', function () {
		const parsed = lacParser.parse(contest);

		expect(parsed.lists.length).toBe(3);

		expect(
			parsed.lists.filter(function (l) {
				return l.prime === '100109';
			}).length,
		).toBe(1);
		expect(
			parsed.lists.filter(function (l) {
				return l.prime === '100169';
			}).length,
		).toBe(1);
		expect(
			parsed.lists.filter(function (l) {
				return l.prime === '100153';
			}).length,
		).toBe(1);
	});

	it('should parse the blank list', function () {
		const parsed = lacParser.parse(contest);

		const blankList = parsed.lists.filter(function (l) {
			return l.prime === '100109';
		})[0];

		expect(blankList.isBlank).toBe(true);
		expect(blankList.candidates.length).toBe(2);

		expect(
			blankList.candidates.filter(function (c) {
				return c.prime === '100183';
			}).length,
		).toBe(1);
		expect(
			blankList.candidates.filter(function (c) {
				return c.prime === '100193';
			}).length,
		).toBe(1);
	});

	it('should parse the candidates', function () {
		const parsed = lacParser.parse(contest);

		const list1 = parsed.lists.filter(function (l) {
			return l.prime === '100169';
		})[0];

		expect(list1.isBlank).toBe(false);
		expect(list1.candidates.length).toBe(2);

		expect(
			list1.candidates.filter(function (c) {
				return c.prime === '100129';
			}).length,
		).toBe(1);
		expect(
			list1.candidates.filter(function (c) {
				return c.prime === '100237';
			}).length,
		).toBe(1);

		const list2 = parsed.lists.filter(function (l) {
			return l.prime === '100153';
		})[0];

		expect(list2.isBlank).toBe(false);
		expect(list2.candidates.length).toBe(2);

		expect(
			list2.candidates.filter(function (c) {
				return c.prime === '100207';
			}).length,
		).toBe(1);
		expect(
			list2.candidates.filter(function (c) {
				return c.prime === '100267';
			}).length,
		).toBe(1);
	});

	it('should parse the blank candidate inside a non selectable blank list', function () {
		const parsed = lacParser.parse(candidatesOnly);

		const blankList = parsed.lists.filter(function (l) {
			return l.isBlank === true;
		})[0];

		expect(blankList.isBlank).toBe(true);
		expect(blankList.candidates.length).toBe(1);

		expect(
			blankList.candidates.filter(function (c) {
				return c.prime === '100673';
			}).length,
		).toBe(1);
	});

	it('should parse the candidates with non selectable lists', function () {
		const parsed = lacParser.parse(candidatesOnly);

		const list1 = parsed.lists.filter(function (l) {
			return l.id === '9053f1203ecf4281b220b9c385c3c18e';
		})[0];

		expect(list1.isBlank).toBe(false);
		expect(list1.candidates.length).toBe(2);

		expect(
			list1.candidates.filter(function (c) {
				return c.prime === '10069';
			}).length,
		).toBe(1);
		expect(
			list1.candidates.filter(function (c) {
				return c.prime === '100613';
			}).length,
		).toBe(1);

		const list2 = parsed.lists.filter(function (l) {
			return l.id === '980cd86438a048e78737405f34687c05';
		})[0];

		expect(list2.isBlank).toBe(false);
		expect(list2.candidates.length).toBe(2);

		expect(
			list2.candidates.filter(function (c) {
				return c.prime === '100559';
			}).length,
		).toBe(1);
		expect(
			list2.candidates.filter(function (c) {
				return c.prime === '100549';
			}).length,
		).toBe(1);
	});

	it('should parse the candidates with writeins', function () {
		const parsed = lacParser.parse(candidatesWithWriteIns);

		const blankList = parsed.lists.find(function (l) {
			return l.isBlank === true;
		});

		expect(blankList.candidates.length).toBe(2);

		expect(
			blankList.candidates.find(function (o) {
				return o.isBlank;
			}),
		).toBeDefined();
		expect(
			blankList.candidates.find(function (o) {
				return o.isWriteIn;
			}),
		).toBeDefined();
	});
});
