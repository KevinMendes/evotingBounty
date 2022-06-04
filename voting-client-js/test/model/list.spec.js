/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
/* global OV */

describe('List model', function () {
	'use strict';

	const list1 = {
		id: '001',
	};

	const list2 = {
		id: '002',
		attribute1: 'Text 1',
		attribute2: 'Text 2',
	};

	it('should init a list', function () {
		const l1 = new OV.List(list1);

		expect(l1.id).toBe('001');
	});

	it('should init a list with min, max, attr1 and attr2', function () {
		const l1 = new OV.List(list2);

		expect(l1.id).toBe('002');
	});

	it('should add candidates', function () {
		const l1 = new OV.List(list2);
		const c1 = new OV.Candidate('001');

		l1.addCandidate(c1);
		expect(l1.candidates.length).toBe(1);
	});
});
