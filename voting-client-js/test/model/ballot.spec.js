/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global OV */

describe('Ballot model', function () {
	'use strict';

	const listsAndCandidates = require('./mocks/listsAndCandidates.json');
	const optionsContest = require('./mocks/options.json');

	it('should init a contest', function () {
		const c1 = new OV.ListsAndCandidates(listsAndCandidates);

		expect(c1.template).toBe('listsAndCandidates');
		expect(c1.allowFullBlank).toBe(true);
	});

	it('should create a simple question with two options', function () {
		const b1 = new OV.Ballot('001');
		const c1 = new OV.Options(optionsContest);
		const q1 = new OV.Question(optionsContest.questions[0]);

		const o1 = new OV.Option(optionsContest.options[0], false);
		const o2 = new OV.Option(optionsContest.options[1], false);

		b1.addContest(c1);
		c1.addQuestion(q1);
		q1.addOption(o1);
		q1.addOption(o2);

		expect(q1).toBeDefined();
		expect(q1.options.length).toBe(2);
		expect(o1.parent).toBe(q1);
		expect(o2.parent).toBe(q1);

		expect(b1.getQualifiedId()).toBe('001');
		expect(c1.getQualifiedId()).toBe('001_e133300f6a124ca8aea3fd7c935b18e0');
		expect(q1.getQualifiedId()).toBe(
			'001_e133300f6a124ca8aea3fd7c935b18e0_50e2e005c2c0413a96cd6b36e659fae0',
		);
	});

	it('should create a question with a blank option', function () {
		const q1 = new OV.Question(optionsContest.questions[0]);
		const o1 = new OV.Option(optionsContest.options[1], true);

		q1.addOption(o1);
		expect(q1.options.length).toBe(0);
		expect(q1.blankOption).toBe(o1);
	});

	it('should keep track of options ordering within the questions', function () {
		const q = new OV.Question('q');
		const o1 = new OV.Option({id: '1', representation: '3'});
		const o2 = new OV.Option({id: '1', representation: '5'});
		const o3 = new OV.Option({id: '1', representation: '7'});
		const oblank = new OV.Option({id: '1', representation: '11'}, true);

		q.addOption(o1);
		q.addOption(o2);
		q.addOption(o3);
		q.addOption(oblank);

		expect(o1.ordinal).toBe(1);
		expect(o2.ordinal).toBe(2);
		expect(o3.ordinal).toBe(3);
		expect(oblank.ordinal).toBe(0);
	});

	it('should check types of childs', function () {
		expect(function () {
			const x = new OV.Ballot('001');
			x.addContest({});
		}).toThrow(new Error('Bad argument type, need a Contest'));

		expect(function () {
			const x = new OV.Options('001', 'options');
			x.addQuestion({});
		}).toThrow(new Error('Bad argument type, need a Question'));

		expect(function () {
			const x = new OV.Question('001');
			x.addOption({});
		}).toThrow(new Error('Bad argument type, need an Option'));
	});

	it('should not accept more than one blank option in a question', function () {
		const q1 = new OV.Question('001');
		const o1 = new OV.Option('001', 3, true, true);
		const o2 = new OV.Option('002', 5, true, false);

		q1.addOption(o1);
		expect(function () {
			q1.addOption(o2);
		}).toThrow(new Error('Question already has a blank option'));
	});
});
